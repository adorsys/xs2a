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

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.FrequencyCode;
import de.adorsys.psd2.xs2a.core.pis.PurposeCode;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.profile.StandardPaymentProductsResolver;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.AbstractBodyValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.body.DateFieldValidator;
import de.adorsys.psd2.xs2a.web.validator.body.TppRedirectUriBodyValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.body.payment.type.PaymentTypeValidator;
import de.adorsys.psd2.xs2a.web.validator.body.payment.type.PaymentTypeValidatorContext;
import de.adorsys.psd2.xs2a.web.validator.body.raw.FieldExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aRequestBodyDateFields.PAYMENT_DATE_FIELDS;

@Slf4j
@Component
public class PaymentBodyValidatorImpl extends AbstractBodyValidatorImpl implements PaymentBodyValidator {
    private static final String PAYMENT_SERVICE_PATH_VAR = "payment-service";
    private static final String PAYMENT_PRODUCT_PATH_VAR = "payment-product";
    private static final String PERIODIC_PAYMENT_PATH_VAR = "periodic-payments";
    static final String PURPOSE_CODE_FIELD_NAME = "purposeCode";
    static final String FREQUENCY_FIELD_NAME = "frequency";

    private PaymentTypeValidatorContext paymentTypeValidatorContext;
    private DateFieldValidator dateFieldValidator;

    private TppRedirectUriBodyValidatorImpl tppRedirectUriBodyValidator;
    private final StandardPaymentProductsResolver standardPaymentProductsResolver;
    private final FieldExtractor fieldExtractor;

    @Autowired
    public PaymentBodyValidatorImpl(ErrorBuildingService errorBuildingService, Xs2aObjectMapper xs2aObjectMapper,
                                    PaymentTypeValidatorContext paymentTypeValidatorContext,
                                    StandardPaymentProductsResolver standardPaymentProductsResolver,
                                    TppRedirectUriBodyValidatorImpl tppRedirectUriBodyValidator,
                                    DateFieldValidator dateFieldValidator,
                                    FieldExtractor fieldExtractor) {
        super(errorBuildingService, xs2aObjectMapper);
        this.paymentTypeValidatorContext = paymentTypeValidatorContext;
        this.standardPaymentProductsResolver = standardPaymentProductsResolver;
        this.dateFieldValidator = dateFieldValidator;
        this.tppRedirectUriBodyValidator = tppRedirectUriBodyValidator;
        this.fieldExtractor = fieldExtractor;
    }

    @Override
    public void validate(HttpServletRequest request, MessageError messageError) {
        if (isRawPaymentProduct(getPathParameters(request))) {
            log.info("Raw payment product is detected.");
            return;
        }

        super.validate(request, messageError);
    }

    @Override
    public void validateBodyFields(HttpServletRequest request, MessageError messageError) {
        tppRedirectUriBodyValidator.validate(request, messageError);

        Optional<Object> bodyOptional = mapBodyToInstance(request, messageError, Object.class);

        // In case of wrong JSON - we don't proceed the inner fields validation.
        if (!bodyOptional.isPresent()) {
            return;
        }

        validateInitiatePaymentBody(bodyOptional.get(), getPathParameters(request), messageError);
    }

    @Override
    public void validateRawData(HttpServletRequest request, MessageError messageError) {
        dateFieldValidator.validateDayOfExecution(request, messageError);
        dateFieldValidator.validateDateFormat(request, PAYMENT_DATE_FIELDS.getDateFields(), messageError);

        validateFrequencyForPeriodicPayment(request, messageError);
        validatePurposeCodes(request, messageError);
    }

    private void validateFrequencyForPeriodicPayment(HttpServletRequest request, MessageError messageError) {
        Optional<String> frequencyOptional = fieldExtractor.extractField(request, FREQUENCY_FIELD_NAME, messageError);
        boolean isPeriodicPayment = getPathParameters(request).get(PAYMENT_SERVICE_PATH_VAR).equals(PERIODIC_PAYMENT_PATH_VAR);
        if (isPeriodicPayment) {
            if (!frequencyOptional.isPresent()) {
                errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, FREQUENCY_FIELD_NAME));
            } else if (FrequencyCode.fromValue(frequencyOptional.get()) == null) {
                errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_WRONG_FORMAT_VALUE, FREQUENCY_FIELD_NAME));
            }
        }
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

    private void validatePurposeCodes(HttpServletRequest request, MessageError messageError) {
        List<String> purposeCodes = extractPurposeCodes(request, messageError);
        boolean isPurposeCodeInvalid = purposeCodes.stream()
                                             .map(PurposeCode::fromValue)
                                             .anyMatch(Objects::isNull);

        if (isPurposeCodeInvalid) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_WRONG_FORMAT_VALUE, PURPOSE_CODE_FIELD_NAME));
        }
    }

    private List<String> extractPurposeCodes(HttpServletRequest request, MessageError messageError) {
        List<String> purposeCodes = new ArrayList<>();
        try {
            purposeCodes.addAll(xs2aObjectMapper.toJsonGetValuesForField(request.getInputStream(), PURPOSE_CODE_FIELD_NAME));
        } catch (IOException e) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_DESERIALIZATION_FAIL));
        }
        return purposeCodes;
    }

    private Map<String, String> getPathParameters(HttpServletRequest request) {
        return (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    }
}
