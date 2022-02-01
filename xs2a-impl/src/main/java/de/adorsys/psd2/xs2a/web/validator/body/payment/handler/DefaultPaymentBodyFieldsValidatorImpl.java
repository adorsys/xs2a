/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
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
        if (bodyOptional.isEmpty()) {
            return messageError;
        }

        Optional<PaymentTypeValidator> validator = paymentTypeValidatorContext.getValidator(paymentService);
        if (validator.isEmpty()) {
            throw new IllegalArgumentException("Unsupported payment service");
        }

        return validator.get().validate(bodyOptional.get(), messageError, validationConfig);
    }

    public DefaultPaymentValidationConfigImpl createPaymentValidationConfig() {
        return new DefaultPaymentValidationConfigImpl();
    }

}
