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
