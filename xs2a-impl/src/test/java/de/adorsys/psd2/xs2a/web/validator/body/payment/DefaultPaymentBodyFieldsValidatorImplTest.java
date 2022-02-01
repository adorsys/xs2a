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

package de.adorsys.psd2.xs2a.web.validator.body.payment;

import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.web.validator.body.payment.handler.DefaultPaymentBodyFieldsValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.body.payment.handler.config.DefaultPaymentValidationConfigImpl;
import de.adorsys.psd2.xs2a.web.validator.body.payment.handler.type.PaymentTypeValidator;
import de.adorsys.psd2.xs2a.web.validator.body.payment.handler.type.PaymentTypeValidatorContext;
import de.adorsys.psd2.xs2a.web.validator.body.raw.FieldExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultPaymentBodyFieldsValidatorImplTest {

    private static final String PAYMENT_SERVICE = PaymentType.SINGLE.getValue();

    @InjectMocks
    private DefaultPaymentBodyFieldsValidatorImpl validator;

    @Mock
    private PaymentTypeValidatorContext paymentTypeValidatorContext;
    @Mock
    private FieldExtractor fieldExtractor;
    @Mock
    private PaymentTypeValidator paymentTypeValidator;

    private MessageError messageError;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        messageError = new MessageError();
        request = new MockHttpServletRequest();
    }

    @Test
    void validate_success() {
        when(fieldExtractor.mapBodyToInstance(request, messageError, Object.class)).thenReturn(Optional.of("body"));
        when(paymentTypeValidatorContext.getValidator(PAYMENT_SERVICE)).thenReturn(Optional.of(paymentTypeValidator));

        validator.validate(request, PAYMENT_SERVICE, messageError);

        verify(fieldExtractor, times(1)).mapBodyToInstance(request, messageError, Object.class);
        verify(paymentTypeValidatorContext, times(1)).getValidator(PAYMENT_SERVICE);
        verify(paymentTypeValidator, times(1)).validate(eq("body"), eq(messageError), any(DefaultPaymentValidationConfigImpl.class));
    }

    @Test
    void validate_wrongJsonBody() {
        when(fieldExtractor.mapBodyToInstance(request, messageError, Object.class)).thenReturn(Optional.empty());

        validator.validate(request, PAYMENT_SERVICE, messageError);

        verify(fieldExtractor, times(1)).mapBodyToInstance(request, messageError, Object.class);
        verify(paymentTypeValidatorContext, never()).getValidator(any());
        verify(paymentTypeValidator, never()).validate(any(), any(), any());
    }

    @Test
    void validate_unsupportedPaymentService() {
        when(fieldExtractor.mapBodyToInstance(request, messageError, Object.class)).thenReturn(Optional.of("body"));
        when(paymentTypeValidatorContext.getValidator(PAYMENT_SERVICE)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> validator.validate(request, PAYMENT_SERVICE, messageError));

        verify(fieldExtractor, times(1)).mapBodyToInstance(request, messageError, Object.class);
        verify(paymentTypeValidatorContext, times(1)).getValidator(PAYMENT_SERVICE);
        verify(paymentTypeValidator, never()).validate(any(), any(), any());
    }

    @Test
    void checkPaymentValidationConfig() {
        DefaultPaymentValidationConfigImpl validationConfig = validator.createPaymentValidationConfig();
        assertNotNull(validationConfig);
        assertTrue(validationConfig instanceof DefaultPaymentValidationConfigImpl);
    }
}
