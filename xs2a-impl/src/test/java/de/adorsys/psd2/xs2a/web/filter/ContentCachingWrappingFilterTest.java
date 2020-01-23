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

import de.adorsys.psd2.xs2a.component.MultiReadHttpServletRequest;
import de.adorsys.psd2.xs2a.component.MultiReadHttpServletResponse;
import de.adorsys.psd2.xs2a.web.Xs2aEndpointChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentCachingWrappingFilterTest {
    @Mock
    private FilterChain filterChain;
    @Mock
    private Xs2aEndpointChecker xs2aEndpointChecker;

    @InjectMocks
    private ContentCachingWrappingFilter contentCachingWrappingFilter;

    @Captor
    private ArgumentCaptor<HttpServletRequest> capturedRequest;
    @Captor
    private ArgumentCaptor<HttpServletResponse> capturedResponse;

    @Test
    void doFilterInternal_shouldWrapRequestAndResponse() throws ServletException, IOException {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        when(xs2aEndpointChecker.isXs2aEndpoint(mockRequest))
            .thenReturn(true);

        // When
        contentCachingWrappingFilter.doFilter(mockRequest, mockResponse, filterChain);

        // Then
        verify(filterChain).doFilter(capturedRequest.capture(), capturedResponse.capture());
        assertTrue(capturedRequest.getValue() instanceof MultiReadHttpServletRequest);
        assertTrue(capturedResponse.getValue() instanceof MultiReadHttpServletResponse);
    }
}
