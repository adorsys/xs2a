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

package de.adorsys.psd2.xs2a.service.validator.ais.account;

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.validator.OauthConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountAccessMultipleAccountsValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountAccessValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.GetAccountListConsentObject;
import de.adorsys.psd2.xs2a.service.validator.tpp.AisAccountTppInfoValidator;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_INVALID;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAccountListValidatorTest {
    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
    private static final String REQUEST_URI = "/accounts";

    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("invalid authorisation number");

    private static final MessageError TPP_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(UNAUTHORIZED));
    private static final MessageError ACCESS_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(CONSENT_INVALID));

    @Mock
    private AccountConsentValidator accountConsentValidator;
    @Mock
    private AisAccountTppInfoValidator aisAccountTppInfoValidator;
    @Mock
    private AccountAccessValidator accountAccessValidator;
    @Mock
    private AccountAccessMultipleAccountsValidator accountAccessMultipleAccountsValidator;
    @Mock
    private OauthConsentValidator oauthConsentValidator;

    @InjectMocks
    private GetAccountListValidator getAccountListValidator;

    private JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        // Inject pisTppInfoValidator via setter
        getAccountListValidator.setAisAccountTppInfoValidator(aisAccountTppInfoValidator);
    }

    @Test
    void validate_withValidConsentObject_shouldReturnValid() {
        // Given
        AccountAccess accountAccess = buildXs2aAccountAccess();
        AisConsent aisConsent = buildAccountConsent(accountAccess, TPP_INFO);

        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(accountConsentValidator.validate(aisConsent, REQUEST_URI))
            .thenReturn(ValidationResult.valid());
        when(accountAccessValidator.validate(aisConsent, aisConsent.isWithBalance()))
            .thenReturn(ValidationResult.valid());
        when(accountAccessMultipleAccountsValidator.validate(aisConsent, aisConsent.isWithBalance()))
            .thenReturn(ValidationResult.valid());
        when(oauthConsentValidator.validate(aisConsent))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = getAccountListValidator.validate(new GetAccountListConsentObject(aisConsent, false, REQUEST_URI));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withBalanceRequestAndValidAccess_shouldReturnValid() {
        // Given
        AccountAccess accountAccess = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-access-without-balance.json", AccountAccess.class);
        AisConsent aisConsent = buildAccountConsent(accountAccess, TPP_INFO);

        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(accountConsentValidator.validate(aisConsent, REQUEST_URI))
            .thenReturn(ValidationResult.valid());
        when(accountAccessValidator.validate(any(), anyBoolean()))
            .thenReturn(ValidationResult.valid());
        when(accountAccessMultipleAccountsValidator.validate(aisConsent, true))
            .thenReturn(ValidationResult.valid());
        when(oauthConsentValidator.validate(aisConsent))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = getAccountListValidator.validate(new GetAccountListConsentObject(aisConsent, true, REQUEST_URI));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidTppInConsent_shouldReturnTppValidationError() {
        // Given
        AccountAccess accountAccess = buildXs2aAccountAccess();
        AisConsent aisConsent = buildAccountConsent(accountAccess, INVALID_TPP_INFO);

        when(aisAccountTppInfoValidator.validateTpp(INVALID_TPP_INFO))
            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = getAccountListValidator.validate(new GetAccountListConsentObject(aisConsent, false, REQUEST_URI));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withBalanceRequestAndNoBalanceAccessInConsent_shouldReturnAccessValidationError() {
        // Given
        AccountAccess accountAccess = buildXs2aAccountAccess();
        AisConsent aisConsent = buildAccountConsent(accountAccess, TPP_INFO);

        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(accountConsentValidator.validate(aisConsent, REQUEST_URI))
            .thenReturn(ValidationResult.valid());
        when(accountAccessValidator.validate(any(), anyBoolean()))
            .thenReturn(ValidationResult.invalid(ACCESS_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = getAccountListValidator.validate(new GetAccountListConsentObject(aisConsent, true, REQUEST_URI));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(ACCESS_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withBalanceRequestAndInvalidAccess_shouldReturnAccessValidationError() {
        // Given
        AccountAccess accountAccess = buildXs2aAccountAccess();
        AisConsent aisConsent = buildAccountConsent(accountAccess, TPP_INFO);

        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(accountConsentValidator.validate(aisConsent, REQUEST_URI))
            .thenReturn(ValidationResult.valid());
        when(accountAccessValidator.validate(aisConsent, true))
            .thenReturn(ValidationResult.invalid(ACCESS_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = getAccountListValidator.validate(new GetAccountListConsentObject(aisConsent, true, REQUEST_URI));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(ACCESS_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidTppInConsentAndInvalidAccess_shouldReturnTppValidationErrorFirst() {
        // Given
        AccountAccess accountAccess = buildXs2aAccountAccess();
        AisConsent aisConsent = buildAccountConsent(accountAccess, INVALID_TPP_INFO);

        when(aisAccountTppInfoValidator.validateTpp(INVALID_TPP_INFO))
            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = getAccountListValidator.validate(new GetAccountListConsentObject(aisConsent, true, REQUEST_URI));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withNoIbanInConsent_shouldReturnValidationError() {
        // Given
        AccountAccess accountAccess = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-access-without-iban.json", AccountAccess.class);
        AisConsent aisConsent = buildAccountConsent(accountAccess, TPP_INFO);

        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = getAccountListValidator.validate(new GetAccountListConsentObject(aisConsent, true, REQUEST_URI));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(ACCESS_VALIDATION_ERROR, validationResult.getMessageError());
    }

    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }

    private AisConsent buildAccountConsent(AccountAccess accountAccess, TppInfo tppInfo) {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/ais-consent.json", AisConsent.class);
        aisConsent.getConsentTppInformation().setTppInfo(tppInfo);
        aisConsent.setConsentData(AisConsentData.buildDefaultAisConsentData());
        aisConsent.setTppAccountAccesses(accountAccess);
        return aisConsent;
    }

    private AccountAccess buildXs2aAccountAccess() {
        return jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-access-without-balance.json", AccountAccess.class);
    }

}
