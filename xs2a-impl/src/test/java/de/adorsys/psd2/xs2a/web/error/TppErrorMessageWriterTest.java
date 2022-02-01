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

package de.adorsys.psd2.xs2a.web.error;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.core.domain.MessageCategory;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorMapperContainer;
import de.adorsys.psd2.xs2a.web.filter.TppErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TppErrorMessageWriterTest {
    @Mock
    private ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    @Mock
    private ErrorMapperContainer errorMapperContainer;
    @Mock
    private Xs2aObjectMapper xs2aObjectMapper;
    @Mock
    private HttpServletResponse response;

    private TppErrorMessageWriter tppErrorMessageWriter;

    private final static ServiceType SERVICE_TYPE = ServiceType.AIS;
    private final static MessageCategory MESSAGE_CATEGORY = MessageCategory.ERROR;
    private final static MessageErrorCode MESSAGE_ERROR_CODE = MessageErrorCode.FORMAT_ERROR;
    private final static int STATUS_CODE = HttpServletResponse.SC_UNAUTHORIZED;
    private final static String MESSAGE_ERROR_STRING = "Test";

    private final static PrintWriter PRINT_WRITER = new PrintWriter(System.out);
    private final static ErrorMapperContainer.ErrorBody ERROR_BODY = new ErrorMapperContainer.ErrorBody(new Object(), HttpStatus.OK);

    @BeforeEach
    void setUp() {
        tppErrorMessageWriter = new TppErrorMessageWriter(serviceTypeDiscoveryService, errorMapperContainer, xs2aObjectMapper);
    }

    @Test
    void writeError_wrongServiceType() {
        TppErrorMessage tppErrorMessage = new TppErrorMessage(MESSAGE_CATEGORY, MESSAGE_ERROR_CODE, MESSAGE_ERROR_STRING);

        assertThrows(IllegalArgumentException.class, () -> tppErrorMessageWriter.writeError(response, tppErrorMessage));

        verify(serviceTypeDiscoveryService).getServiceType();
    }

    @Test
    void writeError_successful() throws IOException {
        when(serviceTypeDiscoveryService.getServiceType()).thenReturn(SERVICE_TYPE);

        TppErrorMessage tppErrorMessage = new TppErrorMessage(MESSAGE_CATEGORY, MESSAGE_ERROR_CODE, MESSAGE_ERROR_STRING);
        MessageError messageError = new MessageError(ErrorType.AIS_400, TppMessageInformation.of(tppErrorMessage.getCategory(), tppErrorMessage.getCode(), tppErrorMessage.getTextParams()));
        when(errorMapperContainer.getErrorBody(messageError)).thenReturn(ERROR_BODY);
        when(response.getWriter()).thenReturn(PRINT_WRITER);

        tppErrorMessageWriter.writeError(response, tppErrorMessage);

        ArgumentCaptor<Writer> writerArgumentCaptor = ArgumentCaptor.forClass(Writer.class);
        ArgumentCaptor<ErrorMapperContainer.ErrorBody> errorBodyArgumentCaptor = ArgumentCaptor.forClass(ErrorMapperContainer.ErrorBody.class);

        verify(serviceTypeDiscoveryService).getServiceType();
        verify(errorMapperContainer).getErrorBody(messageError);
        verify(xs2aObjectMapper).writeValue(writerArgumentCaptor.capture(), errorBodyArgumentCaptor.capture());

        assertEquals(PRINT_WRITER, writerArgumentCaptor.getValue());
        assertEquals(ERROR_BODY.getBody(), errorBodyArgumentCaptor.getValue());
    }

    @Test
    void writeMessageError_successful() throws IOException {
        //Given
        MessageError messageError = new MessageError(ErrorType.AIS_400, TppMessageInformation.buildWithCustomError(MESSAGE_ERROR_CODE, MESSAGE_ERROR_STRING));
        ErrorMapperContainer.ErrorBody errorBody = new ErrorMapperContainer.ErrorBody(messageError, HttpStatus.OK);

        when(errorMapperContainer.getErrorBody(messageError)).thenReturn(errorBody);
        when(response.getWriter()).thenReturn(PRINT_WRITER);
        //When
        tppErrorMessageWriter.writeError(response, messageError);
        //Then
        ArgumentCaptor<Writer> writerArgumentCaptor = ArgumentCaptor.forClass(Writer.class);
        ArgumentCaptor<ErrorMapperContainer.ErrorBody> errorBodyArgumentCaptor = ArgumentCaptor.forClass(ErrorMapperContainer.ErrorBody.class);

        verify(errorMapperContainer).getErrorBody(messageError);
        verify(xs2aObjectMapper).writeValue(writerArgumentCaptor.capture(), errorBodyArgumentCaptor.capture());

        assertEquals(PRINT_WRITER, writerArgumentCaptor.getValue());
        assertEquals(errorBody.getBody(), errorBodyArgumentCaptor.getValue());
    }
}
