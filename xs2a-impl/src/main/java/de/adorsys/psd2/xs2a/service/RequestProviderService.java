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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.xs2a.domain.RequestData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestProviderService {
    private final HttpServletRequest httpServletRequest;

    public RequestData getRequestData() {
        String uri = httpServletRequest.getRequestURI();
        UUID requestId = UUID.fromString(httpServletRequest.getHeader("X-Request-ID"));
        String ip = httpServletRequest.getRemoteAddr();
        Map<String, String> headers = getRequestHeaders(httpServletRequest);

        return new RequestData(uri, requestId, ip, headers);
    }

    private Map<String, String> getRequestHeaders(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames())
                   .stream()
                   .collect(Collectors.toMap(Function.identity(), request::getHeader));
    }
}
