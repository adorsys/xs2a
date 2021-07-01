/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.mapper.psd2;

import de.adorsys.psd2.model.Error400NGPIS;
import de.adorsys.psd2.model.TppMessage400PIS;
import de.adorsys.psd2.model.TppMessageCategory;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.web.error.ServiceUnavailableError;
import de.adorsys.psd2.xs2a.web.header.ResponseHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResponseErrorMapperTest {
    private static final String FORMAT_ERROR = "FORMAT_ERROR";

    @InjectMocks
    private ResponseErrorMapper responseErrorMapper;
    @Mock
    private ErrorMapperContainer errorMapperContainer;

    private ErrorMapperContainer.ErrorBody errorBody;
    private Error400NGPIS error400NGPIS;
    private TppMessage400PIS tppMessage;
    private HttpStatus httpStatus;
    private MessageError messageError;

    @BeforeEach
    void setUp() {
        tppMessage = getTppMessage400PIS();
        messageError = new MessageError();
        httpStatus = HttpStatus.BAD_REQUEST;
        error400NGPIS = getError400NGPIS();

        errorBody = getErrorBody();
    }

    @Test
    void generateErrorResponseFromMessageError() {
        // Given
        when(errorMapperContainer.getErrorBody(messageError)).thenReturn(errorBody);

        // When
        var actual = responseErrorMapper.generateErrorResponse(messageError);
        var expected = new ResponseEntity<>(error400NGPIS, httpStatus);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void generateErrorResponseFromMessageErrorAndResponseHeaders() {
        // Given
        var responseHeaders = ResponseHeaders.builder().build();
        when(errorMapperContainer.getErrorBody(messageError)).thenReturn(errorBody);

        // When
        var actual = responseErrorMapper.generateErrorResponse(messageError, responseHeaders);
        var expected = new ResponseEntity<>(error400NGPIS, responseHeaders.getHttpHeaders(), httpStatus);

        // Then
        assertThat(actual).isEqualTo(expected);
    }


    @Test
    void generateServiceUnavailableErrorResponse() {
        // When
        var actual = responseErrorMapper.generateServiceUnavailableErrorResponse("unknown error");
        var expected = new ResponseEntity<>(new ServiceUnavailableError(), HttpStatus.SERVICE_UNAVAILABLE);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    private ErrorMapperContainer.ErrorBody getErrorBody() {
        Error400NGPIS error400NGPIS = getError400NGPIS();
        return new ErrorMapperContainer.ErrorBody(error400NGPIS, httpStatus);
    }

    private Error400NGPIS getError400NGPIS() {
        Error400NGPIS error400NGPIS = new Error400NGPIS();
        error400NGPIS.setTppMessages(Collections.singletonList(tppMessage));
        return error400NGPIS;
    }

    private TppMessage400PIS getTppMessage400PIS() {
        TppMessage400PIS tppMessage = new TppMessage400PIS();
        tppMessage.setCategory(TppMessageCategory.ERROR);
        tppMessage.setCode(FORMAT_ERROR);
        tppMessage.setText(FORMAT_ERROR);
        return tppMessage;
    }
}
