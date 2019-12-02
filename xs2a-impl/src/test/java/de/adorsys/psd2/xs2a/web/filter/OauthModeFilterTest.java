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

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.exception.MessageCategory;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.error.TppErrorMessageBuilder;
import de.adorsys.psd2.xs2a.web.error.TppErrorMessageWriter;
import de.adorsys.psd2.xs2a.web.request.RequestPathResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORBIDDEN;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.UNAUTHORIZED_NO_TOKEN;
import static de.adorsys.psd2.xs2a.exception.MessageCategory.ERROR;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OauthModeFilterTest {
    private static final String TOKEN = "Bearer 111111";
    private static final String IDP_URL = "http://localhost:4200/idp/";
    private static final String XS2A_PATH = "/v1/payments";
    private static final String CUSTOM_PATH = "/custom-endpoint";

    private static final TppErrorMessage TPP_ERROR_MESSAGE_UNAUTHORIZED = new TppErrorMessage(ERROR, UNAUTHORIZED_NO_TOKEN, String.format("Please retrieve token first from %s", IDP_URL));
    private static final TppErrorMessage TPP_ERROR_MESSAGE_FORBIDDEN = new TppErrorMessage(ERROR, FORBIDDEN, "Token is not valid for the addressed service/resource");
    private static final String HTTP_METHOD = HttpMethod.POST.name();

    @InjectMocks
    private OauthModeFilter oauthModeFilter;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private PrintWriter printWriter;
    @Mock
    private FilterChain chain;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private TppErrorMessageBuilder tppErrorMessageBuilder;
    @Mock
    private TppErrorMessageWriter tppErrorMessageWriter;
    @Mock
    private AspspProfileServiceWrapper aspspProfileService;
    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private RequestPathResolver requestPathResolver;

    @Before
    public void init() {
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(ScaApproach.REDIRECT);
        when(requestPathResolver.resolveRequestPath(request))
            .thenReturn(XS2A_PATH);
        when(request.getMethod())
            .thenReturn(HTTP_METHOD);
    }

    @Test
    public void doFilter_success() throws IOException, ServletException {
        // When
        oauthModeFilter.doFilter(request, response, chain);

        // Then
        verify(chain).doFilter(any(), any());
    }

    @Test
    public void doFilter_preStepWithoutToken_shouldReturnUnauthorised() throws IOException, ServletException {
        // Given
        when(aspspProfileService.getOauthConfigurationUrl())
            .thenReturn(IDP_URL);
        when(aspspProfileService.getScaRedirectFlow())
            .thenReturn(ScaRedirectFlow.OAUTH_PRE_STEP);
        when(tppErrorMessageBuilder.buildTppErrorMessageWithPlaceholder(MessageCategory.ERROR, UNAUTHORIZED_NO_TOKEN, IDP_URL))
            .thenReturn(TPP_ERROR_MESSAGE_UNAUTHORIZED);

        ArgumentCaptor<Integer> statusCode = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<TppErrorMessage> message = ArgumentCaptor.forClass(TppErrorMessage.class);

        // When
        oauthModeFilter.doFilter(request, response, chain);

        // Then
        verify(tppErrorMessageWriter).writeError(eq(response), statusCode.capture(), message.capture());
        verify(chain, never()).doFilter(any(), any());
        assertEquals((Integer) 401, statusCode.getValue());
        assertEquals(TPP_ERROR_MESSAGE_UNAUTHORIZED, message.getValue());
    }

    @Test
    public void doFilter_integratedWithToken_shouldReturnForbidden() throws IOException, ServletException {
        // Given
        when(requestProviderService.getOAuth2Token())
            .thenReturn(TOKEN);
        when(aspspProfileService.getScaRedirectFlow())
            .thenReturn(ScaRedirectFlow.OAUTH);
        when(tppErrorMessageBuilder.buildTppErrorMessage(MessageCategory.ERROR, FORBIDDEN))
            .thenReturn(TPP_ERROR_MESSAGE_FORBIDDEN);

        ArgumentCaptor<Integer> statusCode = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<TppErrorMessage> message = ArgumentCaptor.forClass(TppErrorMessage.class);

        // When
        oauthModeFilter.doFilter(request, response, chain);

        // Then
        verify(tppErrorMessageWriter).writeError(eq(response), statusCode.capture(), message.capture());
        verify(chain, never()).doFilter(any(), any());
        assertEquals((Integer) 403, statusCode.getValue());
        assertEquals(TPP_ERROR_MESSAGE_FORBIDDEN, message.getValue());
    }

    @Test
    public void doFilter_onCustomEndpointAndRedirectFlow_shouldSkipFilter() throws ServletException, IOException {
        // Given
        when(requestPathResolver.resolveRequestPath(request))
            .thenReturn(CUSTOM_PATH);

        // When
        oauthModeFilter.doFilter(request, response, chain);

        // Then
        verify(chain).doFilter(request, response);
        verifyZeroInteractions(tppErrorMessageWriter, requestProviderService);
    }

    @Test
    public void doFilter_onCustomEndpointAndOauthPreStepFlow_shouldSkipFilter() throws ServletException, IOException {
        // Given
        when(requestPathResolver.resolveRequestPath(request))
            .thenReturn(CUSTOM_PATH);
        when(aspspProfileService.getScaRedirectFlow())
            .thenReturn(ScaRedirectFlow.OAUTH_PRE_STEP);
        when(request.getMethod())
            .thenReturn(HttpMethod.GET.name());

        // When
        oauthModeFilter.doFilter(request, response, chain);

        // Then
        verify(chain).doFilter(request, response);
        verifyZeroInteractions(tppErrorMessageWriter, requestProviderService);
    }
}
