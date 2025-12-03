/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.web.interceptor.logging;

import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.component.logger.TppLogger;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.web.PathParameterExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.Optional;

/**
 * VOP-specific logging interceptor that handles request/response logging for Verification of Party endpoints.
 * This interceptor follows the same pattern as other logging interceptors in the XS2A framework
 * but provides VOP-specific context and parameters.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class VopLoggingInterceptor implements HandlerInterceptor {

    private static final String NOT_EXIST_IN_URI = "Not exist in URI";

    private final TppService tppService;
    private final LoggingContextService loggingContextService;
    private final PathParameterExtractor pathParameterExtractor;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Extract endpoint information from request URI since VopController uses explicit paths
        String requestUri = request.getRequestURI();
        Map<String, String> pathVariables = pathParameterExtractor.extractParameters(request);

        // Extract single/bulk party verification endpoint from URI
        String partyVerificationEndpoint = extractEndpointFromUri(requestUri,
                                                                   "payee-verifications",
                                                                   "payer-verifications",
                                                                   "party-verifications");
        String bulkPartyVerificationEndpoint = extractEndpointFromUri(requestUri,
                                                                       "bulk-payee-verifications",
                                                                       "bulk-payer-verifications",
                                                                       "bulk-party-verifications");

        // Extract vopBulkId from path variables if present (used in GET requests)
        String vopBulkId = Optional.ofNullable(pathVariables)
                               .map(vr -> vr.get("vopBulkId"))
                               .orElse(NOT_EXIST_IN_URI);

        TppLogger.logRequest(request)
            .withTpp(tppService.getTppInfo())
            .withRequestUri()
            .withParam("Party Verification Endpoint", partyVerificationEndpoint)
            .withParam("Bulk Party Verification Endpoint", bulkPartyVerificationEndpoint)
            .withParam("VOP Bulk ID", vopBulkId)
            .perform();

        return true;
    }

    /**
     * Extracts the endpoint name from the request URI by checking if it contains any of the provided endpoint names.
     *
     * @param requestUri the full request URI
     * @param endpointNames the possible endpoint names to match
     * @return the matched endpoint name or NOT_EXIST_IN_URI if no match found
     */
    private String extractEndpointFromUri(String requestUri, String... endpointNames) {
        for (String endpointName : endpointNames) {
            if (requestUri.contains("/" + endpointName)) {
                return endpointName;
            }
        }
        return NOT_EXIST_IN_URI;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TppLogger.logResponse(response)
            .withTpp(tppService.getTppInfo())
            .withResponseStatus()
            .withParam("Response Location", Optional.ofNullable(response.getHeader("Location"))
                .orElse(NOT_EXIST_IN_URI))
            .withParam("vopBulkStatus", loggingContextService.getVopBulkStatus())
            .withParam("vopBulkId", loggingContextService.getVopBulkId())
            .perform();
    }
}
