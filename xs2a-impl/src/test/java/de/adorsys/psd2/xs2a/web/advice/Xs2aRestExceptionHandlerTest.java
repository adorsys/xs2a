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

package de.adorsys.psd2.xs2a.web.advice;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorMapperContainer;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Xs2aRestExceptionHandlerTest {

    private static final String ERROR_TEXT = "HTTP method 'DELETE' is not supported";
    private static final String METHOD_NAME = "DELETE";

    @InjectMocks
    private Xs2aRestExceptionHandler xs2aRestExceptionHandler;
    @Mock
    private ErrorMapperContainer errorMapperContainer;
    @Mock
    private ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    @Mock
    private ServiceTypeToErrorTypeMapper errorTypeMapper;

    @Mock
    private WebRequest webRequest;

    @Mock
    private Xs2aObjectMapper xs2aObjectMapper;

    @Test
    void handleHttpRequestMethodNotSupported() throws JsonProcessingException {
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException(METHOD_NAME);
        HttpHeaders headers = new HttpHeaders();
        HttpStatus status = HttpStatus.OK;

        when(serviceTypeDiscoveryService.getServiceType()).thenReturn(ServiceType.PIS);
        when(errorTypeMapper.mapToErrorType(ServiceType.PIS, 405)).thenReturn(ErrorType.PIS_405);
        when(errorMapperContainer.getErrorBody(any())).thenReturn(new ErrorMapperContainer.ErrorBody(ERROR_TEXT, HttpStatus.METHOD_NOT_ALLOWED));
        when(xs2aObjectMapper.writeValueAsString(any())).thenReturn(ERROR_TEXT);

        ResponseEntity response = xs2aRestExceptionHandler.handleHttpRequestMethodNotSupported(exception, headers, status, webRequest);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertEquals(ERROR_TEXT, response.getBody());
    }
}
