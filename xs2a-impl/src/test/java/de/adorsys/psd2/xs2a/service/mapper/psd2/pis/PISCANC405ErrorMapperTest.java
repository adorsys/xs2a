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

package de.adorsys.psd2.xs2a.service.mapper.psd2.pis;

import de.adorsys.psd2.model.Error405NGPISCANC;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.function.Function;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CANCELLATION_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PISCANC405ErrorMapperTest {
    private static final String ERROR_JSON_PATH = "json/service/mapper/psd2/pis/Error405NGPISCANC.json";
    private static final String ERROR_CUSTOM_TEXT_JSON_PATH = "json/service/mapper/psd2/pis/Error405NGPISCANC-custom-text.json";
    private static final String CUSTOM_ERROR_TEXT = "Custom text";
    private static final MessageError MESSAGE_ERROR = new MessageError(ErrorType.PIS_405,
                                                                       TppMessageInformation.of(CANCELLATION_INVALID, "text"));
    private static final MessageError MESSAGE_ERROR_WITHOUT_TEXT = new MessageError(ErrorType.PIS_405,
                                                                                    TppMessageInformation.of(CANCELLATION_INVALID));

    private JsonReader jsonReader = new JsonReader();
    @Mock
    private MessageService messageService;
    @InjectMocks
    private PISCANC405ErrorMapper piscanc405ErrorMapper;

    @Test
    void getErrorStatus_shouldReturn405() {
        // When
        HttpStatus errorStatus = piscanc405ErrorMapper.getErrorStatus();

        // Then
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, errorStatus);
    }

    @Test
    void getMapper_shouldReturnCorrectErrorMapper() {
        when(messageService.getMessage(CANCELLATION_INVALID.name()))
            .thenReturn("Some %s");

        // Given
        Error405NGPISCANC expectedError = jsonReader.getObjectFromFile(ERROR_JSON_PATH, Error405NGPISCANC.class);

        // When
        Function<MessageError, Error405NGPISCANC> mapper = piscanc405ErrorMapper.getMapper();
        Error405NGPISCANC actualError = mapper.apply(MESSAGE_ERROR);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void getMapper_withNoTextInTppMessage_shouldGetTextFromMessageService() {
        when(messageService.getMessage(CANCELLATION_INVALID.name()))
            .thenReturn(CUSTOM_ERROR_TEXT);

        // Given
        Error405NGPISCANC expectedError = jsonReader.getObjectFromFile(ERROR_CUSTOM_TEXT_JSON_PATH, Error405NGPISCANC.class);

        // When
        Function<MessageError, Error405NGPISCANC> mapper = piscanc405ErrorMapper.getMapper();
        Error405NGPISCANC actualError = mapper.apply(MESSAGE_ERROR_WITHOUT_TEXT);

        // Then
        assertEquals(expectedError, actualError);
        verify(messageService).getMessage(CANCELLATION_INVALID.name());
    }
}
