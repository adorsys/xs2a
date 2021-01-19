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

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.REQUESTED_FORMATS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionReportAcceptHeaderValidatorTest {

    private static final MessageError REQUESTED_FORMATS_INVALID_ERROR =
        new MessageError(ErrorType.AIS_406, TppMessageInformation.of(REQUESTED_FORMATS_INVALID));

    @InjectMocks
    private TransactionReportAcceptHeaderValidator validator;
    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;

    private JsonReader jsonReader = new JsonReader();
    private AspspSettings aspspSettings;

    @BeforeEach
    void setUp() {
        aspspSettings = jsonReader.getObjectFromFile("json/aspsp-settings.json", AspspSettings.class);
    }

    @Test
    void validate_success() {
        when(aspspProfileServiceWrapper.getSupportedTransactionApplicationTypes()).thenReturn(aspspSettings.getAis()
                                                                                                  .getTransactionParameters()
                                                                                                  .getSupportedTransactionApplicationTypes());

        ValidationResult actual = validator.validate(MediaType.APPLICATION_JSON_VALUE);
        assertTrue(actual.isValid());
    }

    @Test
    void validate_error() {
        when(aspspProfileServiceWrapper.getSupportedTransactionApplicationTypes()).thenReturn(aspspSettings.getAis()
                                                                                                  .getTransactionParameters()
                                                                                                  .getSupportedTransactionApplicationTypes());

        ValidationResult actual = validator.validate(MediaType.APPLICATION_PDF_VALUE);
        assertTrue(actual.isNotValid());
        assertEquals(REQUESTED_FORMATS_INVALID_ERROR, actual.getMessageError());
    }

    @Test
    void validate_isAtLeastOneAcceptHeaderSupported() {
        when(aspspProfileServiceWrapper.getSupportedTransactionApplicationTypes()).thenReturn(aspspSettings.getAis()
                                                                                                  .getTransactionParameters()
                                                                                                  .getSupportedTransactionApplicationTypes());

        ValidationResult actual = validator.validate("application/xml, application/json");
        assertTrue(actual.isValid());
    }

    @Test
    void validate_isAtLeastOneAcceptHeaderSupported_extraBlanks() {
        when(aspspProfileServiceWrapper.getSupportedTransactionApplicationTypes()).thenReturn(aspspSettings.getAis()
                                                                                                  .getTransactionParameters()
                                                                                                  .getSupportedTransactionApplicationTypes());

        ValidationResult actual = validator.validate("   application/xml,application/json   ");
        assertTrue(actual.isValid());
    }

    @Test
    void validate_isAtLeastOneAcceptHeaderSupported_ignoreCaseSensitive() {
        when(aspspProfileServiceWrapper.getSupportedTransactionApplicationTypes()).thenReturn(aspspSettings.getAis()
                                                                                                  .getTransactionParameters()
                                                                                                  .getSupportedTransactionApplicationTypes());

        ValidationResult actual = validator.validate("APPLICATION/XML,APPLICATION/JSON");
        assertTrue(actual.isValid());
    }

    @Test
    void validate_acceptHeaderIsNotPresented_success() {
        ValidationResult actual = validator.validate(null);
        assertTrue(actual.isValid());

        actual = validator.validate("");
        assertTrue(actual.isValid());
    }
}
