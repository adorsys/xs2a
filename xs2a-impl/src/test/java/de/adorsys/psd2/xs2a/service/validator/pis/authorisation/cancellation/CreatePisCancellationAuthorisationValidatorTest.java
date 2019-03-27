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

package de.adorsys.psd2.xs2a.service.validator.pis.authorisation.cancellation;

import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.tpp.PisTppInfoValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.UUID;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.PSU_CREDENTIALS_INVALID;
import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.UNAUTHORIZED;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_401;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreatePisCancellationAuthorisationValidatorTest {
    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("invalid authorisation number");
    private static final UUID X_REQUEST_ID = UUID.fromString("1af360bc-13cb-40ab-9aa0-cc0d6af4510c");

    private static final PsuIdData PSU_DATA = new PsuIdData("psu id", null, null, null);
    private static final PsuIdData EMPTY_PSU_DATA = new PsuIdData(null, null, null, null);
    private static final PsuIdData INVALID_PSU_DATA = new PsuIdData("invalid psu id", null, null, null);

    private static final MessageError TPP_VALIDATION_ERROR =
        new MessageError(ErrorType.PIS_401, TppMessageInformation.of(UNAUTHORIZED));
    private static final MessageError INVALID_PSU_ERROR = new MessageError(PIS_401, of(PSU_CREDENTIALS_INVALID));

    @Mock
    private PisTppInfoValidator pisTppInfoValidator;
    @Mock
    private RequestProviderService requestProviderService;

    @InjectMocks
    private CreatePisCancellationAuthorisationValidator createPisCancellationAuthorisationValidator;

    @Before
    public void setUp() {
        // Inject pisTppInfoValidator via setter
        createPisCancellationAuthorisationValidator.setPisTppInfoValidator(pisTppInfoValidator);

        when(requestProviderService.getRequestId()).thenReturn(X_REQUEST_ID);

        when(pisTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(pisTppInfoValidator.validateTpp(INVALID_TPP_INFO))
            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));
    }

    @Test
    public void validate_withValidPaymentObject_shouldReturnValid() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);

        // When
        ValidationResult validationResult = createPisCancellationAuthorisationValidator.validate(new CreatePisCancellationAuthorisationPO(commonPaymentResponse, PSU_DATA));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validate_withValidPaymentObjectAndEmptyPsuData_shouldReturnValid() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);

        // When
        ValidationResult validationResult = createPisCancellationAuthorisationValidator.validate(new CreatePisCancellationAuthorisationPO(commonPaymentResponse, EMPTY_PSU_DATA));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validate_withInvalidTppInPayment_shouldReturnTppValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(INVALID_TPP_INFO);

        // When
        ValidationResult validationResult = createPisCancellationAuthorisationValidator.validate(new CreatePisCancellationAuthorisationPO(commonPaymentResponse, PSU_DATA));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }


    @Test
    public void validate_withInvalidPsuData_shouldReturnPsuValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);

        // When
        ValidationResult validationResult = createPisCancellationAuthorisationValidator.validate(new CreatePisCancellationAuthorisationPO(commonPaymentResponse, INVALID_PSU_DATA));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(INVALID_PSU_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validate_withInvalidPsuDataAndInvalidTppInPayment_shouldReturnTppValidationErrorFirst() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(INVALID_TPP_INFO);

        // When
        ValidationResult validationResult = createPisCancellationAuthorisationValidator.validate(new CreatePisCancellationAuthorisationPO(commonPaymentResponse, INVALID_PSU_DATA));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }

    private PisCommonPaymentResponse buildPisCommonPaymentResponse(TppInfo tppInfo) {
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        pisCommonPaymentResponse.setTppInfo(tppInfo);
        pisCommonPaymentResponse.setPsuData(Collections.singletonList(PSU_DATA));
        return pisCommonPaymentResponse;
    }
}
