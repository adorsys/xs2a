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
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.validator.tpp.PiisConsentTppInfoValidator;
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
class DeleteConfirmationOfFundsConsentByIdValidatorTest {
    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("invalid authorisation number");
    private static final MessageError TPP_VALIDATION_ERROR =
        new MessageError(ErrorType.PIIS_401, TppMessageInformation.of(UNAUTHORIZED));

    @InjectMocks
    private DeleteConfirmationOfFundsConsentByIdValidator deleteConfirmationOfFundsConsentByIdValidator;
    @Mock
    private PiisConsentTppInfoValidator piisConsentTppInfoValidator;

    @BeforeEach
    void setUp() {
        // Inject piisConsentTppInfoValidator via setter
        deleteConfirmationOfFundsConsentByIdValidator.setPiisConsentTppInfoValidator(piisConsentTppInfoValidator);
    }

    @Test
    void validate_withValidConsentObject_shouldReturnValid() {
        // Given
        PiisConsent piisConsent = buildPiisConsent(TPP_INFO);
        when(piisConsentTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        // When
        ValidationResult validationResult = deleteConfirmationOfFundsConsentByIdValidator.validate(new CommonConfirmationOfFundsConsentObject(piisConsent));
        // Then
        verify(piisConsentTppInfoValidator).validateTpp(piisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidTppInConsent_shouldReturnTppValidationError() {
        // Given
        PiisConsent piisConsent = buildPiisConsent(INVALID_TPP_INFO);
        when(piisConsentTppInfoValidator.validateTpp(INVALID_TPP_INFO))
            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));
        // When
        ValidationResult validationResult = deleteConfirmationOfFundsConsentByIdValidator.validate(new CommonConfirmationOfFundsConsentObject(piisConsent));
        // Then
        verify(piisConsentTppInfoValidator).validateTpp(piisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    private PiisConsent buildPiisConsent(TppInfo tppInfo) {
        PiisConsent piisConsent = new PiisConsent(ConsentType.PIIS_TPP);
        ConsentTppInformation consentTppInfo = new ConsentTppInformation();
        consentTppInfo.setTppInfo(tppInfo);
        piisConsent.setConsentTppInformation(consentTppInfo);
        return piisConsent;
    }

    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }

    @Test
    void buildWarningMessages() {
        //Given
        PiisConsent piisConsent = buildPiisConsent(INVALID_TPP_INFO);
        CommonConfirmationOfFundsConsentObject commonConfirmationOfFundsConsentObject = new CommonConfirmationOfFundsConsentObject(piisConsent);

        //When
        Set<TppMessageInformation> actual = deleteConfirmationOfFundsConsentByIdValidator.buildWarningMessages(commonConfirmationOfFundsConsentObject);

        //Then
        assertThat(actual).isEmpty();
        verifyNoInteractions(piisConsentTppInfoValidator);
    }
}
