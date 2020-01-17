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

package de.adorsys.psd2.aspsp.profile.web.filter;

import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.logger.web.AbstractLoggingContextFilter;
import org.springframework.stereotype.Component;

/**
 * Filter for clearing logging context after each request.
 */
@Component
public class AspspProfileLoggingContextFilter extends AbstractLoggingContextFilter {
    private static final String ASPSP_PROFILE_ENDPOINTS_PREFIX = "/api/v1/aspsp-profile";

    public AspspProfileLoggingContextFilter(LoggingContextService loggingContextService) {
        super(loggingContextService);
    }

    @Override
    protected String getEndpointsPrefix() {
        return ASPSP_PROFILE_ENDPOINTS_PREFIX;
    }
}
