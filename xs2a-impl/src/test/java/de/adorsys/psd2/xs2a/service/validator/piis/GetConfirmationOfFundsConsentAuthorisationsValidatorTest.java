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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetConfirmationOfFundsConsentAuthorisationsValidatorTest {
    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");

    @Mock
    private ConfirmationOfFundsAuthorisationValidator confirmationOfFundsAuthorisationValidator;

    @Mock
    private PiisConsentTppInfoValidator piisConsentTppInfoValidator;

    @Mock
    private OauthPiisConsentValidator oauthPiisConsentValidator;

    @InjectMocks
    private GetConfirmationOfFundsConsentAuthorisationsValidator getConfirmationOfFundsConsentAuthorisationsValidator;


    @BeforeEach
    void setUp() {
        // Inject pisTppInfoValidator via setter
        getConfirmationOfFundsConsentAuthorisationsValidator.setPiisConsentTppInfoValidator(piisConsentTppInfoValidator);
    }

    @Test
    void validate_withValidConsentObject_shouldReturnValid() {
        // Given
        PiisConsent piisConsent = buildPiisConsent(TPP_INFO);
        when(piisConsentTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(oauthPiisConsentValidator.validate(piisConsent))
            .thenReturn(ValidationResult.valid());

        CommonConfirmationOfFundsConsentObject consentObject = new CommonConfirmationOfFundsConsentObject(piisConsent);

        // When
        ValidationResult validationResult = getConfirmationOfFundsConsentAuthorisationsValidator.validate(consentObject);

        // Then
        verify(piisConsentTppInfoValidator).validateTpp(piisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
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
