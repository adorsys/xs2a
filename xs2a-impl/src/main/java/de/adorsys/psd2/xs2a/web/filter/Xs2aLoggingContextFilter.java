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
import de.adorsys.psd2.xs2a.web.error.TppErrorMessageWriter;
import org.springframework.stereotype.Component;

import javax.annotation.Priority;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter for managing logging context for each request.
 * Responsible for populating logging context with request-related data and clearing context afterwards.
 */
@Priority(0)
@Component
public class Xs2aLoggingContextFilter extends AbstractXs2aFilter {
    private final LoggingContextService loggingContextService;
    private final RequestProviderService requestProviderService;

    public Xs2aLoggingContextFilter(TppErrorMessageWriter tppErrorMessageWriter,
                                    Xs2aEndpointChecker xs2aEndpointChecker,
                                    LoggingContextService loggingContextService,
                                    RequestProviderService requestProviderService) {
        super(tppErrorMessageWriter, xs2aEndpointChecker);
        this.loggingContextService = loggingContextService;
        this.requestProviderService = requestProviderService;
    }

    @Override
    protected void doFilterInternalCustom(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        RequestInfo requestInfo = new RequestInfo(requestProviderService.getInternalRequestIdString(), requestProviderService.getRequestIdString(), requestProviderService.getInstanceId());
        loggingContextService.storeRequestInformation(requestInfo);

        try {
            doFilter(request, response, filterChain);
        } finally {
            loggingContextService.clearContext();
        }
    }
}
