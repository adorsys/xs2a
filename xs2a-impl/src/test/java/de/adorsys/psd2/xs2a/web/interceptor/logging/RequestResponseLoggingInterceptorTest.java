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

package de.adorsys.psd2.xs2a.web.interceptor.logging;

import de.adorsys.psd2.xs2a.component.logger.request.RequestResponseLogMessage;
import de.adorsys.psd2.xs2a.component.logger.request.RequestResponseLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RequestResponseLoggingInterceptorTest {
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private HttpServletResponse httpServletResponse;
    @Mock
    private RequestResponseLogger requestResponseLogger;
    @InjectMocks
    private RequestResponseLoggingInterceptor requestResponseLoggingInterceptor;

    @Test
    void afterCompletion_shouldLogRequestAndResponse() {
        // Given
        UUID internalRequestId = UUID.fromString("b87028ad-6925-41fa-b892-88912606a2f4");

        RequestResponseLogMessage message = RequestResponseLogMessage.builder(httpServletRequest, httpServletResponse)
                                                .withRequestUri()
                                                .withRequestHeaders()
                                                .withRequestPayload()
                                                .withResponseStatus()
                                                .withResponseHeaders()
                                                .withResponseBody()
                                                .build();

        // When
        requestResponseLoggingInterceptor.afterCompletion(httpServletRequest, httpServletResponse, null, null);

        // Then
        verify(requestResponseLogger).logMessage(message);
    }
}
