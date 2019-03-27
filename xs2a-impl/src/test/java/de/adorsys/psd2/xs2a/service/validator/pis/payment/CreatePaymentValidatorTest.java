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

package de.adorsys.psd2.xs2a.service.validator.pis.payment;

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.FORMAT_ERROR;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_400;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreatePaymentValidatorTest {
    private static final PsuIdData PSU_DATA =
        new PsuIdData("psu id", null, null, null);
    private static final PsuIdData EMPTY_PSU_DATA =
        new PsuIdData(null, null, null, null);
    private static final MessageError NO_PSU_ERROR =
        new MessageError(PIS_400, TppMessageInformation.of(FORMAT_ERROR, "Please provide the PSU identification data"));
    private static final UUID X_REQUEST_ID = UUID.fromString("1af360bc-13cb-40ab-9aa0-cc0d6af4510c");

    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;
    @Mock
    private RequestProviderService requestProviderService;

    @InjectMocks
    private CreatePaymentValidator createPaymentValidator;

    @Before
    public void setUp() {
        when(requestProviderService.getRequestId()).thenReturn(X_REQUEST_ID);
    }

    @Test
    public void validate_withValidInitiationParameters_shouldReturnValid() {
        // Given
        when(aspspProfileServiceWrapper.isPsuInInitialRequestMandated())
            .thenReturn(false);

        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(PSU_DATA);

        // When
        ValidationResult validationResult = createPaymentValidator.validate(paymentInitiationParameters);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validate_withEmptyPsuDataInParameters_withNotMandatedPsu_shouldReturnValid() {
        // Given
        when(aspspProfileServiceWrapper.isPsuInInitialRequestMandated())
            .thenReturn(false);

        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(EMPTY_PSU_DATA);

        // When
        ValidationResult validationResult = createPaymentValidator.validate(paymentInitiationParameters);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validate_withEmptyPsuDataInParameters_withMandatedPsu_shouldReturnError() {
        // Given
        when(aspspProfileServiceWrapper.isPsuInInitialRequestMandated())
            .thenReturn(true);

        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(EMPTY_PSU_DATA);

        // When
        ValidationResult validationResult = createPaymentValidator.validate(paymentInitiationParameters);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(NO_PSU_ERROR, validationResult.getMessageError());
    }

    private PaymentInitiationParameters buildPaymentInitiationParameters(PsuIdData psuIdData) {
        PaymentInitiationParameters requestParameters = new PaymentInitiationParameters();
        requestParameters.setPsuData(psuIdData);
        return requestParameters;
    }
}
