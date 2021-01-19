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

package de.adorsys.psd2.xs2a.service.validator.ais.account.common;

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_INVALID;
import static org.junit.jupiter.api.Assertions.*;

class AccountReferenceAccessValidatorTest {

    private static final String ACCOUNT_ID = "11111-999999999";
    private static final String WRONG_ACCOUNT_ID = "wrong_account_id";

    private AccountReferenceAccessValidator validator;
    private JsonReader jsonReader;

    private AisConsent aisConsent;
    private AccountReference accountReference;

    @BeforeEach
    void setUp() {
        validator = new AccountReferenceAccessValidator();
        jsonReader = new JsonReader();

        aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-consent.json", AisConsent.class);
        accountReference = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-reference.json", AccountReference.class);
    }

    @Test
    void validate_success() {
        ValidationResult validationResult = validator.validate(aisConsent, Collections.singletonList(accountReference), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS);
        assertTrue(validationResult.isValid());
    }

    @Test
    void validate_success_global() {
        ValidationResult validationResult = validator.validate(aisConsent, Collections.singletonList(accountReference), ACCOUNT_ID, AisConsentRequestType.GLOBAL);
        assertTrue(validationResult.isValid());
    }

    @Test
    void validate_notValidByAccountId() {
        ValidationResult validationResult = validator.validate(aisConsent, Collections.singletonList(accountReference), WRONG_ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS);

        assertFalse(validationResult.isValid());
        assertEquals(ErrorType.AIS_401, validationResult.getMessageError().getErrorType());
        assertEquals(CONSENT_INVALID, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
    }

    @Test
    void validate_notValidAvailableAccount() {
        aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-consent-all-available-accounts.json", AisConsent.class);
        ValidationResult validationResult = validator.validate(aisConsent, Collections.emptyList(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS);

        assertFalse(validationResult.isValid());
        assertEquals(ErrorType.AIS_401, validationResult.getMessageError().getErrorType());
        assertEquals(CONSENT_INVALID, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
    }

    @Test
    void validate_notValidAvailableAccountWithValances() {
        aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-consent-all-available-accounts-with-balance.json", AisConsent.class);
        ValidationResult validationResult = validator.validate(aisConsent, Collections.emptyList(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS);

        assertFalse(validationResult.isValid());
        assertEquals(ErrorType.AIS_401, validationResult.getMessageError().getErrorType());
        assertEquals(CONSENT_INVALID, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
    }

    @Test
    void validate_AccountReferenceWithoutResourceId() {
        AccountReference accountReference = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-reference-without-resource-id.json", AccountReference.class);
        ValidationResult validationResult = validator.validate(aisConsent, Collections.singletonList(accountReference), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS);

        assertTrue(validationResult.isNotValid());
        assertEquals(ErrorType.AIS_401, validationResult.getMessageError().getErrorType());
        assertEquals(CONSENT_INVALID, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
    }
}
