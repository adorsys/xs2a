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

import de.adorsys.psd2.model.Error409NGPIIS;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
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

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.STATUS_INVALID;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PIIS409ErrorMapperTest {
    private static final String ERROR_JSON_PATH = "json/service/mapper/psd2/piis/Error409NGPIIS.json";
    private static final String ERROR_CUSTOM_TEXT_JSON_PATH = "json/service/mapper/psd2/piis/Error409NGPIIS-custom-text.json";
    private static final String CUSTOM_ERROR_TEXT = "Custom text";
    private static final MessageError MESSAGE_ERROR = new MessageError(ErrorType.PIIS_409,
                                                                       TppMessageInformation.of(STATUS_INVALID, "text"));
    private static final MessageError MESSAGE_ERROR_WITHOUT_TEXT = new MessageError(ErrorType.PIIS_409,
                                                                                    TppMessageInformation.of(STATUS_INVALID));

    private JsonReader jsonReader = new JsonReader();
    @Mock
    private MessageService messageService;
    @InjectMocks
    private PIIS409ErrorMapper piis409ErrorMapper;

    @Test
    public void getErrorStatus_shouldReturn409() {
        // When
        HttpStatus errorStatus = piis409ErrorMapper.getErrorStatus();

        // Then
        assertEquals(HttpStatus.CONFLICT, errorStatus);
    }

    @Test
    public void getMapper_shouldReturnCorrectErrorMapper() {
        when(messageService.getMessage(STATUS_INVALID.name()))
            .thenReturn("Some %s");

        // Given
        Error409NGPIIS expectedError = jsonReader.getObjectFromFile(ERROR_JSON_PATH, Error409NGPIIS.class);

        // When
        Function<MessageError, Error409NGPIIS> mapper = piis409ErrorMapper.getMapper();
        Error409NGPIIS actualError = mapper.apply(MESSAGE_ERROR);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    public void getMapper_withNoTextInTppMessage_shouldGetTextFromMessageService() {
        when(messageService.getMessage(STATUS_INVALID.name()))
            .thenReturn(CUSTOM_ERROR_TEXT);

        // Given
        Error409NGPIIS expectedError = jsonReader.getObjectFromFile(ERROR_CUSTOM_TEXT_JSON_PATH, Error409NGPIIS.class);

        // When
        Function<MessageError, Error409NGPIIS> mapper = piis409ErrorMapper.getMapper();
        Error409NGPIIS actualError = mapper.apply(MESSAGE_ERROR_WITHOUT_TEXT);

        // Then
        assertEquals(expectedError, actualError);
        verify(messageService).getMessage(STATUS_INVALID.name());
    }
}
