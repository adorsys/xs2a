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

import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
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
    private Xs2aAccountAccess accountAccess;
    private AccountReference accountReference;

    @BeforeEach
    void setUp() {
        validator = new AccountReferenceAccessValidator();
        jsonReader = new JsonReader();

        accountAccess = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-access.json", Xs2aAccountAccess.class);
        accountReference = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-reference.json", AccountReference.class);
    }

    @Test
    void validate_success() {
        accountAccess = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-access.json", Xs2aAccountAccess.class);
        ValidationResult validationResult = validator.validate(accountAccess, Collections.singletonList(accountReference), ACCOUNT_ID);
        assertTrue(validationResult.isValid());
    }

    @Test
    void validate_notValidByAccountId() {
        accountAccess = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-access.json", Xs2aAccountAccess.class);
        ValidationResult validationResult = validator.validate(accountAccess, Collections.singletonList(accountReference), WRONG_ACCOUNT_ID);

        assertFalse(validationResult.isValid());
        assertEquals(ErrorType.AIS_401, validationResult.getMessageError().getErrorType());
        assertEquals(CONSENT_INVALID, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
    }

    @Test
    void validate_notValidAvailableAccount() {
        accountAccess = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-access-available_accounts.json", Xs2aAccountAccess.class);
        ValidationResult validationResult = validator.validate(accountAccess, Collections.emptyList(), ACCOUNT_ID);

        assertFalse(validationResult.isValid());
        assertEquals(ErrorType.AIS_401, validationResult.getMessageError().getErrorType());
        assertEquals(CONSENT_INVALID, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
    }

    @Test
    void validate_notValidAvailableAccountWithValances() {
        accountAccess = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-access-available_accounts_with_balances.json", Xs2aAccountAccess.class);
        ValidationResult validationResult = validator.validate(accountAccess, Collections.emptyList(), ACCOUNT_ID);

        assertFalse(validationResult.isValid());
        assertEquals(ErrorType.AIS_401, validationResult.getMessageError().getErrorType());
        assertEquals(CONSENT_INVALID, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
    }

    @Test
    void validate_AccountReferenceWithoutResourceId() {
        AccountReference accountReference = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-reference-without-resource-id.json", AccountReference.class);
        ValidationResult validationResult = validator.validate(this.accountAccess, Collections.singletonList(accountReference), ACCOUNT_ID);

        assertTrue(validationResult.isNotValid());
        assertEquals(ErrorType.AIS_401, validationResult.getMessageError().getErrorType());
        assertEquals(CONSENT_INVALID, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
    }
}
