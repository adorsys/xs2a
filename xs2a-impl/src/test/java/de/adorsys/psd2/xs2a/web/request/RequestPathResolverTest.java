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

package de.adorsys.psd2.xs2a.web.request;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestPathResolverTest {
    private static final String HTTP_METHOD = HttpMethod.GET.name();

    private RequestPathResolver requestPathResolver = new RequestPathResolver();

    @Test
    void resolveRequestPath() {
        // Given
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest(HTTP_METHOD, "/v1/consents");

        // When
        String requestPath = requestPathResolver.resolveRequestPath(mockHttpServletRequest);

        // Then
        assertEquals("/v1/consents", requestPath);
    }

    @Test
    void resolveRequestPath_withContextPath_shouldReturnPathWithoutContext() {
        // Given
        String contextPath = "/custom-context";
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest(HttpMethod.GET.name(), contextPath + "/v1/consents");
        mockHttpServletRequest.setContextPath(contextPath);

        // When
        String requestPath = requestPathResolver.resolveRequestPath(mockHttpServletRequest);

        // Then
        assertEquals("/v1/consents", requestPath);
    }
}
