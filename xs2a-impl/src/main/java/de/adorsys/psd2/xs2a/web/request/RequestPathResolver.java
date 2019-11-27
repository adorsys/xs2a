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

package de.adorsys.psd2.xs2a.web.request;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;

@Component
public class RequestPathResolver {
    private static final UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();

    /**
     * Returns string representation of request path for the given request, without context path
     *
     * @param httpServletRequest request to extract path from
     * @return request path
     */
    public String resolveRequestPath(HttpServletRequest httpServletRequest) {
        return URL_PATH_HELPER.getPathWithinApplication(httpServletRequest);
    }
}
