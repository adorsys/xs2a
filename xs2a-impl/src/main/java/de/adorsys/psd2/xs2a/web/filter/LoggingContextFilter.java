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

package de.adorsys.psd2.xs2a.web.filter;

import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.context.LoggingContextService;
import de.adorsys.psd2.xs2a.web.error.TppErrorMessageWriter;
import de.adorsys.psd2.xs2a.web.request.RequestPathResolver;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter for managing logging context for each request.
 * Responsible for populating logging context with request-related data and clearing context afterwards.
 */
@Component
public class LoggingContextFilter extends AbstractXs2aFilter {
    private final LoggingContextService loggingContextService;
    private final RequestProviderService requestProviderService;

    public LoggingContextFilter(TppErrorMessageWriter tppErrorMessageWriter,
                                RequestPathResolver requestPathResolver,
                                LoggingContextService loggingContextService,
                                RequestProviderService requestProviderService) {
        super(tppErrorMessageWriter, requestPathResolver);
        this.loggingContextService = loggingContextService;
        this.requestProviderService = requestProviderService;
    }

    @Override
    protected void doFilterInternalCustom(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        loggingContextService.storeRequestInformation(requestProviderService.getInternalRequestIdString(), requestProviderService.getRequestIdString());
        try {
            doFilter(request, response, filterChain);
        } finally {
            loggingContextService.clearContext();
        }
    }
}
