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

package de.adorsys.psd2.xs2a.service.validator.piis;

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.validator.OauthPiisConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.tpp.PiisConsentTppInfoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetConfirmationOfFundsConsentAuthorisationScaStatusValidatorTest {
    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("invalid authorisation number");
    private static final String AUTHORISATION_ID = "random";

    private static final MessageError TPP_VALIDATION_ERROR =
        new MessageError(ErrorType.PIIS_401, TppMessageInformation.of(UNAUTHORIZED));

    @Mock
    private ConfirmationOfFundsAuthorisationValidator confirmationOfFundsAuthorisationValidator;

    @Mock
    private PiisConsentTppInfoValidator piisConsentTppInfoValidator;

    @Mock
    private OauthPiisConsentValidator oauthPiisConsentValidator;

    @InjectMocks
    private GetConfirmationOfFundsConsentAuthorisationScaStatusValidator getConfirmationOfFundsConsentAuthorisationScaStatusValidator;

    @BeforeEach
    void setUp() {
        // Inject pisTppInfoValidator via setter
        getConfirmationOfFundsConsentAuthorisationScaStatusValidator.setPiisConsentTppInfoValidator(piisConsentTppInfoValidator);
    }

    @Test
    void validate_withValidConsentObject_shouldReturnValid() {
        // Given
        PiisConsent piisConsent = buildPiisConsent(TPP_INFO);
        when(piisConsentTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(confirmationOfFundsAuthorisationValidator.validate(AUTHORISATION_ID, piisConsent)).thenReturn(ValidationResult.valid());
        when(oauthPiisConsentValidator.validate(piisConsent))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = getConfirmationOfFundsConsentAuthorisationScaStatusValidator.validate(new GetConfirmationOfFundsConsentAuthorisationScaStatusPO(piisConsent, AUTHORISATION_ID));

        // Then
        verify(piisConsentTppInfoValidator).validateTpp(piisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_AuthorisationValidatorFailed_shouldReturnInvalid() {
        // Given
        PiisConsent piisConsent = buildPiisConsent(TPP_INFO);
        when(piisConsentTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        ValidationResult authorisationValidationResult = ValidationResult.invalid(ErrorType.PIIS_403, RESOURCE_UNKNOWN_403);
        when(confirmationOfFundsAuthorisationValidator.validate(AUTHORISATION_ID, piisConsent))
            .thenReturn(authorisationValidationResult);

        // When
        ValidationResult validationResult = getConfirmationOfFundsConsentAuthorisationScaStatusValidator.validate(new GetConfirmationOfFundsConsentAuthorisationScaStatusPO(piisConsent, AUTHORISATION_ID));

        // Then
        verify(piisConsentTppInfoValidator).validateTpp(piisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(authorisationValidationResult, validationResult);
    }

    @Test
    void validate_OauthValidatorFailed_shouldReturnInvalid() {
        // Given
        PiisConsent piisConsent = buildPiisConsent(TPP_INFO);
        when(piisConsentTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(confirmationOfFundsAuthorisationValidator.validate(AUTHORISATION_ID, piisConsent)).thenReturn(ValidationResult.valid());
        ValidationResult oauthValidationResult = ValidationResult.invalid(ErrorType.PIIS_403, FORBIDDEN);
        when(oauthPiisConsentValidator.validate(piisConsent))
            .thenReturn(oauthValidationResult);

        // When
        ValidationResult validationResult = getConfirmationOfFundsConsentAuthorisationScaStatusValidator.validate(new GetConfirmationOfFundsConsentAuthorisationScaStatusPO(piisConsent, AUTHORISATION_ID));

        // Then
        verify(piisConsentTppInfoValidator).validateTpp(piisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(oauthValidationResult, validationResult);
    }

    @Test
    void validate_withInvalidTppInConsent_shouldReturnTppValidationError() {
        // Given
        PiisConsent piisConsent = buildPiisConsent(INVALID_TPP_INFO);
        when(piisConsentTppInfoValidator.validateTpp(INVALID_TPP_INFO))
            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = getConfirmationOfFundsConsentAuthorisationScaStatusValidator.validate(new GetConfirmationOfFundsConsentAuthorisationScaStatusPO(piisConsent, AUTHORISATION_ID));

        // Then
        verify(piisConsentTppInfoValidator).validateTpp(piisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }

    private PiisConsent buildPiisConsent(TppInfo tppInfo) {
        PiisConsent piisConsent = new PiisConsent();
        ConsentTppInformation consentTppInformation = new ConsentTppInformation();
        consentTppInformation.setTppInfo(tppInfo);
        piisConsent.setConsentTppInformation(consentTppInformation);
        return piisConsent;
    }
}
