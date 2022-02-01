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
