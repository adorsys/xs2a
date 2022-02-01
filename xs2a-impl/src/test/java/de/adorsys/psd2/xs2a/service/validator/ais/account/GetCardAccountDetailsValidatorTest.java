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

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.validator.OauthConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountReferenceAccessValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.GetCardAccountDetailsRequestObject;
import de.adorsys.psd2.xs2a.service.validator.tpp.AisAccountTppInfoValidator;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Set;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_401;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCardAccountDetailsValidatorTest {
    private static final TppInfo TPP_INFO = buildTppInfo("PSDDE-FAKENCA-87B2AC");
    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("000RU-FAKENCA-87B2AC");
    private static final String ACCOUNT_ID = "11111-999999999";
    private static final String REQUEST_URI = "/accounts";

    private static final MessageError TPP_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(UNAUTHORIZED));

    private static final MessageError CONSENT_INVALID_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(CONSENT_INVALID));

    @InjectMocks
    private GetCardAccountDetailsValidator getCardAccountDetailsValidator;

    @Mock
    private AccountConsentValidator accountConsentValidator;
    @Mock
    private AisAccountTppInfoValidator aisAccountTppInfoValidator;
    @Mock
    private AccountReferenceAccessValidator accountReferenceAccessValidator;
    @Mock
    private OauthConsentValidator oauthConsentValidator;

    private final JsonReader jsonReader = new JsonReader();
    private AisConsent aisConsent;

    @BeforeEach
    void setUp() {
        AccountAccess accountAccess = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-access.json", AccountAccess.class);
        aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/ais-consent.json", AisConsent.class);
        aisConsent.setConsentData(AisConsentData.buildDefaultAisConsentData());
        aisConsent.setTppAccountAccesses(accountAccess);
        // Inject pisTppInfoValidator via setter
        getCardAccountDetailsValidator.setAisAccountTppInfoValidator(aisAccountTppInfoValidator);
    }

    @Test
    void validate_withValidConsentObject_shouldReturnValid() {
        // Given
        AisConsent accountConsent = getCardAccountConsent(TPP_INFO);

        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(accountReferenceAccessValidator.validate(accountConsent, accountConsent.getAccess().getAccounts(), ACCOUNT_ID, AisConsentRequestType.GLOBAL))
            .thenReturn(ValidationResult.valid());
        when(accountConsentValidator.validate(accountConsent, REQUEST_URI))
            .thenReturn(ValidationResult.valid());
        when(oauthConsentValidator.validate(accountConsent))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = getCardAccountDetailsValidator.validate(new GetCardAccountDetailsRequestObject(accountConsent, ACCOUNT_ID, REQUEST_URI));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(accountConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidTppInConsent_shouldReturnTppValidationError() {
        // Given
        AisConsent accountConsent = getCardAccountConsent(INVALID_TPP_INFO);
        when(aisAccountTppInfoValidator.validateTpp(INVALID_TPP_INFO))
            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = getCardAccountDetailsValidator.validate(new GetCardAccountDetailsRequestObject(accountConsent, ACCOUNT_ID, REQUEST_URI));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(accountConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidConsent_shouldReturnInvalid() {
        // Given
        AisConsent accountConsent = getCardAccountConsent(TPP_INFO);
        accountConsent.setValidUntil(LocalDate.now().minusDays(1));

        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(accountReferenceAccessValidator.validate(accountConsent, accountConsent.getAccess().getAccounts(), ACCOUNT_ID, AisConsentRequestType.GLOBAL))
            .thenReturn(ValidationResult.valid());
        when(oauthConsentValidator.validate(accountConsent))
            .thenReturn(ValidationResult.valid());
        ValidationResult validationResultExpected = ValidationResult.invalid(AIS_401, CONSENT_EXPIRED);
        when(accountConsentValidator.validate(accountConsent, REQUEST_URI))
            .thenReturn(validationResultExpected);

        // When
        ValidationResult validationResult = getCardAccountDetailsValidator.validate(new GetCardAccountDetailsRequestObject(accountConsent, ACCOUNT_ID, REQUEST_URI));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(validationResultExpected.getMessageError(), validationResult.getMessageError());
    }

    @Test
    void validate_withAccountReferenceAccessInvalid_shouldReturnInvalid() {
        // Given
        AisConsent accountConsent = getCardAccountConsent(TPP_INFO);
        accountConsent.setConsentData(AisConsentData.buildDefaultAisConsentData());

        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        ValidationResult validationResultExpected = ValidationResult.invalid(AIS_401, CONSENT_INVALID);

        // When
        ValidationResult validationResult = getCardAccountDetailsValidator.validate(new GetCardAccountDetailsRequestObject(accountConsent, ACCOUNT_ID, REQUEST_URI));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(validationResultExpected.getMessageError(), validationResult.getMessageError());
    }

    @Test
    void validate_withOauthConsentInvalid_shouldReturnInvalid() {
        // Given
        AisConsent accountConsent = getCardAccountConsent(TPP_INFO);
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(accountReferenceAccessValidator.validate(accountConsent, accountConsent.getAccess().getAccounts(), ACCOUNT_ID, AisConsentRequestType.GLOBAL))
            .thenReturn(ValidationResult.valid());
        ValidationResult validationResultExpected = ValidationResult.invalid(AIS_401, MessageErrorCode.FORBIDDEN);
        when(oauthConsentValidator.validate(accountConsent))
            .thenReturn(validationResultExpected);

        // When
        ValidationResult validationResult = getCardAccountDetailsValidator.validate(new GetCardAccountDetailsRequestObject(accountConsent, ACCOUNT_ID, REQUEST_URI));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(validationResultExpected.getMessageError(), validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidAccountInConsent_shouldReturnConsentInvalidError() {
        // Given
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = getCardAccountDetailsValidator.validate(new GetCardAccountDetailsRequestObject(aisConsent, ACCOUNT_ID, REQUEST_URI));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(CONSENT_INVALID_ERROR, validationResult.getMessageError());
    }

    @Test
    void buildWarningMessages() {
        //Given
        GetCardAccountDetailsRequestObject getCardAccountDetailsRequestObject =
            new GetCardAccountDetailsRequestObject(aisConsent, ACCOUNT_ID, REQUEST_URI);

        //When
        Set<TppMessageInformation> actual = getCardAccountDetailsValidator.buildWarningMessages(getCardAccountDetailsRequestObject);

        //Then
        assertThat(actual).isEmpty();
        verifyNoInteractions(accountConsentValidator);
        verifyNoInteractions(aisAccountTppInfoValidator);
        verifyNoInteractions(accountReferenceAccessValidator);
        verifyNoInteractions(oauthConsentValidator);
    }

    private AisConsent getCardAccountConsent(TppInfo tppInfo) {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/ais-consent-all-accounts.json", AisConsent.class);
        aisConsent.getConsentTppInformation().setTppInfo(tppInfo);

        return aisConsent;
    }

    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }
}
