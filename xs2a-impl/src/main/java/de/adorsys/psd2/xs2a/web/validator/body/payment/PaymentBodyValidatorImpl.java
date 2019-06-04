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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.xs2a.component.JsonConverter;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.profile.StandardPaymentProductsResolver;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.AbstractBodyValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.body.payment.type.PaymentTypeValidator;
import de.adorsys.psd2.xs2a.web.validator.body.payment.type.PaymentTypeValidatorContext;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Component
public class PaymentBodyValidatorImpl extends AbstractBodyValidatorImpl implements PaymentBodyValidator {
    private static final String PAYMENT_SERVICE_PATH_VAR = "payment-service";
    private static final String PAYMENT_PRODUCT_PATH_VAR = "payment-product";
    private static final String DAY_OF_EXECUTION_FIELD_NAME = "dayOfExecution";
    private static final String DAY_OF_MONTH_REGEX = "([1-9]|[12]\\d|3[01])";
    private static final String DAY_OF_EXECUTION_WRONG_VALUE_ERROR = "Value 'dayOfExecution' should be a number of day in month";
    private static final String BODY_DESERIALIZATION_ERROR = "Cannot deserialize the request body";

    private PaymentTypeValidatorContext paymentTypeValidatorContext;

    private final JsonConverter jsonConverter;
    private final StandardPaymentProductsResolver standardPaymentProductsResolver;

    @Autowired
    public PaymentBodyValidatorImpl(ErrorBuildingService errorBuildingService, ObjectMapper objectMapper,
                                    PaymentTypeValidatorContext paymentTypeValidatorContext,
                                    StandardPaymentProductsResolver standardPaymentProductsResolver,
                                    JsonConverter jsonConverter) {
        super(errorBuildingService, objectMapper);
        this.paymentTypeValidatorContext = paymentTypeValidatorContext;
        this.standardPaymentProductsResolver = standardPaymentProductsResolver;
        this.jsonConverter = jsonConverter;
    }

    @Override
    public void validate(HttpServletRequest request, MessageError messageError) {
        Map<String, String> pathParametersMap = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        if (isRawPaymentProduct(pathParametersMap)) {
            return;
        }

        Optional<Object> bodyOptional = mapBodyToInstance(request, messageError, Object.class);

        // In case of wrong JSON - we don't proceed the inner fields validation.
        if (!bodyOptional.isPresent()) {
            return;
        }

        validateRawData(request, messageError);

        validateInitiatePaymentBody(bodyOptional.get(), pathParametersMap, messageError);
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

    private void validateRawData(HttpServletRequest request, MessageError messageError) {
        String dayOfExecution = extractDayOfExecution(request, messageError);
        validateDayOfExecutionValue(dayOfExecution, messageError);
    }

    private void validateDayOfExecutionValue(String value, MessageError messageError) {
        if (value == null) {
            return;
        }

        if (!isNumberADayOfMonth(value)) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR, DAY_OF_EXECUTION_WRONG_VALUE_ERROR));
        }
    }

    private boolean isNumberADayOfMonth(@NotNull String value) {
        return value.matches(DAY_OF_MONTH_REGEX);
    }

    private String extractDayOfExecution(HttpServletRequest request, MessageError messageError) {
        Optional<String> dayOfExecutionOptional = Optional.empty();
        try {
            // TODO: create common class with Jackson's functionality instead of two: JsonConverter and ObjectMapper.
            //  https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/870
            dayOfExecutionOptional = jsonConverter.toJsonField(request.getInputStream(), DAY_OF_EXECUTION_FIELD_NAME, new TypeReference<String>() {
            });
        } catch (IOException e) {
            errorBuildingService.enrichMessageError(messageError, BODY_DESERIALIZATION_ERROR);
        }

        return dayOfExecutionOptional.orElse(null);
    }
}
