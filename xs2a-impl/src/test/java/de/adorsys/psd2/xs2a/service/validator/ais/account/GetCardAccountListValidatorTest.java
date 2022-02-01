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

package de.adorsys.psd2.xs2a.service.validator.ais.account;

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.validator.OauthConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.GetCardAccountListConsentObject;
import de.adorsys.psd2.xs2a.service.validator.tpp.AisAccountTppInfoValidator;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_401;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCardAccountListValidatorTest {
    private static final String REQUEST_URI = "/accounts";
    private static final TppInfo TPP_INFO = buildTppInfo("PSDDE-FAKENCA-87B2AC");
    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("invalid authorisation number");
    private static final MessageError TPP_VALIDATION_ERROR = new MessageError(ErrorType.AIS_401, TppMessageInformation.of(UNAUTHORIZED));
    private static final MessageError ACCESS_VALIDATION_ERROR = new MessageError(ErrorType.AIS_401, TppMessageInformation.of(CONSENT_INVALID));
    private static final MessageError FORBIDDEN_ERROR = new MessageError(ErrorType.AIS_403, TppMessageInformation.of(MessageErrorCode.FORBIDDEN));

    @InjectMocks
    private GetCardAccountListValidator getCardAccountListValidator;

    @Mock
    private AccountConsentValidator accountConsentValidator;
    @Mock
    private OauthConsentValidator oauthConsentValidator;
    @Mock
    private AisAccountTppInfoValidator aisAccountTppInfoValidator;

    private final JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        // Inject pisTppInfoValidator via setter
        getCardAccountListValidator.setAisAccountTppInfoValidator(aisAccountTppInfoValidator);
    }

    @Test
    void validate_withValidConsentObject_maskedPan_shouldReturnValid() {
        // Given
        AisConsent aisConsent = buildAisConsent(TPP_INFO);
        when(accountConsentValidator.validate(aisConsent, REQUEST_URI))
            .thenReturn(ValidationResult.valid());
        when(oauthConsentValidator.validate(aisConsent))
            .thenReturn(ValidationResult.valid());
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = getCardAccountListValidator.validate(new GetCardAccountListConsentObject(aisConsent, REQUEST_URI));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withValidConsentObject_Iban_shouldReturnInvalid() {
        // Given
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/ais-consent-with-iban.json", AisConsent.class);
        aisConsent.setConsentData(AisConsentData.buildDefaultAisConsentData());

        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = getCardAccountListValidator.validate(new GetCardAccountListConsentObject(aisConsent, REQUEST_URI));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(ACCESS_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withValidConsentObject_mixedAccountReference_shouldReturnValid() {
        // Given
        AisConsent aisConsent = buildAisConsent(TPP_INFO);
        when(accountConsentValidator.validate(aisConsent, REQUEST_URI))
            .thenReturn(ValidationResult.valid());
        when(oauthConsentValidator.validate(aisConsent))
            .thenReturn(ValidationResult.valid());
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());

        // When
        GetCardAccountListConsentObject getCardAccountListConsentObject = new GetCardAccountListConsentObject(aisConsent, REQUEST_URI);
        ValidationResult validationResult = getCardAccountListValidator.validate(getCardAccountListConsentObject);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidTppInConsent_shouldReturnTppValidationError() {
        // Given
        AisConsent aisConsent = buildAisConsent(INVALID_TPP_INFO);
        when(aisAccountTppInfoValidator.validateTpp(INVALID_TPP_INFO))
            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        // When
        GetCardAccountListConsentObject getCardAccountListConsentObject = new GetCardAccountListConsentObject(aisConsent, REQUEST_URI);
        ValidationResult validationResult = getCardAccountListValidator.validate(getCardAccountListConsentObject);

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidConsent_shouldReturnInvalid() {
        // Given
        AisConsent aisConsent = buildAisConsent(TPP_INFO);
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        ValidationResult validationResultExpected = ValidationResult.invalid(AIS_401, CONSENT_EXPIRED);
        when(accountConsentValidator.validate(aisConsent, REQUEST_URI))
            .thenReturn(validationResultExpected);

        // When
        ValidationResult validationResult = getCardAccountListValidator.validate(new GetCardAccountListConsentObject(aisConsent, REQUEST_URI));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(validationResultExpected.getMessageError(), validationResult.getMessageError());
    }

    @Test
    void validate_withOauthConcentInvalid_shouldReturnInvalid() {
        // Given
        AisConsent aisConsent = buildAisConsent(TPP_INFO);
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(accountConsentValidator.validate(aisConsent, REQUEST_URI))
            .thenReturn(ValidationResult.valid());
        when(oauthConsentValidator.validate(aisConsent))
            .thenReturn(ValidationResult.invalid(FORBIDDEN_ERROR));

        // When
        ValidationResult validationResult = getCardAccountListValidator.validate(new GetCardAccountListConsentObject(aisConsent, REQUEST_URI));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(FORBIDDEN_ERROR, validationResult.getMessageError());
    }

    @Test
    void buildWarningMessages() {
        // Given
        AisConsent aisConsent = buildAisConsent(TPP_INFO);
        GetCardAccountListConsentObject getCardAccountListConsentObject =
            new GetCardAccountListConsentObject(aisConsent, REQUEST_URI);

        //When
        Set<TppMessageInformation> actual =
            getCardAccountListValidator.buildWarningMessages(getCardAccountListConsentObject);

        //Then
        assertThat(actual).isEmpty();
        verifyNoInteractions(accountConsentValidator);
        verifyNoInteractions(oauthConsentValidator);
        verifyNoInteractions(aisAccountTppInfoValidator);
    }

    private AisConsent buildAisConsent(TppInfo tppInfo) {

        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/ais-consent-with-masked-pan.json", AisConsent.class);
        aisConsent.getConsentTppInformation().setTppInfo(tppInfo);
        aisConsent.setConsentData(AisConsentData.buildDefaultAisConsentData());

        return aisConsent;
    }

    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }
}
