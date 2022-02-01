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
    private static final String INSTANCE_ID = "bank1";

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
        when(loggingContextService.getRequestInformation()).thenReturn(new RequestInfo(INTERNAL_REQUEST_ID, X_REQUEST_ID, INSTANCE_ID));

        // When
        ClientHttpResponse actualResponse = loggingContextInterceptor.intercept(mockHttpRequest, REQUEST_BODY, mockClientHttpRequestExecution);

        // Then
        assertEquals(mockClientHttpResponse, actualResponse);

        verify(mockClientHttpRequestExecution).execute(httpRequestArgumentCaptor.capture(), eq(REQUEST_BODY));

        HttpHeaders capturedHeaders = httpRequestArgumentCaptor.getValue().getHeaders();
        assertEquals(INTERNAL_REQUEST_ID, capturedHeaders.getFirst(INTERNAL_REQUEST_ID_HEADER_NAME));
        assertEquals(X_REQUEST_ID, capturedHeaders.getFirst(X_REQUEST_ID_HEADER_NAME));
    }

    @Test
    void intercept_noValues() throws IOException {
        // Given
        when(loggingContextService.getRequestInformation()).thenReturn(new RequestInfo(null, null, null));

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
