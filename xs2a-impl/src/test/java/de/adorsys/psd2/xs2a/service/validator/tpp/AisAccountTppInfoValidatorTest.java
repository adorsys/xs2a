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

package de.adorsys.psd2.xs2a.service.validator.tpp;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AisAccountTppInfoValidatorTest {
    private static final String TPP_AUTHORITY_ID = "authority id";
    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
    private static final TppInfo DIFFERENT_TPP_INFO = buildTppInfo("different authorisation number");

    @Mock
    private TppInfoCheckerService tppInfoCheckerService;

    @InjectMocks
    private AisAccountTppInfoValidator aisAccountTppInfoValidator;

    @Test
    void validateTpp_withSameTppInConsentAsInRequest_shouldReturnValid() {
        when(tppInfoCheckerService.differsFromTppInRequest(TPP_INFO)).thenReturn(false);
        // When
        ValidationResult validationResult = aisAccountTppInfoValidator.validateTpp(TPP_INFO);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validateTpp_withDifferentTppInConsent_shouldReturnError() {
        // Given
        MessageError expectedError = new MessageError(ErrorType.AIS_400,
                                                      TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400_INCORRECT_CERTIFICATE));
        when(tppInfoCheckerService.differsFromTppInRequest(DIFFERENT_TPP_INFO)).thenReturn(true);
        // When
        ValidationResult validationResult = aisAccountTppInfoValidator.validateTpp(DIFFERENT_TPP_INFO);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());

        MessageError actualError = validationResult.getMessageError();
        assertNotNull(actualError);
        assertEquals(expectedError.getErrorType(), actualError.getErrorType());
        assertEquals(expectedError.getTppMessage(), actualError.getTppMessage());
    }

    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        tppInfo.setAuthorityId(TPP_AUTHORITY_ID);
        return tppInfo;
    }
}
