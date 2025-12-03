/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.web.header;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class VopResponseHeaders {
    @NotNull
    private final HttpHeaders httpHeaders;

    public static VopResponseHeadersBuilder builder() {
        return new VopResponseHeadersBuilder();
    }

    public static class VopResponseHeadersBuilder {
        private static final String REFERENCE_API_NAME = "X-Reference-API-Name";
        private static final String REFERENCE_API_DOCUMENT = "X-Reference-API-Document";
        private static final String REFERENCE_API_VERSION = "X-Reference-API-Version";
        private static final String REQUEST_ID = "X-Request-ID";

        private HttpHeaders httpHeaders = new HttpHeaders();

        public VopResponseHeadersBuilder referenceApiName(@NotNull String referenceApiName) {
            if (StringUtils.isNotBlank(referenceApiName)) {
                this.httpHeaders.add(REFERENCE_API_NAME, referenceApiName);
            }
            return this;
        }

        public VopResponseHeadersBuilder referenceApiDocument(@NotNull String referenceApiDocument) {
            if (StringUtils.isNotBlank(referenceApiDocument)) {
                this.httpHeaders.add(REFERENCE_API_DOCUMENT, referenceApiDocument);
            }
            return this;
        }

        public VopResponseHeadersBuilder referenceApiVersion(@NotNull String referenceApiVersion) {
            if (StringUtils.isNotBlank(referenceApiVersion)) {
                this.httpHeaders.add(REFERENCE_API_VERSION, referenceApiVersion);
            }
            return this;
        }

        public VopResponseHeadersBuilder requestId(@NotNull String requestId) {
            if (StringUtils.isNotBlank(requestId)) {
                this.httpHeaders.add(REQUEST_ID, requestId);
            }
            return this;
        }

        public VopResponseHeaders build() {
            return new VopResponseHeaders(httpHeaders);
        }
    }
}

