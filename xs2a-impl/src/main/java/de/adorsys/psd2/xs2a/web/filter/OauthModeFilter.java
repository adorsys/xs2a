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
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.error.TppErrorMessageBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORBIDDEN;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.UNAUTHORIZED_NO_TOKEN;
import static de.adorsys.psd2.xs2a.exception.MessageCategory.ERROR;


@Slf4j
@Component
@RequiredArgsConstructor
public class OauthModeFilter extends OncePerRequestFilter {

    // Map which defines XS2A endpoints, which can use OAuth2 authorisation and their POST and DELETE HTTP methods.
    private static final Map<String, List<String>> OAUTH2_ENDPOINTS_WITH_METHODS = new HashMap<>();

    // List which defines XS2A endpoints for receiving any information with OAuth2 authorisation.
    private static final List<String> OAUTH2_GET_ENDPOINTS_WITH_METHODS = new ArrayList<>();

    static {
        OAUTH2_ENDPOINTS_WITH_METHODS.put("/v1/payments", Arrays.asList("POST", "DELETE"));
        OAUTH2_ENDPOINTS_WITH_METHODS.put("/v1/bulk-payments", Arrays.asList("POST", "DELETE"));
        OAUTH2_ENDPOINTS_WITH_METHODS.put("/v1/periodic-payments", Arrays.asList("POST", "DELETE"));
        OAUTH2_ENDPOINTS_WITH_METHODS.put("/v1/consents", Collections.singletonList("POST"));

        OAUTH2_GET_ENDPOINTS_WITH_METHODS.add("/v1/payments");
        OAUTH2_GET_ENDPOINTS_WITH_METHODS.add("/v1/bulk-payments");
        OAUTH2_GET_ENDPOINTS_WITH_METHODS.add("/v1/periodic-payments");
        OAUTH2_GET_ENDPOINTS_WITH_METHODS.add("/v1/consents");
    }

    private final AspspProfileServiceWrapper aspspProfileService;
    private final RequestProviderService requestProviderService;
    private final TppErrorMessageBuilder tppErrorMessageBuilder;
    private final ScaApproachResolver scaApproachResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (isRedirectApproachWithGivenOauthType(ScaRedirectFlow.OAUTH_PRE_STEP) && requestProviderService.getOAuth2Token() == null) {

            log.info("InR-ID: [{}], X-Request-ID: [{}], OAuth pre-step selected, no authorisation header is present in the request",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print(tppErrorMessageBuilder.buildTppErrorMessageWithPlaceholder(ERROR, UNAUTHORIZED_NO_TOKEN, aspspProfileService.getOauthConfigurationUrl()));

            return;
        }

        if (isRedirectApproachWithGivenOauthType(ScaRedirectFlow.OAUTH) && StringUtils.isNotBlank(requestProviderService.getOAuth2Token())) {

            log.info("InR-ID: [{}], X-Request-ID: [{}], OAuth integrated selected, authorisation header is present in the request",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId());

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().print(tppErrorMessageBuilder.buildTppErrorMessage(ERROR, FORBIDDEN));

            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String servletPath = request.getServletPath();

        if (isRedirectApproachWithGivenOauthType(ScaRedirectFlow.OAUTH_PRE_STEP) && request.getMethod().equals("GET")) {
            return OAUTH2_GET_ENDPOINTS_WITH_METHODS
                       .stream()
                       .noneMatch(servletPath::startsWith);
        }

        return OAUTH2_ENDPOINTS_WITH_METHODS.entrySet()
                   .stream()
                   .filter(entry -> servletPath.startsWith(entry.getKey()))
                   .noneMatch(entry -> entry.getValue().contains(request.getMethod()));
    }

    private boolean isRedirectApproachWithGivenOauthType(ScaRedirectFlow scaRedirectFlow) {
        return scaApproachResolver.resolveScaApproach() == ScaApproach.REDIRECT
                   && aspspProfileService.getScaRedirectFlow() == scaRedirectFlow;
    }

}
