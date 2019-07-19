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

package de.adorsys.psd2.xs2a.web.interceptor.logging;

import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SigningBasketLoggingInterceptorTest {
    private static final String TPP_IP = "1.1.1.1";
    private static final String TPP_INFO_JSON = "json/web/interceptor/logging/tpp-info.json";
    private static final String REQUEST_URI = "request_uri";
    private static final String X_REQUEST_ID_HEADER_NAME = "x-request-id";
    private static final String X_REQUEST_ID_HEADER_VALUE = "222";
    private static final String REDIRECT_ID = "redirect-id";

    @InjectMocks
    private SigningBasketLoggingInterceptor interceptor;
    @Mock
    private TppService tppService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private RedirectIdService redirectIdService;

    private JsonReader jsonReader = new JsonReader();

    @Before
    public void setUp() {
        when(response.getHeader(X_REQUEST_ID_HEADER_NAME)).thenReturn(X_REQUEST_ID_HEADER_VALUE);
        when(tppService.getTppInfo()).thenReturn(jsonReader.getObjectFromFile(TPP_INFO_JSON, TppInfo.class));
    }

    @Test
    public void preHandle_success() {
        Map<Object, Object> pathVariables = new HashMap<>();
        when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathVariables);
        when(request.getRemoteAddr()).thenReturn(TPP_IP);
        when(request.getRequestURI()).thenReturn(REQUEST_URI);

        interceptor.preHandle(request, response, null);

        verify(request, times(1)).getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        verify(tppService, times(1)).getTppInfo();
        verify(request, times(1)).getHeader(eq(X_REQUEST_ID_HEADER_NAME));
        verify(request, times(1)).getRemoteAddr();
        verify(request, times(1)).getRequestURI();
    }

    @Test
    public void preHandle_pathVariableIsNull() {
        when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(null);
        when(tppService.getTppInfo()).thenReturn(jsonReader.getObjectFromFile(TPP_INFO_JSON, TppInfo.class));
        when(request.getRemoteAddr()).thenReturn(TPP_IP);
        when(request.getRequestURI()).thenReturn(REQUEST_URI);

        interceptor.preHandle(request, response, null);

        verify(request, times(1)).getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        verify(tppService, times(1)).getTppInfo();
        verify(request, times(1)).getHeader(eq(X_REQUEST_ID_HEADER_NAME));
        verify(request, times(1)).getRemoteAddr();
        verify(request, times(1)).getRequestURI();
    }

    @Test
    public void afterCompletion_success() {
        when(response.getStatus()).thenReturn(HttpServletResponse.SC_OK);
        when(redirectIdService.getRedirectId()).thenReturn(REDIRECT_ID);

        interceptor.afterCompletion(request, response, null, null);

        verify(tppService, times(1)).getTppInfo();
        verify(response, times(1)).getHeader(eq(X_REQUEST_ID_HEADER_NAME));
        verify(response, times(1)).getStatus();
        verify(redirectIdService).getRedirectId();
    }
}
