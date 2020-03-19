/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
