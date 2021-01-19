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
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountAccessMultipleAccountsValidatorTest {
    private static final String DEDICATED_CONSENT_PATH = "json/service/validator/ais/account/xs2a-account-consent.json";
    private static final String GLOBAL_CONSENT_PATH = "json/service/validator/ais/account/xs2a-account-consent-global.json";
    private static final String ALL_AVAILABLE_ACCOUNTS_CONSENT_PATH = "json/service/validator/ais/account/xs2a-account-consent-all-available-accounts.json";
    private static final String BANK_OFFERED_CONSENT_PATH = "json/service/validator/ais/account/xs2a-account-consent-bank-offered.json";
    private static final String DEDICATED_CONSENT_WITH_MULTIPLE_ACCOUNTS_PATH = "json/service/validator/ais/account/xs2a-account-consent-with-multiple-accounts.json";

    private JsonReader jsonReader = new JsonReader();
    private AccountAccessMultipleAccountsValidator validator = new AccountAccessMultipleAccountsValidator();

    @Test
    void DEDICATED_ACCOUNTS_WithBalance_shouldReturnValid() {
        //Given
        AisConsent accountConsent = getAccountConsentFromFileAndUpdateRequestType(DEDICATED_CONSENT_PATH);

        //When
        ValidationResult actual = validator.validate(accountConsent, true);
        //Then
        assertTrue(actual.isValid());
    }

    @Test
    void DEDICATED_ACCOUNTS_WithBalance_shouldReturnError() {
        //Given
        AisConsent accountConsent = getAccountConsentFromFileAndUpdateRequestType(DEDICATED_CONSENT_WITH_MULTIPLE_ACCOUNTS_PATH);

        //When
        ValidationResult actual = validator.validate(accountConsent, true);
        // Then
        assertTrue(actual.isNotValid());
        assertEquals(new MessageError(ErrorType.AIS_401, TppMessageInformation.of(CONSENT_INVALID)), actual.getMessageError());
    }

    @Test
    void DEDICATED_ACCOUNTS_WithoutBalance_shouldReturnValid() {
        //Given
        AisConsent accountConsent = getAccountConsentFromFileAndUpdateRequestType(DEDICATED_CONSENT_WITH_MULTIPLE_ACCOUNTS_PATH);
        //When
        ValidationResult actual = validator.validate(accountConsent, false);
        //Then
        assertTrue(actual.isValid());
    }

    @Test
    void GLOBAL_WithBalance_shouldReturnValid() {
        //Given
        AisConsent accountConsent = getAccountConsentFromFileAndUpdateRequestType(GLOBAL_CONSENT_PATH);
        //When
        ValidationResult actual = validator.validate(accountConsent, true);
        //Then
        assertTrue(actual.isValid());
    }

    @Test
    void GLOBAL_WithoutBalance_shouldReturnValid() {
        //Given
        AisConsent accountConsent = getAccountConsentFromFileAndUpdateRequestType(GLOBAL_CONSENT_PATH);
        //When
        ValidationResult actual = validator.validate(accountConsent, false);
        //Then
        assertTrue(actual.isValid());
    }

    @Test
    void ALL_AVAILABLE_ACCOUNTS_WithBalance_shouldReturnValid() {
        //Given
        AisConsent accountConsent = getAccountConsentFromFileAndUpdateRequestType(ALL_AVAILABLE_ACCOUNTS_CONSENT_PATH);
        //When
        ValidationResult actual = validator.validate(accountConsent, true);
        //Then
        assertTrue(actual.isValid());
    }

    @Test
    void ALL_AVAILABLE_ACCOUNTS_WithoutBalance_shouldReturnValid() {
        //Given
        AisConsent accountConsent = getAccountConsentFromFileAndUpdateRequestType(ALL_AVAILABLE_ACCOUNTS_CONSENT_PATH);
        //When
        ValidationResult actual = validator.validate(accountConsent, false);
        //Then
        assertTrue(actual.isValid());
    }

    @Test
    void BANK_OFFERED_ACCOUNTS_WithBalance_shouldReturnValid() {
        //Given
        AisConsent accountConsent = getAccountConsentFromFileAndUpdateRequestType(BANK_OFFERED_CONSENT_PATH);
        //When
        ValidationResult actual = validator.validate(accountConsent, true);
        //Then
        assertTrue(actual.isValid());
    }

    @Test
    void BANK_OFFERED_ACCOUNTS_WithoutBalance_shouldReturnValid() {
        //Given
        AisConsent accountConsent = getAccountConsentFromFileAndUpdateRequestType(BANK_OFFERED_CONSENT_PATH);
        //When
        ValidationResult actual = validator.validate(accountConsent, false);
        //Then
        assertTrue(actual.isValid());
    }

    private AisConsent getAccountConsentFromFileAndUpdateRequestType(String file) {
        return jsonReader.getObjectFromFile(file, AisConsent.class);
    }
}
