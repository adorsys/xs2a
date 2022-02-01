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
