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

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_INVALID;
import static org.junit.Assert.*;

public class AccountAccessValidatorTest {

    private static final MessageError AIS_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(CONSENT_INVALID));

    private AccountAccessValidator accountAccessValidator;

    private JsonReader jsonReader;
    private AccountConsent accountConsent;

    @Before
    public void setUp() {
        accountAccessValidator = new AccountAccessValidator();
        jsonReader = new JsonReader();
    }

    @Test
    public void testValidate_withoutBalance_shouldReturnValid() {
        // Given
        accountConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-consent.json", AccountConsent.class);

        // When
        ValidationResult actual = accountAccessValidator.validate(accountConsent, false);

        // Then
        assertTrue(actual.isValid());
    }

    @Test
    public void testValidate_withBalanceAndNullBalances_shouldReturnError() {
        // Given
        accountConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-consent.json", AccountConsent.class);
        accountConsent.getAccess().getBalances().clear();

        // When
        ValidationResult actual = accountAccessValidator.validate(accountConsent, true);

        // Then
        assertTrue(actual.isNotValid());
        assertEquals(AIS_VALIDATION_ERROR, actual.getMessageError());
    }

    @Test
    public void testValidate_globalConsent_withBalanceAndNullBalances_shouldReturnError() {
        // Given
        accountConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-consent-global.json", AccountConsent.class);
        assertNull(accountConsent.getAccess().getBalances());

        // When
        ValidationResult actual = accountAccessValidator.validate(accountConsent, true);

        // Then
        assertTrue(actual.isValid());
    }

    @Test
    public void testValidate_globalConsent_withoutBalanceAndNullBalances_shouldReturnError() {
        // Given
        accountConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-consent-global.json", AccountConsent.class);
        assertNull(accountConsent.getAccess().getBalances());

        // When
        ValidationResult actual = accountAccessValidator.validate(accountConsent, false);

        // Then
        assertTrue(actual.isValid());
    }
}
