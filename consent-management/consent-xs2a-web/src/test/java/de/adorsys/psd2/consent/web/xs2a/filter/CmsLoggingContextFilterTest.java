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

package de.adorsys.psd2.consent.web.xs2a.filter;

import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.logger.context.RequestInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CmsLoggingContextFilterTest {
    private static final String CMS_ENDPOINT_PATH = "/api/v1/";
    private static final String CUSTOM_PATH = "/custom-endpoint";
    private static final String X_REQUEST_ID_HEADER_NAME = "X-Request-ID";
    private static final String INTERNAL_REQUEST_ID_HEADER_NAME = "X-Internal-Request-ID";
    private static final String X_REQUEST_ID = "0d7f200e-09b4-46f5-85bd-f4ea89fccace";
    private static final String INTERNAL_REQUEST_ID = "9fe83704-6019-46fa-b8aa-53fb8fa667ea";
    private static final String INSTANCE_ID_HEADER_NAME = "Instance-ID";
    private static final String INSTANCE_ID = "bank1";
    private static final String HTTP_METHOD = HttpMethod.GET.name();

    @Mock
    private LoggingContextService loggingContextService;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private CmsLoggingContextFilter cmsLoggingContextFilter;

    @Test
    void doFilter_onCmsEndpoint_shouldHandleLoggingContext() throws ServletException, IOException {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest(HTTP_METHOD, CMS_ENDPOINT_PATH);
        mockRequest.addHeader(INTERNAL_REQUEST_ID_HEADER_NAME, INTERNAL_REQUEST_ID);
        mockRequest.addHeader(X_REQUEST_ID_HEADER_NAME, X_REQUEST_ID);
        mockRequest.addHeader(INSTANCE_ID_HEADER_NAME, INSTANCE_ID);
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        // When
        cmsLoggingContextFilter.doFilter(mockRequest, mockResponse, filterChain);

        // Then
        InOrder inOrder = Mockito.inOrder(filterChain, loggingContextService);
        inOrder.verify(loggingContextService).storeRequestInformation(new RequestInfo(INTERNAL_REQUEST_ID, X_REQUEST_ID, INSTANCE_ID));
        inOrder.verify(filterChain).doFilter(mockRequest, mockResponse);
        inOrder.verify(loggingContextService).clearContext();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void doFilter_onCmsEndpointAndNoHeaders_shouldHandleLoggingContext() throws ServletException, IOException {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest(HTTP_METHOD, CMS_ENDPOINT_PATH);
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        // When
        cmsLoggingContextFilter.doFilter(mockRequest, mockResponse, filterChain);

        // Then
        InOrder inOrder = Mockito.inOrder(filterChain, loggingContextService);
        inOrder.verify(loggingContextService).storeRequestInformation(new RequestInfo(null, null, null));
        inOrder.verify(filterChain).doFilter(mockRequest, mockResponse);
        inOrder.verify(loggingContextService).clearContext();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void doFilter_onCustomEndpoint_shouldSkipFilter() throws ServletException, IOException {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest(HTTP_METHOD, CUSTOM_PATH);
        mockRequest.addHeader(INTERNAL_REQUEST_ID_HEADER_NAME, INTERNAL_REQUEST_ID);
        mockRequest.addHeader(X_REQUEST_ID_HEADER_NAME, X_REQUEST_ID);
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        // When
        cmsLoggingContextFilter.doFilter(mockRequest, mockResponse, filterChain);

        // Then
        verify(filterChain).doFilter(mockRequest, mockResponse);
        verify(loggingContextService, never()).storeRequestInformation(any());
        verify(loggingContextService, never()).clearContext();
    }
}
