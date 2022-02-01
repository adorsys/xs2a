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

package de.adorsys.psd2.xs2a.service.validator.piis;

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.validator.OauthPiisConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.tpp.PiisConsentTppInfoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @Test
    void buildWarningMessages() {
        //Given
        PiisConsent piisConsent = buildPiisConsent(INVALID_TPP_INFO);
        GetConfirmationOfFundsConsentAuthorisationScaStatusPO confirmation =
            new GetConfirmationOfFundsConsentAuthorisationScaStatusPO(piisConsent, AUTHORISATION_ID);

        //When
        Set<TppMessageInformation> actual =
            getConfirmationOfFundsConsentAuthorisationScaStatusValidator.buildWarningMessages(confirmation);

        //Then
        assertThat(actual).isEmpty();
        verifyNoInteractions(confirmationOfFundsAuthorisationValidator);
        verifyNoInteractions(piisConsentTppInfoValidator);
        verifyNoInteractions(oauthPiisConsentValidator);
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
