/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract filter that will be executed only once and will be applied only to XS2A endpoints.
 */
public abstract class AbstractXs2aFilter extends OncePerRequestFilter {
    private static final List<String> XS2A_ENDPOINTS = Arrays.asList("/v1/accounts",
                                                                     "/v1/consents",
                                                                     "/v1/funds-confirmations",
                                                                     "/v1/payments",
                                                                     "/v1/bulk-payments",
                                                                     "/v1/periodic-payments",
                                                                     "/v1/signing-baskets");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        return XS2A_ENDPOINTS.stream()
                   .noneMatch(servletPath::startsWith);
    }
}
