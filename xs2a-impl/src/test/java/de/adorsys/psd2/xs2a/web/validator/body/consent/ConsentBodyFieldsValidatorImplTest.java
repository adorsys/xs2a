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

package de.adorsys.psd2.xs2a.web.validator.body.consent;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.model.Consents;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.Assert.*;

public class ConsentBodyFieldsValidatorImplTest {

    private HttpServletRequest request;
    private ConsentBodyFieldsValidatorImpl validator;
    private Consents consents;
    private MessageError messageError;

    @Before
    public void setUp() {
        JsonReader jsonReader = new JsonReader();
        consents = jsonReader.getObjectFromFile("json/validation/consents.json", Consents.class);
        messageError = new MessageError();
        request = new MockHttpServletRequest();

        validator = new ConsentBodyFieldsValidatorImpl(new ErrorBuildingServiceMock(ErrorType.AIS_400), new ObjectMapper()) {
            @SuppressWarnings("unchecked")
            @Override
            protected <T> Optional<T> mapBodyToInstance(HttpServletRequest request, MessageError messageError, Class<T> clazz) {
                assertEquals(Consents.class, clazz);
                return (Optional<T>) Optional.of(consents);
            }
        };
    }

    @Test
    public void validate_success() {
        validator.validate(request, messageError);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    public void validate_recurringIndicator_null_error() {
        consents.setRecurringIndicator(null);

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Value 'recurringIndicator' should not be null", messageError.getTppMessage().getText());
    }

    @Test
    public void validate_validUntil_null_error() {
        consents.setValidUntil(null);

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Value 'validUntil' should not be null", messageError.getTppMessage().getText());
    }

    @Test
    public void validate_validUntil_inPast_error() {
        consents.setValidUntil(LocalDate.now().minusDays(1));

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Value 'validUntil' should not be in the past", messageError.getTppMessage().getText());
    }

    @Test
    public void validate_frequencyPerDay_null_error() {
        consents.setFrequencyPerDay(null);

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Value 'frequencyPerDay' should not be null", messageError.getTppMessage().getText());
    }

    @Test
    public void validate_frequencyPerDay_is0_error() {
        consents.setFrequencyPerDay(0);

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Value 'frequencyPerDay' should not be lower than 1", messageError.getTppMessage().getText());
    }

    @Test
    public void validate_frequencyPerDay_lessThen1_error() {
        consents.setFrequencyPerDay(-1);

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Value 'frequencyPerDay' should not be lower than 1", messageError.getTppMessage().getText());
    }
}
