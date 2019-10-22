/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.service.context.LoggingContextService;
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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LoggingContextClearingFilterTest {
    private static final String SERVLET_PATH = "/v1/accounts";

    @Mock
    private LoggingContextService loggingContextService;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private LoggingContextClearingFilter loggingContextClearingFilter;

    @Test
    public void doFilter_onXs2aEndpoint_shouldClearLoggingContext() throws ServletException, IOException {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setServletPath(SERVLET_PATH);
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        // When
        loggingContextClearingFilter.doFilter(mockRequest, mockResponse, filterChain);

        // Then
        InOrder inOrder = Mockito.inOrder(filterChain, loggingContextService);
        inOrder.verify(filterChain).doFilter(mockRequest, mockResponse);
        inOrder.verify(loggingContextService).clearContext();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void doFilter_onCustomEndpoint_shouldSkipFilter() throws ServletException, IOException {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        // When
        loggingContextClearingFilter.doFilter(mockRequest, mockResponse, filterChain);

        // Then
        verify(filterChain).doFilter(mockRequest, mockResponse);
        verify(loggingContextService, never()).clearContext();
    }
}
