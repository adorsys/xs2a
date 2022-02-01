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

package de.adorsys.psd2.xs2a.service.validator.ais.consent;

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.validator.OauthConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.tpp.AisConsentTppInfoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetConsentAuthorisationScaStatusValidatorTest {
    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("invalid authorisation number");
    private static final String AUTHORISATION_ID = "random";

    private static final MessageError TPP_VALIDATION_ERROR =
        new MessageError(ErrorType.PIS_401, TppMessageInformation.of(UNAUTHORIZED));

    @Mock
    private AisAuthorisationValidator aisAuthorisationValidator;

    @Mock
    private AisConsentTppInfoValidator aisConsentTppInfoValidator;

    @Mock
    private OauthConsentValidator oauthConsentValidator;

    @InjectMocks
    private GetConsentAuthorisationScaStatusValidator getConsentAuthorisationScaStatusValidator;

    @BeforeEach
    void setUp() {
        // Inject pisTppInfoValidator via setter
        getConsentAuthorisationScaStatusValidator.setAisConsentTppInfoValidator(aisConsentTppInfoValidator);
    }

    @Test
    void validate_withValidConsentObject_shouldReturnValid() {
        // Given
        AisConsent aisConsent = buildAccountConsent(TPP_INFO);
        when(aisConsentTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(aisAuthorisationValidator.validate(AUTHORISATION_ID, aisConsent)).thenReturn(ValidationResult.valid());
        when(oauthConsentValidator.validate(aisConsent))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = getConsentAuthorisationScaStatusValidator.validate(new GetConsentAuthorisationScaStatusPO(aisConsent, AUTHORISATION_ID));

        // Then
        verify(aisConsentTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withValidConsentObject_oauth() {
        // Given
        AisConsent aisConsent = buildAccountConsent(TPP_INFO);
        when(aisConsentTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(aisAuthorisationValidator.validate(AUTHORISATION_ID, aisConsent)).thenReturn(ValidationResult.valid());
        when(oauthConsentValidator.validate(aisConsent))
            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = getConsentAuthorisationScaStatusValidator.validate(new GetConsentAuthorisationScaStatusPO(aisConsent, AUTHORISATION_ID));

        // Then
        verify(aisConsentTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
    }

    @Test
    void validate_withValidConsentObject_authorisationValidation_error() {
        // Given
        AisConsent aisConsent = buildAccountConsent(TPP_INFO);
        when(aisConsentTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(aisAuthorisationValidator.validate(AUTHORISATION_ID, aisConsent)).thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = getConsentAuthorisationScaStatusValidator.validate(new GetConsentAuthorisationScaStatusPO(aisConsent, AUTHORISATION_ID));

        // Then
        verify(aisConsentTppInfoValidator).validateTpp(aisConsent.getTppInfo());
        verifyNoInteractions(oauthConsentValidator);

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
    }

    @Test
    void validate_withInvalidTppInConsent_shouldReturnTppValidationError() {
        // Given
        AisConsent aisConsent = buildAccountConsent(INVALID_TPP_INFO);
        when(aisConsentTppInfoValidator.validateTpp(INVALID_TPP_INFO))
            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = getConsentAuthorisationScaStatusValidator.validate(new GetConsentAuthorisationScaStatusPO(aisConsent, AUTHORISATION_ID));

        // Then
        verify(aisConsentTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void buildWarningMessages() {
        // Given
        AisConsent aisConsent = buildAccountConsent(INVALID_TPP_INFO);
        GetConsentAuthorisationScaStatusPO getConsentAuthorisationScaStatusPO =
            new GetConsentAuthorisationScaStatusPO(aisConsent, AUTHORISATION_ID);

        //When
        Set<TppMessageInformation> actual =
            getConsentAuthorisationScaStatusValidator.buildWarningMessages(getConsentAuthorisationScaStatusPO);

        //Then
        assertThat(actual).isEmpty();
        verifyNoInteractions(aisAuthorisationValidator);
        verifyNoInteractions(aisConsentTppInfoValidator);
        verifyNoInteractions(oauthConsentValidator);
    }

    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }

    private AisConsent buildAccountConsent(TppInfo tppInfo) {
        AisConsent aisConsent = new AisConsent();
        ConsentTppInformation consentTppInformation = new ConsentTppInformation();
        consentTppInformation.setTppInfo(tppInfo);
        aisConsent.setConsentTppInformation(consentTppInformation);
        return aisConsent;
    }
}
