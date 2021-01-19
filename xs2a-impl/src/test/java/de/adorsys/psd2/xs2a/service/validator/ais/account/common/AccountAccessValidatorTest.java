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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_INVALID;
import static org.junit.jupiter.api.Assertions.*;

class AccountAccessValidatorTest {

    private static final MessageError AIS_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(CONSENT_INVALID));

    private AccountAccessValidator accountAccessValidator;

    private JsonReader jsonReader;
    private AisConsent aisConsent;

    @BeforeEach
    void setUp() {
        accountAccessValidator = new AccountAccessValidator();
        jsonReader = new JsonReader();
    }

    @Test
    void testValidate_allAccountConsentWithBalances_shouldReturnValid(){
        aisConsent = jsonReader.getObjectFromFile( "json/service/validator/ais/account/xs2a-account-consent-all-available-accounts-with-balance.json", AisConsent.class);

        ValidationResult actual = accountAccessValidator.validate(aisConsent, true);

        assertTrue(actual.isValid());
    }

    @Test
    void testValidate_allAccountConsentWithBalances_withOwnerName_shouldReturnValid(){
        aisConsent = jsonReader.getObjectFromFile( "json/service/validator/ais/account/xs2a-account-consent-all-available-accounts-with-balance_with_owner_name.json", AisConsent.class);

        ValidationResult actual = accountAccessValidator.validate(aisConsent, true);

        assertTrue(actual.isValid());
    }

    @Test
    void testValidate_withoutBalance_shouldReturnValid() {
        // Given
        aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-consent.json", AisConsent.class);

        // When
        ValidationResult actual = accountAccessValidator.validate(aisConsent, false);

        // Then
        assertTrue(actual.isValid());
    }

    @Test
    void testValidate_withBalanceAndNullBalances_shouldReturnError() {
        // Given
        aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-consent.json", AisConsent.class);
        aisConsent.getAccess().getBalances().clear();

        // When
        ValidationResult actual = accountAccessValidator.validate(aisConsent, true);

        // Then
        assertTrue(actual.isNotValid());
        assertEquals(AIS_VALIDATION_ERROR, actual.getMessageError());
    }

    @Test
    void testValidate_globalConsent_withBalanceAndNullBalances_shouldReturnError() {
        // Given
        aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-consent-global.json", AisConsent.class);
        assertNull(aisConsent.getAccess().getBalances());

        // When
        ValidationResult actual = accountAccessValidator.validate(aisConsent, true);

        // Then
        assertTrue(actual.isValid());
    }

    @Test
    void testValidate_globalConsent_withoutBalanceAndNullBalances_shouldReturnError() {
        // Given
        aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-consent-global.json", AisConsent.class);
        assertNull(aisConsent.getAccess().getBalances());

        // When
        ValidationResult actual = accountAccessValidator.validate(aisConsent, false);

        // Then
        assertTrue(actual.isValid());
    }
}
