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

package de.adorsys.psd2.xs2a.web.filter;

import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.context.LoggingContextService;
import de.adorsys.psd2.xs2a.web.request.RequestPathResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LoggingContextFilterTest {
    private static final String XS2A_PATH = "/v1/accounts";
    private static final String CUSTOM_PATH = "/custom-endpoint";
    private static final String X_REQUEST_ID = "0d7f200e-09b4-46f5-85bd-f4ea89fccace";
    private static final String INTERNAL_REQUEST_ID = "9fe83704-6019-46fa-b8aa-53fb8fa667ea";

    @Mock
    private LoggingContextService loggingContextService;
    @Mock
    private FilterChain filterChain;
    @Mock
    private RequestPathResolver requestPathResolver;
    @Mock
    private RequestProviderService requestProviderService;

    @InjectMocks
    private LoggingContextFilter loggingContextFilter;

    @Test
    public void doFilter_onXs2aEndpoint_shouldHandleLoggingContext() throws ServletException, IOException {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        when(requestPathResolver.resolveRequestPath(mockRequest))
            .thenReturn(XS2A_PATH);

        when(requestProviderService.getInternalRequestIdString()).thenReturn(INTERNAL_REQUEST_ID);
        when(requestProviderService.getRequestIdString()).thenReturn(X_REQUEST_ID);

        // When
        loggingContextFilter.doFilter(mockRequest, mockResponse, filterChain);

        // Then
        InOrder inOrder = Mockito.inOrder(filterChain, loggingContextService);
        inOrder.verify(loggingContextService).storeRequestInformation(INTERNAL_REQUEST_ID, X_REQUEST_ID);
        inOrder.verify(filterChain).doFilter(mockRequest, mockResponse);
        inOrder.verify(loggingContextService).clearContext();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void doFilter_onCustomEndpoint_shouldSkipFilter() throws ServletException, IOException {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        when(requestPathResolver.resolveRequestPath(mockRequest))
            .thenReturn(CUSTOM_PATH);

        // When
        loggingContextFilter.doFilter(mockRequest, mockResponse, filterChain);

        // Then
        verify(filterChain).doFilter(mockRequest, mockResponse);
        verify(loggingContextService, never()).storeRequestInformation(any(), any());
        verify(loggingContextService, never()).clearContext();
    }
}
