/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.xs2a.web.validator.body.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.xs2a.component.JsonConverter;
import de.adorsys.psd2.xs2a.core.pis.PurposeCode;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.profile.StandardPaymentProductsResolver;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.AbstractBodyValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.body.TppRedirectUriBodyValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.body.DateFieldValidator;
import de.adorsys.psd2.xs2a.web.validator.body.payment.type.PaymentTypeValidator;
import de.adorsys.psd2.xs2a.web.validator.body.payment.type.PaymentTypeValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aRequestBodyDateFields.PAYMENT_DATE_FIELDS;

@Component
public class PaymentBodyValidatorImpl extends AbstractBodyValidatorImpl implements PaymentBodyValidator {
    private static final String PAYMENT_SERVICE_PATH_VAR = "payment-service";
    private static final String PAYMENT_PRODUCT_PATH_VAR = "payment-product";
    private static final String PURPOSE_CODE_FIELD_NAME = "purposeCode";
    private static final String BODY_DESERIALIZATION_ERROR = "Cannot deserialize the request body";
    static final String PURPOSE_CODE_ERROR_FORMAT = "Field 'purposeCode' has wrong value";

    private PaymentTypeValidatorContext paymentTypeValidatorContext;
    private DateFieldValidator dateFieldValidator;

    private final JsonConverter jsonConverter;
    private TppRedirectUriBodyValidatorImpl tppRedirectUriBodyValidator;
    private final StandardPaymentProductsResolver standardPaymentProductsResolver;

    @Autowired
    public PaymentBodyValidatorImpl(ErrorBuildingService errorBuildingService, ObjectMapper objectMapper,
                                    PaymentTypeValidatorContext paymentTypeValidatorContext,
                                    StandardPaymentProductsResolver standardPaymentProductsResolver,
                                    JsonConverter jsonConverter, TppRedirectUriBodyValidatorImpl tppRedirectUriBodyValidator,
                                    DateFieldValidator dateFieldValidator) {
        super(errorBuildingService, objectMapper);
        this.paymentTypeValidatorContext = paymentTypeValidatorContext;
        this.standardPaymentProductsResolver = standardPaymentProductsResolver;
        this.dateFieldValidator = dateFieldValidator;
        this.jsonConverter = jsonConverter;
        this.tppRedirectUriBodyValidator = tppRedirectUriBodyValidator;
    }

    @Override
    public void validateBodyFields(HttpServletRequest request, MessageError messageError) {
        tppRedirectUriBodyValidator.validate(request, messageError);

        Map<String, String> pathParametersMap = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        if (isRawPaymentProduct(pathParametersMap)) {
            return;
        }

        Optional<Object> bodyOptional = mapBodyToInstance(request, messageError, Object.class);

        // In case of wrong JSON - we don't proceed the inner fields validation.
        if (!bodyOptional.isPresent()) {
            return;
        }

        validateInitiatePaymentBody(bodyOptional.get(), pathParametersMap, messageError);
    }

    @Override
    public void validateRawData(HttpServletRequest request, MessageError messageError) {
        dateFieldValidator.validateDayOfExecution(request, messageError);
        dateFieldValidator.validateDateFormat(request, PAYMENT_DATE_FIELDS.getDateFields(), messageError);
        List<String> purposeCodes = extractPurposeCodes(request, messageError);
        validatePurposeCodes(purposeCodes, messageError);
    }

    private void validateInitiatePaymentBody(Object body, Map<String, String> pathParametersMap, MessageError messageError) {
        String paymentService = pathParametersMap.get(PAYMENT_SERVICE_PATH_VAR);

        Optional<PaymentTypeValidator> validator = paymentTypeValidatorContext.getValidator(paymentService);
        if (!validator.isPresent()) {
            throw new IllegalArgumentException("Unsupported payment service");
        }
        validator.get().validate(body, messageError);
    }

    private boolean isRawPaymentProduct(Map<String, String> pathParametersMap) {
        String paymentProduct = pathParametersMap.get(PAYMENT_PRODUCT_PATH_VAR);
        return standardPaymentProductsResolver.isRawPaymentProduct(paymentProduct);
    }

    private void validatePurposeCodes(List<String> purposeCodes, MessageError messageError) {
        boolean isPurposeCodesNotValid = purposeCodes.stream()
                                             .map(PurposeCode::fromValue)
                                             .anyMatch(Objects::isNull);

        if (isPurposeCodesNotValid) {
            enrichFormatMessageError(PURPOSE_CODE_ERROR_FORMAT, messageError);
        }
    }

    private void enrichFormatMessageError(String message, MessageError messageError) {
        errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR, message));
    }

    private List<String> extractPurposeCodes(HttpServletRequest request, MessageError messageError) {
        List<String> purposeCodes = new ArrayList<>();
        try {
            purposeCodes.addAll(jsonConverter.toJsonGetValuesForField(request.getInputStream(), PURPOSE_CODE_FIELD_NAME));
        } catch (IOException e) {
            errorBuildingService.enrichMessageError(messageError, BODY_DESERIALIZATION_ERROR);
        }
        return purposeCodes;
    }
}
