/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
public class DefaultPaymentBodyFieldsValidatorImplTest {

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
