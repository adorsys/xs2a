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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

    private final JsonReader jsonReader = new JsonReader();
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

    @ParameterizedTest
    @ValueSource(strings = {"application/xml, application/json", "   application/xml,application/json   ",
        "APPLICATION/XML,APPLICATION/JSON"
    })
    void validate_isAtLeastOneAcceptHeaderSupported(String accept) {
        when(aspspProfileServiceWrapper.getSupportedTransactionApplicationTypes()).thenReturn(aspspSettings.getAis()
                                                                                                  .getTransactionParameters()
                                                                                                  .getSupportedTransactionApplicationTypes());

        ValidationResult actual = validator.validate(accept);
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
