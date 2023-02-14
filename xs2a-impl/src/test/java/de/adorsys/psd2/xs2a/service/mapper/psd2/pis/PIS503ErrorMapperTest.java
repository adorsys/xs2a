/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.exception.model.error503.Error503NGPIS;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.function.Function;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.SERVICE_UNAVAILABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PIS503ErrorMapperTest {
    private static final String ERROR_JSON_PATH = "json/service/mapper/psd2/pis/Error503NGPIS.json";
    private static final String ERROR_CUSTOM_TEXT_JSON_PATH = "json/service/mapper/psd2/pis/Error503NGPIS-custom-text.json";
    private static final String CUSTOM_ERROR_TEXT = "Custom text";
    private static final MessageError MESSAGE_ERROR = new MessageError(ErrorType.PIS_503,
        TppMessageInformation.of(SERVICE_UNAVAILABLE, "text"));
    private static final MessageError MESSAGE_ERROR_WITHOUT_TEXT = new MessageError(ErrorType.PIS_503,
        TppMessageInformation.of(SERVICE_UNAVAILABLE));

    private JsonReader jsonReader = new JsonReader();
    @Mock
    private MessageService messageService;
    @InjectMocks
    private PIS503ErrorMapper pis503ErrorMapper;

    @Test
    void getErrorStatus_shouldReturn503() {
        // When
        HttpStatus errorStatus = pis503ErrorMapper.getErrorStatus();

        // Then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, errorStatus);
    }

    @Test
    void getMapper_shouldReturnCorrectErrorMapper() {
        when(messageService.getMessage(SERVICE_UNAVAILABLE.name()))
            .thenReturn("Some %s");

        // Given
        Error503NGPIS expectedError = jsonReader.getObjectFromFile(ERROR_JSON_PATH, Error503NGPIS.class);

        // When
        Function<MessageError, Error503NGPIS> mapper = pis503ErrorMapper.getMapper();
        Error503NGPIS actualError = mapper.apply(MESSAGE_ERROR);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void getMapper_withNoTextInTppMessage_shouldGetTextFromMessageService() {
        when(messageService.getMessage(SERVICE_UNAVAILABLE.name()))
            .thenReturn(CUSTOM_ERROR_TEXT);

        // Given
        Error503NGPIS expectedError = jsonReader.getObjectFromFile(ERROR_CUSTOM_TEXT_JSON_PATH, Error503NGPIS.class);

        // When
        Function<MessageError, Error503NGPIS> mapper = pis503ErrorMapper.getMapper();
        Error503NGPIS actualError = mapper.apply(MESSAGE_ERROR_WITHOUT_TEXT);

        // Then
        assertEquals(expectedError, actualError);
        verify(messageService).getMessage(SERVICE_UNAVAILABLE.name());
    }
}
