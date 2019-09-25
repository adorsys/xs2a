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

package de.adorsys.psd2.xs2a.service.mapper.psd2.piis;

import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.exception.model.error429.Error429NGPIIS;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import java.util.function.Function;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.ACCESS_EXCEEDED;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PIIS429ErrorMapperTest {
    private static final String ERROR_JSON_PATH = "json/service/mapper/psd2/piis/Error429NGPIIS.json";
    private static final String ERROR_CUSTOM_TEXT_JSON_PATH = "json/service/mapper/psd2/piis/Error429NGPIIS-custom-text.json";
    private static final String CUSTOM_ERROR_TEXT = "Custom text";
    private static final MessageError MESSAGE_ERROR = new MessageError(ErrorType.PIIS_429,
                                                                       TppMessageInformation.of(ACCESS_EXCEEDED, "text"));
    private static final MessageError MESSAGE_ERROR_WITHOUT_TEXT = new MessageError(ErrorType.PIIS_429,
                                                                                    TppMessageInformation.of(ACCESS_EXCEEDED));

    private JsonReader jsonReader = new JsonReader();
    @Mock
    private MessageService messageService;
    @InjectMocks
    private PIIS429ErrorMapper piis429ErrorMapper;

    @Test
    public void getErrorStatus_shouldReturn429() {
        // When
        HttpStatus errorStatus = piis429ErrorMapper.getErrorStatus();

        // Then
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, errorStatus);
    }

    @Test
    public void getMapper_shouldReturnCorrectErrorMapper() {
        when(messageService.getMessage(ACCESS_EXCEEDED.name()))
            .thenReturn("Some %s");

        // Given
        Error429NGPIIS expectedError = jsonReader.getObjectFromFile(ERROR_JSON_PATH, Error429NGPIIS.class);

        // When
        Function<MessageError, Error429NGPIIS> mapper = piis429ErrorMapper.getMapper();
        Error429NGPIIS actualError = mapper.apply(MESSAGE_ERROR);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    public void getMapper_withNoTextInTppMessage_shouldGetTextFromMessageService() {
        when(messageService.getMessage(ACCESS_EXCEEDED.name()))
            .thenReturn(CUSTOM_ERROR_TEXT);

        // Given
        Error429NGPIIS expectedError = jsonReader.getObjectFromFile(ERROR_CUSTOM_TEXT_JSON_PATH, Error429NGPIIS.class);

        // When
        Function<MessageError, Error429NGPIIS> mapper = piis429ErrorMapper.getMapper();
        Error429NGPIIS actualError = mapper.apply(MESSAGE_ERROR_WITHOUT_TEXT);

        // Then
        assertEquals(expectedError, actualError);
        verify(messageService).getMessage(ACCESS_EXCEEDED.name());
    }
}
