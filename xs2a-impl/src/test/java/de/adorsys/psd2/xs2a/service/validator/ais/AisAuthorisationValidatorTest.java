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

package de.adorsys.psd2.xs2a.service.validator.ais;

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.AisAuthorisationValidator;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.RESOURCE_UNKNOWN_403;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class AisAuthorisationValidatorTest {
    private static final String AUTHORISATION_ID = "a8fc1f02-3639-4528-bd19-3eacf1c67038";
    private static final String WRONG_AUTHORISATION_ID = "234ef79c-d785-41ec-9b14-2ea3a7ae2ce0";

    @InjectMocks
    private AisAuthorisationValidator validator;

    private JsonReader jsonReader = new JsonReader();
    private AisConsent accountConsent;

    @BeforeEach
    void setUp() {
        accountConsent = jsonReader.getObjectFromFile("json/service/ais-consent.json", AisConsent.class);
    }

    @Test
    void validate_success() {
        // When
        ValidationResult validationResult = validator.validate(AUTHORISATION_ID, accountConsent);

        // Then
        assertTrue(validationResult.isValid());
    }

    @Test
    void validate_wrong_authorisationId() {
        // When
        ValidationResult validationResult = validator.validate(WRONG_AUTHORISATION_ID, accountConsent);

        // Then
        assertTrue(validationResult.isNotValid());
        assertEquals(ErrorType.AIS_403, validationResult.getMessageError().getErrorType());
        assertEquals(RESOURCE_UNKNOWN_403, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
    }
}
