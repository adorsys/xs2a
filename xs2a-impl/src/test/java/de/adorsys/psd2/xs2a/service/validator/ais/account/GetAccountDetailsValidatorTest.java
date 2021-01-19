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

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.validator.OauthConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountAccessValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountReferenceAccessValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.PermittedAccountReferenceValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.CommonAccountRequestObject;
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
class GetAccountDetailsValidatorTest {
    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("invalid authorisation number");
    private static final String ACCOUNT_ID = "account id";
    private static final String REQUEST_URI = "/accounts";
    private static final boolean WITH_BALANCE = false;

    private static final MessageError TPP_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(UNAUTHORIZED));

    private static final MessageError PERMITTED_ACCOUNT_REFERENCE_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(CONSENT_INVALID));

    private static final MessageError CONSENT_INVALID_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(CONSENT_INVALID));

    @InjectMocks
    private GetAccountDetailsValidator getAccountDetailsValidator;

    @Mock
    private AccountConsentValidator accountConsentValidator;
    @Mock
    private AisAccountTppInfoValidator aisAccountTppInfoValidator;
    @Mock
    private AccountReferenceAccessValidator accountReferenceAccessValidator;
    @Mock
    private PermittedAccountReferenceValidator permittedAccountReferenceValidator;
    @Mock
    private AccountAccessValidator accountAccessValidator;
    @Mock
    private OauthConsentValidator oauthConsentValidator;

    private JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        // Inject pisTppInfoValidator via setter
        getAccountDetailsValidator.setAisAccountTppInfoValidator(aisAccountTppInfoValidator);
    }

    @Test
    void validate_withInvalidAccountReference_shouldReturnInvalid() {
        // Given
        AisConsent aisConsent = buildAccountConsent(TPP_INFO);

        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(accountReferenceAccessValidator.validate(aisConsent, aisConsent.getAccess().getAccounts(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS))
            .thenReturn(ValidationResult.valid());
        when(permittedAccountReferenceValidator.validate(aisConsent, ACCOUNT_ID, WITH_BALANCE))
            .thenReturn(ValidationResult.invalid(PERMITTED_ACCOUNT_REFERENCE_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = getAccountDetailsValidator.validate(new CommonAccountRequestObject(aisConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI));

        // Then
        verify(permittedAccountReferenceValidator).validate(aisConsent, ACCOUNT_ID, WITH_BALANCE);

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(PERMITTED_ACCOUNT_REFERENCE_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withValidConsentObject_shouldReturnValid() {
        // Given
        AisConsent aisConsent = buildAccountConsent(TPP_INFO);

        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(accountReferenceAccessValidator.validate(aisConsent, aisConsent.getAccess().getAccounts(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS))
            .thenReturn(ValidationResult.valid());
        when(permittedAccountReferenceValidator.validate(aisConsent, ACCOUNT_ID, aisConsent.isWithBalance()))
            .thenReturn(ValidationResult.valid());
        when(accountAccessValidator.validate(aisConsent, aisConsent.isWithBalance()))
            .thenReturn(ValidationResult.valid());
        when(accountConsentValidator.validate(aisConsent, REQUEST_URI))
            .thenReturn(ValidationResult.valid());
        when(oauthConsentValidator.validate(aisConsent))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = getAccountDetailsValidator.validate(new CommonAccountRequestObject(aisConsent, ACCOUNT_ID, aisConsent.isWithBalance(), REQUEST_URI));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidAccountReferenceAccess_error() {
        // Given
        AisConsent aisConsent = buildAccountConsent(TPP_INFO);

        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(accountReferenceAccessValidator.validate(aisConsent, aisConsent.getAccess().getAccounts(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS))
            .thenReturn(ValidationResult.invalid(ErrorType.AIS_401, CONSENT_INVALID));

        // When
        ValidationResult validationResult = getAccountDetailsValidator.validate(new CommonAccountRequestObject(aisConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertFalse(validationResult.isValid());

        verify(permittedAccountReferenceValidator, never()).validate(any(AisConsent.class), anyString(), anyBoolean());
        verify(accountAccessValidator, never()).validate(any(AisConsent.class), anyBoolean());
        verify(accountConsentValidator, never()).validate(any(AisConsent.class), anyString());
    }

    @Test
    void validate_withInvalidTppInConsent_shouldReturnTppValidationError() {
        // Given
        AisConsent aisConsent = buildAccountConsent(INVALID_TPP_INFO);

        when(aisAccountTppInfoValidator.validateTpp(INVALID_TPP_INFO))
            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = getAccountDetailsValidator.validate(new CommonAccountRequestObject(aisConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidAccountInConsent_shouldReturnConsentInvalidError() {
        // Given
        AisConsent aisConsent = buildCardAccountConsent();

        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = getAccountDetailsValidator.validate(new CommonAccountRequestObject(aisConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(CONSENT_INVALID_ERROR, validationResult.getMessageError());
    }

    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }

    private AisConsent buildAccountConsent(TppInfo tppInfo) {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/ais-consent-with-iban.json", AisConsent.class);
        aisConsent.getConsentTppInformation().setTppInfo(tppInfo);
        aisConsent.setConsentData(AisConsentData.buildDefaultAisConsentData());
        return aisConsent;
    }

    private AisConsent buildCardAccountConsent() {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/ais-consent-with-masked-pan.json", AisConsent.class);
        aisConsent.getConsentTppInformation().setTppInfo(TPP_INFO);
        aisConsent.setConsentData(AisConsentData.buildDefaultAisConsentData());
        return aisConsent;
    }
}
