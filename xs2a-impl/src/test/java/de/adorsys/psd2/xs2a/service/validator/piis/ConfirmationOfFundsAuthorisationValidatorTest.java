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

package de.adorsys.psd2.xs2a.service.validator.piis;

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
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
