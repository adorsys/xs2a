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

package de.adorsys.psd2.xs2a.web.interceptor.logging;

import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.component.MultiReadHttpServletResponse;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.web.PathParameterExtractor;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentLoggingInterceptorTest {
    private static final String TPP_IP = "1.1.1.1";
    private static final String TPP_INFO_JSON = "json/web/interceptor/logging/tpp-info.json";
    private static final String REQUEST_URI = "request_uri";
    private static final String REDIRECT_ID = "redirect-id";

    @InjectMocks
    private PaymentLoggingInterceptor interceptor;
    @Mock
    private TppService tppService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private MultiReadHttpServletResponse response;
    @Mock
    private RedirectIdService redirectIdService;
    @Mock
    private LoggingContextService loggingContextService;
    @Mock
    private PathParameterExtractor pathParameterExtractor;

    private JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        when(tppService.getTppInfo()).thenReturn(jsonReader.getObjectFromFile(TPP_INFO_JSON, TppInfo.class));
    }

    @Test
    void preHandle_pathVariableIsNull() {
        when(pathParameterExtractor.extractParameters(any(HttpServletRequest.class))).thenReturn(null);

        when(request.getRemoteAddr()).thenReturn(TPP_IP);
        when(request.getRequestURI()).thenReturn(REQUEST_URI);

        interceptor.preHandle(request, response, null);

        verify(pathParameterExtractor).extractParameters(any(HttpServletRequest.class));
        verify(tppService).getTppInfo();
        verify(request).getRemoteAddr();
        verify(request).getRequestURI();
    }

    @Test
    void preHandle_success() {
        when(pathParameterExtractor.extractParameters(any(HttpServletRequest.class))).thenReturn(Collections.emptyMap());

        when(request.getRemoteAddr()).thenReturn(TPP_IP);
        when(request.getRequestURI()).thenReturn(REQUEST_URI);

        interceptor.preHandle(request, response, null);

        verify(pathParameterExtractor).extractParameters(any(HttpServletRequest.class));
        verify(tppService).getTppInfo();
        verify(request).getRemoteAddr();
        verify(request).getRequestURI();
    }

    @Test
    void afterCompletion() {
        when(response.getStatus()).thenReturn(HttpServletResponse.SC_OK);
        when(redirectIdService.getRedirectId()).thenReturn(REDIRECT_ID);

        interceptor.afterCompletion(request, response, null, null);

        verify(tppService).getTppInfo();
        verify(response).getStatus();
        verify(redirectIdService).getRedirectId();
        verify(loggingContextService).getTransactionStatus();
        verify(loggingContextService).getScaStatus();
        verify(loggingContextService, never()).getConsentStatus();
    }
}
