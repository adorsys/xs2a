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

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.REQUESTED_FORMATS_INVALID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransactionReportAcceptHeaderValidatorTest {

    private static final MessageError REQUESTED_FORMATS_INVALID_ERROR =
        new MessageError(ErrorType.AIS_406, TppMessageInformation.of(REQUESTED_FORMATS_INVALID));

    @InjectMocks
    private TransactionReportAcceptHeaderValidator validator;

    @Mock
    private AspspProfileService aspspProfileService;

    private JsonReader jsonReader = new JsonReader();

    @Before
    public void setUp() {
        AspspSettings aspspSettings = jsonReader.getObjectFromFile("json/aspsp-settings.json", AspspSettings.class);
        when(aspspProfileService.getAspspSettings()).thenReturn(aspspSettings);
    }

    @Test
    public void validate_success() {
        ValidationResult actual = validator.validate(MediaType.APPLICATION_JSON_VALUE);
        assertTrue(actual.isValid());
    }

    @Test
    public void validate_error() {
        ValidationResult actual = validator.validate(MediaType.APPLICATION_PDF_VALUE);
        assertTrue(actual.isNotValid());
        assertEquals(REQUESTED_FORMATS_INVALID_ERROR, actual.getMessageError());
    }

    @Test
    public void validate_acceptHeaderIsNotPresented_success() {
        ValidationResult actual = validator.validate(null);
        assertTrue(actual.isValid());

        actual = validator.validate("");
        assertTrue(actual.isValid());
    }
}
