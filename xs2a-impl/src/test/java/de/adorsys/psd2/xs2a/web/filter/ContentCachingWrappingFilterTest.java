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

import de.adorsys.psd2.xs2a.component.MultiReadHttpServletRequest;
import de.adorsys.psd2.xs2a.component.MultiReadHttpServletResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ContentCachingWrappingFilterTest {
    private static final String SERVLET_PATH = "/v1/accounts";

    @Mock
    private FilterChain filterChain;

    @Captor
    private ArgumentCaptor<HttpServletRequest> capturedRequest;
    @Captor
    private ArgumentCaptor<HttpServletResponse> capturedResponse;

    @Test
    public void doFilterInternal_shouldWrapRequestAndResponse() throws ServletException, IOException {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setServletPath(SERVLET_PATH);
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        ContentCachingWrappingFilter contentCachingWrappingFilter = new ContentCachingWrappingFilter();

        // When
        contentCachingWrappingFilter.doFilter(mockRequest, mockResponse, filterChain);

        // Then
        verify(filterChain).doFilter(capturedRequest.capture(), capturedResponse.capture());
        assertTrue(capturedRequest.getValue() instanceof MultiReadHttpServletRequest);
        assertTrue(capturedResponse.getValue() instanceof MultiReadHttpServletResponse);
    }

}
