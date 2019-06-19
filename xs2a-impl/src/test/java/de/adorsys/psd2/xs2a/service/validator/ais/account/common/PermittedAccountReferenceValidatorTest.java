/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.CONSENT_INVALID;
import static org.junit.Assert.*;

public class PermittedAccountReferenceValidatorTest {
    private static final String ACCOUNT_ID = "11111-999999999";
    private static final String WRONG_ACCOUNT_ID = "wrong_account_id";

    private PermittedAccountReferenceValidator validator;
    private JsonReader jsonReader;
    private AccountConsent accountConsent;

    @Before
    public void setUp() {
        validator = new PermittedAccountReferenceValidator(new AccountReferenceAccessValidator());
        jsonReader = new JsonReader();
    }

    @Test
    public void validate_withBalanceIsTrue_success() {
        accountConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-consent.json", AccountConsent.class);
        ValidationResult validationResult = validator.validate(accountConsent, ACCOUNT_ID, true);
        assertTrue(validationResult.isValid());
    }

    @Test
    public void validate_withBalanceIsFalse_success() {
        accountConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-consent.json", AccountConsent.class);
        ValidationResult validationResult = validator.validate(accountConsent, ACCOUNT_ID, false);
        assertTrue(validationResult.isValid());
    }

    @Test
    public void validate_withBalanceIsTrue_wrongAccountId_globalConsent_success() {
        accountConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-consent-global.json", AccountConsent.class);
        ValidationResult validationResult = validator.validate(accountConsent, WRONG_ACCOUNT_ID, true);
        assertTrue(validationResult.isValid());
    }

    @Test
    public void validate_withBalanceIsFalse_wrongAccountId_globalConsent_success() {
        accountConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-consent-global.json", AccountConsent.class);
        ValidationResult validationResult = validator.validate(accountConsent, WRONG_ACCOUNT_ID, false);
        assertTrue(validationResult.isValid());
    }

    @Test
    public void validate_withBalanceIsTrue_notValidByAccountId() {
        accountConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-consent.json", AccountConsent.class);
        ValidationResult validationResult = validator.validate(accountConsent, WRONG_ACCOUNT_ID, true);

        assertFalse(validationResult.isValid());
        assertEquals(ErrorType.AIS_401, validationResult.getMessageError().getErrorType());
        assertEquals(CONSENT_INVALID, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
    }

    @Test
    public void validate_withBalanceIsFalse_notValidByAccountId() {
        accountConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-consent.json", AccountConsent.class);
        ValidationResult validationResult = validator.validate(accountConsent, WRONG_ACCOUNT_ID, false);

        assertFalse(validationResult.isValid());
        assertEquals(ErrorType.AIS_401, validationResult.getMessageError().getErrorType());
        assertEquals(CONSENT_INVALID, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
    }
}
