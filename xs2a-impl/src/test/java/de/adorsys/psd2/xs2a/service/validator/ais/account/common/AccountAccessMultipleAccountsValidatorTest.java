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
