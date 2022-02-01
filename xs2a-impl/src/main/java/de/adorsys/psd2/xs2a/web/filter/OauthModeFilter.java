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

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.Xs2aEndpointChecker;
import de.adorsys.psd2.xs2a.web.error.TppErrorMessageWriter;
import de.adorsys.psd2.xs2a.web.request.RequestPathResolver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static de.adorsys.psd2.xs2a.core.domain.MessageCategory.ERROR;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORBIDDEN;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.UNAUTHORIZED_NO_TOKEN;


@Slf4j
@Component
public class OauthModeFilter extends AbstractXs2aFilter {
    // Map which defines XS2A endpoints, which can use OAuth2 authorisation and their POST and DELETE HTTP methods.
    private static final Map<String, List<String>> OAUTH2_ENDPOINTS_WITH_METHODS = new HashMap<>();

    // List which defines XS2A endpoints for receiving any information with OAuth2 authorisation.
    private static final List<String> OAUTH2_GET_ENDPOINTS_WITH_METHODS = new ArrayList<>();

    private static final String HTTP_METHOD_DELETE = "DELETE";
    private static final String HTTP_METHOD_POST = "POST";

    static {
        OAUTH2_ENDPOINTS_WITH_METHODS.put("/v1/payments", Arrays.asList(HTTP_METHOD_POST, HTTP_METHOD_DELETE));
        OAUTH2_ENDPOINTS_WITH_METHODS.put("/v1/bulk-payments", Arrays.asList(HTTP_METHOD_POST, HTTP_METHOD_DELETE));
        OAUTH2_ENDPOINTS_WITH_METHODS.put("/v1/periodic-payments", Arrays.asList(HTTP_METHOD_POST, HTTP_METHOD_DELETE));
        OAUTH2_ENDPOINTS_WITH_METHODS.put("/v1/consents", Collections.singletonList(HTTP_METHOD_POST));
        OAUTH2_ENDPOINTS_WITH_METHODS.put("/v2/consents/confirmation-of-funds", Collections.singletonList(HTTP_METHOD_POST));

        OAUTH2_GET_ENDPOINTS_WITH_METHODS.add("/v1/payments");
        OAUTH2_GET_ENDPOINTS_WITH_METHODS.add("/v1/bulk-payments");
        OAUTH2_GET_ENDPOINTS_WITH_METHODS.add("/v1/periodic-payments");
        OAUTH2_GET_ENDPOINTS_WITH_METHODS.add("/v1/consents");
        OAUTH2_GET_ENDPOINTS_WITH_METHODS.add("/v2/consents/confirmation-of-funds");
    }

    private final AspspProfileServiceWrapper aspspProfileService;
    private final RequestProviderService requestProviderService;
    private final ScaApproachResolver scaApproachResolver;
    private final TppErrorMessageWriter tppErrorMessageWriter;
    private final RequestPathResolver requestPathResolver;

    public OauthModeFilter(TppErrorMessageWriter tppErrorMessageWriter, AspspProfileServiceWrapper aspspProfileService, RequestProviderService requestProviderService, ScaApproachResolver scaApproachResolver, TppErrorMessageWriter tppErrorMessageWriter1, RequestPathResolver requestPathResolver, Xs2aEndpointChecker xs2aEndpointChecker) {
        super(tppErrorMessageWriter, xs2aEndpointChecker);
        this.aspspProfileService = aspspProfileService;
        this.requestProviderService = requestProviderService;
        this.scaApproachResolver = scaApproachResolver;
        this.tppErrorMessageWriter = tppErrorMessageWriter1;
        this.requestPathResolver = requestPathResolver;
    }

    @Override
    protected void doFilterInternalCustom(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (shouldFilterInternal(request)) {
            if (isRedirectApproachWithGivenOauthType(ScaRedirectFlow.OAUTH_PRE_STEP) && requestProviderService.getOAuth2Token() == null) {
                log.info("OAuth pre-step selected, no authorisation header is present in the request");
                tppErrorMessageWriter.writeError(response, new TppErrorMessage(ERROR, UNAUTHORIZED_NO_TOKEN, aspspProfileService.getOauthConfigurationUrl()));
                return;
            }

            if (isRedirectApproachWithGivenOauthType(ScaRedirectFlow.OAUTH) && StringUtils.isNotBlank(requestProviderService.getOAuth2Token())) {
                log.info("OAuth integrated selected, authorisation header is present in the request");
                tppErrorMessageWriter.writeError(response, new TppErrorMessage(ERROR, FORBIDDEN));
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean shouldFilterInternal(HttpServletRequest request) {
        return !shouldNotFilterInternal(request);
    }

    private boolean shouldNotFilterInternal(HttpServletRequest request) {

        String requestPath = requestPathResolver.resolveRequestPath(request);

        if (isRedirectApproachWithGivenOauthType(ScaRedirectFlow.OAUTH_PRE_STEP) && request.getMethod().equals("GET")) {
            return OAUTH2_GET_ENDPOINTS_WITH_METHODS
                       .stream()
                       .noneMatch(requestPath::startsWith);
        }

        return OAUTH2_ENDPOINTS_WITH_METHODS.entrySet()
                   .stream()
                   .filter(entry -> requestPath.startsWith(entry.getKey()))
                   .noneMatch(entry -> entry.getValue().contains(request.getMethod()));
    }

    private boolean isRedirectApproachWithGivenOauthType(ScaRedirectFlow scaRedirectFlow) {
        return scaApproachResolver.resolveScaApproach() == ScaApproach.REDIRECT
                   && aspspProfileService.getScaRedirectFlow() == scaRedirectFlow;
    }
}
