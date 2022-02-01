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

import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FundsConfirmationLoggingInterceptorTest {
    private static final String TPP_IP = "1.1.1.1";
    private static final String TPP_INFO_JSON = "json/web/interceptor/logging/tpp-info.json";
    private static final String REQUEST_URI = "request_uri";

    @InjectMocks
    private FundsConfirmationLoggingInterceptor interceptor;
    @Mock
    private TppService tppService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        when(tppService.getTppInfo()).thenReturn(jsonReader.getObjectFromFile(TPP_INFO_JSON, TppInfo.class));
    }

    @Test
    void preHandle_success() {
        when(request.getRemoteAddr()).thenReturn(TPP_IP);
        when(request.getRequestURI()).thenReturn(REQUEST_URI);

        interceptor.preHandle(request, response, null);

        verify(tppService).getTppInfo();
        verify(request).getRemoteAddr();
        verify(request).getRequestURI();
    }

    @Test
    void afterCompletion_success() {
        when(response.getStatus()).thenReturn(HttpServletResponse.SC_OK);

        interceptor.afterCompletion(request, response, null, null);

        verify(tppService).getTppInfo();
        verify(response).getStatus();
    }
}
