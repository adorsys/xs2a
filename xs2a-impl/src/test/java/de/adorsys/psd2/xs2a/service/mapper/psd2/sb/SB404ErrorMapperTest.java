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

import de.adorsys.psd2.model.Error404NGSBS;
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

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.RESOURCE_UNKNOWN_404;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SB404ErrorMapperTest {
    private static final String ERROR_JSON_PATH = "json/service/mapper/psd2/sb/Error404NGSBS.json";
    private static final String ERROR_CUSTOM_TEXT_JSON_PATH = "json/service/mapper/psd2/sb/Error404NGSBS-custom-text.json";
    private static final String CUSTOM_ERROR_TEXT = "Custom text";
    private static final MessageError MESSAGE_ERROR = new MessageError(ErrorType.SB_404,
                                                                       TppMessageInformation.of(RESOURCE_UNKNOWN_404, "text"));
    private static final MessageError MESSAGE_ERROR_WITHOUT_TEXT = new MessageError(ErrorType.SB_404,
                                                                                    TppMessageInformation.of(RESOURCE_UNKNOWN_404));

    private JsonReader jsonReader = new JsonReader();
    @Mock
    private MessageService messageService;
    @InjectMocks
    private SB404ErrorMapper sb404ErrorMapper;

    @Test
    public void getErrorStatus_shouldReturn404() {
        // When
        HttpStatus errorStatus = sb404ErrorMapper.getErrorStatus();

        // Then
        assertEquals(HttpStatus.NOT_FOUND, errorStatus);
    }

    @Test
    public void getMapper_shouldReturnCorrectErrorMapper() {
        when(messageService.getMessage(RESOURCE_UNKNOWN_404.name()))
            .thenReturn("Some %s");

        // Given
        Error404NGSBS expectedError = jsonReader.getObjectFromFile(ERROR_JSON_PATH, Error404NGSBS.class);

        // When
        Function<MessageError, Error404NGSBS> mapper = sb404ErrorMapper.getMapper();
        Error404NGSBS actualError = mapper.apply(MESSAGE_ERROR);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    public void getMapper_withNoTextInTppMessage_shouldGetTextFromMessageService() {
        when(messageService.getMessage(RESOURCE_UNKNOWN_404.name()))
            .thenReturn(CUSTOM_ERROR_TEXT);

        // Given
        Error404NGSBS expectedError = jsonReader.getObjectFromFile(ERROR_CUSTOM_TEXT_JSON_PATH, Error404NGSBS.class);

        // When
        Function<MessageError, Error404NGSBS> mapper = sb404ErrorMapper.getMapper();
        Error404NGSBS actualError = mapper.apply(MESSAGE_ERROR_WITHOUT_TEXT);

        // Then
        assertEquals(expectedError, actualError);
        verify(messageService).getMessage(RESOURCE_UNKNOWN_404.name());
    }
}
