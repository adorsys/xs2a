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

package de.adorsys.psd2.logger.web;

import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.logger.context.RequestInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoggingContextInterceptorTest {
    private static final String INTERNAL_REQUEST_ID_HEADER_NAME = "X-Internal-Request-ID";
    private static final String X_REQUEST_ID_HEADER_NAME = "X-Request-ID";
    private static final byte[] REQUEST_BODY = "some request body".getBytes();
    private static final String X_REQUEST_ID = "0d7f200e-09b4-46f5-85bd-f4ea89fccace";
    private static final String INTERNAL_REQUEST_ID = "9fe83704-6019-46fa-b8aa-53fb8fa667ea";

    @Mock
    private LoggingContextService loggingContextService;

    @Mock
    private ClientHttpRequestExecution mockClientHttpRequestExecution;
    @Mock
    private ClientHttpResponse mockClientHttpResponse;
    @Mock
    private HttpRequest mockHttpRequest;
    @Captor
    private ArgumentCaptor<HttpRequest> httpRequestArgumentCaptor;

    @InjectMocks
    private LoggingContextInterceptor loggingContextInterceptor;

    @BeforeEach
    void setUp() throws IOException {
        when(mockHttpRequest.getHeaders()).thenReturn(new HttpHeaders());
        when(mockClientHttpRequestExecution.execute(mockHttpRequest, REQUEST_BODY)).thenReturn(mockClientHttpResponse);
    }

    @Test
    void intercept() throws IOException {
        // Given
        when(loggingContextService.getRequestInformation()).thenReturn(new RequestInfo(INTERNAL_REQUEST_ID, X_REQUEST_ID));

        // When
        ClientHttpResponse actualResponse = loggingContextInterceptor.intercept(mockHttpRequest, REQUEST_BODY, mockClientHttpRequestExecution);

        // Then
        assertEquals(mockClientHttpResponse, actualResponse);

        verify(mockClientHttpRequestExecution).execute(httpRequestArgumentCaptor.capture(), eq(REQUEST_BODY));

        HttpHeaders capturedHeaders = httpRequestArgumentCaptor.getValue().getHeaders();
        assertEquals(capturedHeaders.getFirst(INTERNAL_REQUEST_ID_HEADER_NAME), INTERNAL_REQUEST_ID);
        assertEquals(capturedHeaders.getFirst(X_REQUEST_ID_HEADER_NAME), X_REQUEST_ID);
    }

    @Test
    void intercept_noValues() throws IOException {
        // Given
        when(loggingContextService.getRequestInformation()).thenReturn(new RequestInfo(null, null));

        // When
        ClientHttpResponse actualResponse = loggingContextInterceptor.intercept(mockHttpRequest, REQUEST_BODY, mockClientHttpRequestExecution);

        // Then
        assertEquals(mockClientHttpResponse, actualResponse);

        verify(mockClientHttpRequestExecution).execute(httpRequestArgumentCaptor.capture(), eq(REQUEST_BODY));

        HttpHeaders capturedHeaders = httpRequestArgumentCaptor.getValue().getHeaders();
        assertNull(capturedHeaders.getFirst(INTERNAL_REQUEST_ID_HEADER_NAME));
        assertNull(capturedHeaders.getFirst(X_REQUEST_ID_HEADER_NAME));
    }
}
