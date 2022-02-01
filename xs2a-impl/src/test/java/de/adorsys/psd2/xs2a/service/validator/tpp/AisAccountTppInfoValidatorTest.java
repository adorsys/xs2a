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
