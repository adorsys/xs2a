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

import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.service.validator.OauthConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountReferenceAccessValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.GetCardAccountDetailsRequestObject;
import de.adorsys.psd2.xs2a.service.validator.tpp.AisAccountTppInfoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Collections;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_401;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCardAccountDetailsValidatorTest {
    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("invalid authorisation number");
    private static final String ACCOUNT_ID = "account id";
    private static final String REQUEST_URI = "/accounts";

    private static final MessageError TPP_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(UNAUTHORIZED));

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
    @Mock
    private AccountReference accountReference;

    @BeforeEach
    void setUp() {
        // Inject pisTppInfoValidator via setter
        getCardAccountDetailsValidator.setAisAccountTppInfoValidator(aisAccountTppInfoValidator);
    }

    @Test
    void validate_withValidConsentObject_shouldReturnValid() {
        // Given
        AccountConsent accountConsent = buildAccountConsent(TPP_INFO);
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());
        when(accountReferenceAccessValidator.validate(accountConsent.getAccess(), accountConsent.getAccess().getAccounts(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS)).thenReturn(ValidationResult.valid());
        when(accountConsentValidator.validate(accountConsent, REQUEST_URI)).thenReturn(ValidationResult.valid());
        when(oauthConsentValidator.validate(accountConsent)).thenReturn(ValidationResult.valid());

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
        AccountConsent accountConsent = buildAccountConsent(INVALID_TPP_INFO);
        when(aisAccountTppInfoValidator.validateTpp(INVALID_TPP_INFO)).thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

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
        AccountConsent accountConsent = buildAccountConsent(TPP_INFO);
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());
        when(accountReferenceAccessValidator.validate(accountConsent.getAccess(), accountConsent.getAccess().getAccounts(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS)).thenReturn(ValidationResult.valid());
        when(oauthConsentValidator.validate(accountConsent)).thenReturn(ValidationResult.valid());
        ValidationResult validationResultExpected = ValidationResult.invalid(AIS_401, CONSENT_EXPIRED);
        when(accountConsentValidator.validate(accountConsent, REQUEST_URI)).thenReturn(validationResultExpected);

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
        AccountConsent accountConsent = buildAccountConsent(TPP_INFO);
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());
        ValidationResult validationResultExpected = ValidationResult.invalid(AIS_401, CONSENT_INVALID);
        when(accountReferenceAccessValidator.validate(accountConsent.getAccess(), accountConsent.getAccess().getAccounts(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS)).thenReturn(validationResultExpected);

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
        AccountConsent accountConsent = buildAccountConsent(TPP_INFO);
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());
        when(accountReferenceAccessValidator.validate(accountConsent.getAccess(), accountConsent.getAccess().getAccounts(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS)).thenReturn(ValidationResult.valid());
        ValidationResult validationResultExpected = ValidationResult.invalid(AIS_401, MessageErrorCode.FORBIDDEN);
        when(oauthConsentValidator.validate(accountConsent)).thenReturn(validationResultExpected);

        // When
        ValidationResult validationResult = getCardAccountDetailsValidator.validate(new GetCardAccountDetailsRequestObject(accountConsent, ACCOUNT_ID, REQUEST_URI));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(validationResultExpected.getMessageError(), validationResult.getMessageError());
    }

    private AccountConsent buildAccountConsent(TppInfo tppInfo) {
        return new AccountConsent("id", buildXs2aAccountAccess(), buildXs2aAccountAccess(), false, null, null, 0,
                                  null, null, false, false,
                                  Collections.emptyList(), tppInfo, AisConsentRequestType.DEDICATED_ACCOUNTS, false,
                                  Collections.emptyList(), null, Collections.emptyMap(), OffsetDateTime.now());
    }

    private Xs2aAccountAccess buildXs2aAccountAccess() {
        return new Xs2aAccountAccess(Collections.singletonList(accountReference), Collections.emptyList(), Collections.emptyList(), null, null, null, null);
    }

    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }
}
