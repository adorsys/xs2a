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

import de.adorsys.psd2.model.Error403NGPIS;
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

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.PRODUCT_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PIS403ErrorMapperTest {
    private static final String ERROR_JSON_PATH = "json/service/mapper/psd2/pis/Error403NGPIS.json";
    private static final String ERROR_CUSTOM_TEXT_JSON_PATH = "json/service/mapper/psd2/pis/Error403NGPIS-custom-text.json";
    private static final String CUSTOM_ERROR_TEXT = "Custom text";
    private static final MessageError MESSAGE_ERROR = new MessageError(ErrorType.PIS_403,
                                                                       TppMessageInformation.of(PRODUCT_INVALID, "payment"));
    private static final MessageError MESSAGE_ERROR_WITHOUT_TEXT = new MessageError(ErrorType.PIS_403,
                                                                                    TppMessageInformation.of(PRODUCT_INVALID));

    private JsonReader jsonReader = new JsonReader();
    @Mock
    private MessageService messageService;
    @InjectMocks
    private PIS403ErrorMapper pis403ErrorMapper;

    @Test
    void getErrorStatus_shouldReturn403() {
        // When
        HttpStatus errorStatus = pis403ErrorMapper.getErrorStatus();

        // Then
        assertEquals(HttpStatus.FORBIDDEN, errorStatus);
    }

    @Test
    void getMapper_shouldReturnCorrectErrorMapper() {
        // Given
        Error403NGPIS expectedError = jsonReader.getObjectFromFile(ERROR_JSON_PATH, Error403NGPIS.class);
        when(messageService.getMessage(PRODUCT_INVALID.getName())).thenReturn("The addressed %s product is not available for the PSU");

        // When
        Function<MessageError, Error403NGPIS> mapper = pis403ErrorMapper.getMapper();
        Error403NGPIS actualError = mapper.apply(MESSAGE_ERROR);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void getMapper_withNoTextInTppMessage_shouldGetTextFromMessageService() {
        when(messageService.getMessage(PRODUCT_INVALID.name()))
            .thenReturn(CUSTOM_ERROR_TEXT);

        // Given
        Error403NGPIS expectedError = jsonReader.getObjectFromFile(ERROR_CUSTOM_TEXT_JSON_PATH, Error403NGPIS.class);

        // When
        Function<MessageError, Error403NGPIS> mapper = pis403ErrorMapper.getMapper();
        Error403NGPIS actualError = mapper.apply(MESSAGE_ERROR_WITHOUT_TEXT);

        // Then
        assertEquals(expectedError, actualError);
        verify(messageService).getMessage(PRODUCT_INVALID.name());
    }
}
