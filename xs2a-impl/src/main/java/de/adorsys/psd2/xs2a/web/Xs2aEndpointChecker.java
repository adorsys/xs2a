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
