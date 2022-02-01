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

package de.adorsys.psd2.logger.web;

import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.logger.context.RequestInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Abstract Filter for managing logging context for each request.
 * Responsible for populating logging context with request-related data from XS2A and clearing context afterwards.
 */
@RequiredArgsConstructor
public abstract class AbstractLoggingContextFilter extends OncePerRequestFilter {
    private static final String INTERNAL_REQUEST_ID_HEADER_NAME = "X-Internal-Request-ID";
    private static final String X_REQUEST_ID_HEADER_NAME = "X-Request-ID";
    private static final String INSTANCE_ID_HEADER_NAME = "Instance-ID";
    private static final UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();

    private final LoggingContextService loggingContextService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        RequestInfo requestInfo = new RequestInfo(request.getHeader(INTERNAL_REQUEST_ID_HEADER_NAME),
                                                  request.getHeader(X_REQUEST_ID_HEADER_NAME),
                                                  request.getHeader(INSTANCE_ID_HEADER_NAME));
        loggingContextService.storeRequestInformation(requestInfo);

        try {
            doFilter(request, response, filterChain);
        } finally {
            loggingContextService.clearContext();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String pathWithinApplication = URL_PATH_HELPER.getPathWithinApplication(request);
        return !pathWithinApplication.startsWith(getEndpointsPrefix());
    }

    /**
     * Returns prefix used for all endpoints that should trigger this filter.
     * <p>
     * If requested path doesn't start with the specified prefix, this filter will be skipped.
     *
     * @return endpoints prefix
     */
    protected abstract String getEndpointsPrefix();
}
