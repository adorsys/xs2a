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

package de.adorsys.psd2.xs2a.web.validator.body.payment.handler;

import de.adorsys.psd2.validator.payment.PaymentBodyFieldsValidator;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.web.validator.body.payment.handler.config.DefaultPaymentValidationConfigImpl;
import de.adorsys.psd2.xs2a.web.validator.body.payment.handler.config.PaymentValidationConfig;
import de.adorsys.psd2.xs2a.web.validator.body.payment.handler.type.PaymentTypeValidator;
import de.adorsys.psd2.xs2a.web.validator.body.payment.handler.type.PaymentTypeValidatorContext;
import de.adorsys.psd2.xs2a.web.validator.body.raw.FieldExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Primary
@Component
public class DefaultPaymentBodyFieldsValidatorImpl implements PaymentBodyFieldsValidator {

    private PaymentTypeValidatorContext paymentTypeValidatorContext;
    private FieldExtractor fieldExtractor;
    private PaymentValidationConfig validationConfig;

    @Autowired
    public DefaultPaymentBodyFieldsValidatorImpl(PaymentTypeValidatorContext paymentTypeValidatorContext,
                                                 FieldExtractor fieldExtractor) {
        this.paymentTypeValidatorContext = paymentTypeValidatorContext;
        this.fieldExtractor = fieldExtractor;
        this.validationConfig = createPaymentValidationConfig();
    }

    @Override
    public MessageError validate(HttpServletRequest request, String paymentService, MessageError messageError) {
        Optional<Object> bodyOptional = fieldExtractor.mapBodyToInstance(request, messageError, Object.class);

        // In case of wrong JSON - we don't proceed to the inner fields validation.
        if (!bodyOptional.isPresent()) {
            return messageError;
        }

        Optional<PaymentTypeValidator> validator = paymentTypeValidatorContext.getValidator(paymentService);
        if (!validator.isPresent()) {
            throw new IllegalArgumentException("Unsupported payment service");
        }

        return validator.get().validate(bodyOptional.get(), messageError, validationConfig);
    }

    public DefaultPaymentValidationConfigImpl createPaymentValidationConfig() {
        return new DefaultPaymentValidationConfigImpl();
    }

}
