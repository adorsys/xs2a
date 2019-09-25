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

package de.adorsys.psd2.xs2a.service.mapper.psd2.sb;

import de.adorsys.psd2.model.Error401NGSBS;
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

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_INVALID;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SB401ErrorMapperTest {
    private static final String ERROR_JSON_PATH = "json/service/mapper/psd2/sb/Error401NGSBS.json";
    private static final String ERROR_CUSTOM_TEXT_JSON_PATH = "json/service/mapper/psd2/sb/Error401NGSBS-custom-text.json";
    private static final String CUSTOM_ERROR_TEXT = "Custom text";
    private static final MessageError MESSAGE_ERROR = new MessageError(ErrorType.SB_401,
                                                                       TppMessageInformation.of(CONSENT_INVALID, "consent"));
    private static final MessageError MESSAGE_ERROR_WITHOUT_TEXT = new MessageError(ErrorType.SB_401,
                                                                                    TppMessageInformation.of(CONSENT_INVALID));

    private JsonReader jsonReader = new JsonReader();
    @Mock
    private MessageService messageService;
    @InjectMocks
    private SB401ErrorMapper sb401ErrorMapper;

    @Test
    public void getErrorStatus_shouldReturn401() {
        // When
        HttpStatus errorStatus = sb401ErrorMapper.getErrorStatus();

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, errorStatus);
    }

    @Test
    public void getMapper_shouldReturnCorrectErrorMapper() {
        // Given
        Error401NGSBS expectedError = jsonReader.getObjectFromFile(ERROR_JSON_PATH, Error401NGSBS.class);
        when(messageService.getMessage(CONSENT_INVALID.getName())).thenReturn("The %s was created by this TPP but is not valid for the addressed service/resource");

        // When
        Function<MessageError, Error401NGSBS> mapper = sb401ErrorMapper.getMapper();
        Error401NGSBS actualError = mapper.apply(MESSAGE_ERROR);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    public void getMapper_withNoTextInTppMessage_shouldGetTextFromMessageService() {
        when(messageService.getMessage(CONSENT_INVALID.name()))
            .thenReturn(CUSTOM_ERROR_TEXT);

        // Given
        Error401NGSBS expectedError = jsonReader.getObjectFromFile(ERROR_CUSTOM_TEXT_JSON_PATH, Error401NGSBS.class);

        // When
        Function<MessageError, Error401NGSBS> mapper = sb401ErrorMapper.getMapper();
        Error401NGSBS actualError = mapper.apply(MESSAGE_ERROR_WITHOUT_TEXT);

        // Then
        assertEquals(expectedError, actualError);
        verify(messageService).getMessage(CONSENT_INVALID.name());
    }
}
