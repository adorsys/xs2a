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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.RequestData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestProviderService {
    private static final String TPP_REDIRECT_PREFERRED_HEADER = "tpp-redirect-preferred";
    private static final String X_REQUEST_ID_HEADER = "x-request-id";
    private static final String PSU_ID_HEADER = "psu-id";
    private static final String PSU_ID_TYPE_HEADER = "psu-id-type";
    private static final String PSU_CORPORATE_ID_HEADER = "psu-corporate-id";
    private static final String PSU_CORPORATE_ID_TYPE_HEADER = "psu-corporate-id-type";

    private final HttpServletRequest httpServletRequest;

    public Optional<Boolean> resolveTppRedirectPreferred() {
        Map<String, String> headers = getRequestData().getHeaders();
        if (headers == null || !headers.containsKey(TPP_REDIRECT_PREFERRED_HEADER)) {
            return Optional.empty();
        }
        return Optional.of(Boolean.valueOf(headers.get(TPP_REDIRECT_PREFERRED_HEADER)));
    }

    public RequestData getRequestData() {
        String uri = httpServletRequest.getRequestURI();
        UUID requestId = UUID.fromString(getHeader(X_REQUEST_ID_HEADER));
        String ip = httpServletRequest.getRemoteAddr();
        Map<String, String> headers = getRequestHeaders(httpServletRequest);
        PsuIdData psuIdData = getPsuIdData();

        return new RequestData(uri, requestId, ip, headers, psuIdData);
    }

    public PsuIdData getPsuIdData() {
        return new PsuIdData(getHeader(PSU_ID_HEADER),
                             getHeader(PSU_ID_TYPE_HEADER),
                             getHeader(PSU_CORPORATE_ID_HEADER),
                             getHeader(PSU_CORPORATE_ID_TYPE_HEADER));
    }

    private String getHeader(String headerName) {
        return httpServletRequest.getHeader(headerName);
    }

    public UUID getRequestId() {
        return getRequestData().getRequestId();
    }

    private Map<String, String> getRequestHeaders(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames())
                   .stream()
                   .collect(Collectors.toMap(Function.identity(), request::getHeader));
    }
}
