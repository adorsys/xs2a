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

import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.logger.context.RequestInfo;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.web.Xs2aEndpointChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Xs2aLoggingContextFilterTest {
    private static final String X_REQUEST_ID = "0d7f200e-09b4-46f5-85bd-f4ea89fccace";
    private static final String INTERNAL_REQUEST_ID = "9fe83704-6019-46fa-b8aa-53fb8fa667ea";
    private static final String INSTANCE_ID = "bank1";

    @Mock
    private LoggingContextService loggingContextService;
    @Mock
    private FilterChain filterChain;
    @Mock
    private Xs2aEndpointChecker xs2aEndpointChecker;
    ;
    @Mock
    private RequestProviderService requestProviderService;

    @InjectMocks
    private Xs2aLoggingContextFilter loggingContextFilter;

    @Test
    void doFilter_onXs2aEndpoint_shouldHandleLoggingContext() throws ServletException, IOException {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        when(xs2aEndpointChecker.isXs2aEndpoint(mockRequest))
            .thenReturn(true);

        when(requestProviderService.getInternalRequestIdString()).thenReturn(INTERNAL_REQUEST_ID);
        when(requestProviderService.getRequestIdString()).thenReturn(X_REQUEST_ID);
        when(requestProviderService.getInstanceId()).thenReturn(INSTANCE_ID);

        // When
        loggingContextFilter.doFilter(mockRequest, mockResponse, filterChain);

        // Then
        InOrder inOrder = Mockito.inOrder(filterChain, loggingContextService);
        inOrder.verify(loggingContextService).storeRequestInformation(new RequestInfo(INTERNAL_REQUEST_ID, X_REQUEST_ID, INSTANCE_ID));
        inOrder.verify(filterChain).doFilter(mockRequest, mockResponse);
        inOrder.verify(loggingContextService).clearContext();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void doFilter_onCustomEndpoint_shouldSkipFilter() throws ServletException, IOException {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        when(xs2aEndpointChecker.isXs2aEndpoint(mockRequest))
            .thenReturn(false);

        // When
        loggingContextFilter.doFilter(mockRequest, mockResponse, filterChain);

        // Then
        verify(filterChain).doFilter(mockRequest, mockResponse);
        verify(loggingContextService, never()).storeRequestInformation(any());
        verify(loggingContextService, never()).clearContext();
    }
}
