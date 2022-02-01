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

import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.component.logger.TppLogger;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.web.PathParameterExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ConsentLoggingInterceptor extends HandlerInterceptorAdapter {
    private static final String NOT_EXIST_IN_URI = "Not exist in URI";
    private final TppService tppService;
    private final RedirectIdService redirectIdService;
    private final LoggingContextService loggingContextService;
    private final PathParameterExtractor pathParameterExtractor;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Map<String, String> pathVariables = pathParameterExtractor.extractParameters(request);
        String consentId = Optional.ofNullable(pathVariables)
                               .map(pv -> pv.get("consentId"))
                               .orElse(NOT_EXIST_IN_URI);

        TppLogger.logRequest(request)
            .withTpp(tppService.getTppInfo())
            .withRequestUri()
            .withParam("Consent ID", consentId)
            .perform();

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TppLogger.logResponse(response)
            .withTpp(tppService.getTppInfo())
            .withResponseStatus()
            .withOptionalRedirectId(redirectIdService.getRedirectId())
            .withParam("consentStatus", loggingContextService.getConsentStatus())
            .withParam("scaStatus", loggingContextService.getScaStatus())
            .perform();
    }
}
