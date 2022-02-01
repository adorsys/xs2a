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

package de.adorsys.psd2.xs2a.service.validator.piis;

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.RESOURCE_UNKNOWN_403;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfirmationOfFundsAuthorisationValidatorTest {
    private static final String AUTHORISATION_ID = "a8fc1f02-3639-4528-bd19-3eacf1c67038";
    private static final String WRONG_AUTHORISATION_ID = "234ef79c-d785-41ec-9b14-2ea3a7ae2ce0";

    private ConfirmationOfFundsAuthorisationValidator validator = new ConfirmationOfFundsAuthorisationValidator();

    private JsonReader jsonReader = new JsonReader();
    private PiisConsent piisConsent;

    @BeforeEach
    void setUp() {
        piisConsent = jsonReader.getObjectFromFile("json/service/piis-consent.json", PiisConsent.class);
    }

    @Test
    void validate_success() {
        // When
        ValidationResult validationResult = validator.validate(AUTHORISATION_ID, piisConsent);

        // Then
        assertTrue(validationResult.isValid());
    }

    @Test
    void validate_wrong_authorisationId() {
        // When
        ValidationResult validationResult = validator.validate(WRONG_AUTHORISATION_ID, piisConsent);

        // Then
        assertTrue(validationResult.isNotValid());
        assertEquals(ErrorType.PIIS_403, validationResult.getMessageError().getErrorType());
        assertEquals(RESOURCE_UNKNOWN_403, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
    }
}
