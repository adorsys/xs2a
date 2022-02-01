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
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_INVALID;
import static org.junit.jupiter.api.Assertions.*;

class PermittedAccountReferenceValidatorTest {

    private static final String ACCOUNT_ID = "11111-999999999";
    private static final String WRONG_ACCOUNT_ID = "wrong_account_id";
    private PermittedAccountReferenceValidator validator;
    private JsonReader jsonReader = new JsonReader();
    private AisConsent aisConsent = buildAccountConsent();

    @BeforeEach
    void setUp() {
        validator = new PermittedAccountReferenceValidator(new AccountReferenceAccessValidator());
    }

    @Test
    void validate_withBalanceIsTrue_success() {
        ValidationResult validationResult = validator.validate(aisConsent, ACCOUNT_ID, true);
        assertTrue(validationResult.isValid());
    }

    @Test
    void validate_withBalanceIsFalse_success() {
        ValidationResult validationResult = validator.validate(aisConsent, ACCOUNT_ID, false);
        assertTrue(validationResult.isValid());
    }

    @Test
    void validate_withBalanceIsTrue_notValidByAccountId() {
        ValidationResult validationResult = validator.validate(aisConsent, WRONG_ACCOUNT_ID, true);

        assertFalse(validationResult.isValid());
        assertEquals(ErrorType.AIS_401, validationResult.getMessageError().getErrorType());
        assertEquals(CONSENT_INVALID, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
    }

    @Test
    void validate_withBalanceIsFalse_notValidByAccountId() {
        ValidationResult validationResult = validator.validate(aisConsent, WRONG_ACCOUNT_ID, false);

        assertFalse(validationResult.isValid());
        assertEquals(ErrorType.AIS_401, validationResult.getMessageError().getErrorType());
        assertEquals(CONSENT_INVALID, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
    }

    private AisConsent buildAccountConsent() {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/ais-consent-with-iban.json", AisConsent.class);
        AisConsentData consentData = AisConsentData.buildDefaultAisConsentData();
        aisConsent.setConsentData(consentData);

        return aisConsent;
    }
}
