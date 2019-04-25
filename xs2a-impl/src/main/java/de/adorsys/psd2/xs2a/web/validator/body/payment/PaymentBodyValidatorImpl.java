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
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.AbstractBodyValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.body.payment.type.PaymentTypeValidator;
import de.adorsys.psd2.xs2a.web.validator.body.payment.type.PaymentTypeValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

@Component
public class PaymentBodyValidatorImpl extends AbstractBodyValidatorImpl implements PaymentBodyValidator {

    private static final String PAYMENT_SERVICE_PATH_VAR = "payment-service";
    private PaymentTypeValidatorContext paymentTypeValidatorContext;

    @Autowired
    public PaymentBodyValidatorImpl(ErrorBuildingService errorBuildingService, ObjectMapper objectMapper,
                                    PaymentTypeValidatorContext paymentTypeValidatorContext) {
        super(errorBuildingService, objectMapper);
        this.paymentTypeValidatorContext = paymentTypeValidatorContext;
    }

    @Override
    public void validate(HttpServletRequest request, MessageError messageError) {

        Optional<Object> bodyOptional = mapBodyToInstance(request, messageError, Object.class);

        // In case of wrong JSON - we don't proceed the inner fields validation.
        if (!bodyOptional.isPresent()) {
            return;
        }

        Map<String, String> pathParametersMap = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
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
}
