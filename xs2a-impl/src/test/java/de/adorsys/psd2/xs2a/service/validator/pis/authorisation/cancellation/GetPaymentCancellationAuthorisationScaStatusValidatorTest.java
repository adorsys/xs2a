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
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.pis.CommonPaymentObject;
import de.adorsys.psd2.xs2a.service.validator.tpp.PisTppInfoValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.UNAUTHORIZED;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GetPaymentCancellationAuthorisationScaStatusValidatorTest {
    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("invalid authorisation number");

    private static final MessageError TPP_VALIDATION_ERROR =
        new MessageError(ErrorType.PIS_401, TppMessageInformation.of(UNAUTHORIZED));

    @Mock
    private PisTppInfoValidator pisTppInfoValidator;

    @InjectMocks
    private GetPaymentCancellationAuthorisationScaStatusValidator getPaymentCancellationAuthorisationScaStatusValidator;

    @Before
    public void setUp() {
        // Inject pisTppInfoValidator via setter
        getPaymentCancellationAuthorisationScaStatusValidator.setPisTppInfoValidator(pisTppInfoValidator);

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
        ValidationResult validationResult = getPaymentCancellationAuthorisationScaStatusValidator.validate(new CommonPaymentObject(commonPaymentResponse));

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
        ValidationResult validationResult = getPaymentCancellationAuthorisationScaStatusValidator.validate(new CommonPaymentObject(commonPaymentResponse));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

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
        return pisCommonPaymentResponse;
    }
}
