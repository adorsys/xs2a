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
