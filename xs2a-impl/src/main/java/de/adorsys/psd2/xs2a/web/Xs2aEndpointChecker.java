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

package de.adorsys.psd2.xs2a.web;

import de.adorsys.psd2.xs2a.config.Xs2aEndpointPathConstant;
import de.adorsys.psd2.xs2a.web.request.RequestPathResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class Xs2aEndpointChecker {
    private static final AntPathMatcher matcher = new AntPathMatcher();
    private final RequestPathResolver requestPathResolver;

    public boolean isXs2aEndpoint(HttpServletRequest request) {
        String requestPath = requestPathResolver.resolveRequestPath(request);

        return Stream.of(Xs2aEndpointPathConstant.getAllXs2aEndpointPaths())
                   .anyMatch(en -> matcher.match(en, requestPath));
    }
}
