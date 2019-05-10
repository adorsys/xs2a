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

package de.adorsys.psd2.xs2a.web.header;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseHeaders {
    @NotNull
    private final HttpHeaders httpHeaders;

    public static ResponseHeadersBuilder builder() {
        return new ResponseHeadersBuilder();
    }

    public static class ResponseHeadersBuilder {
        private static final String SCA = "Aspsp-Sca-Approach";
        private static final String LOCATION = "Location";
        private HttpHeaders httpHeaders = new HttpHeaders();

        public ResponseHeadersBuilder aspspScaApproach(@NotNull ScaApproach scaApproach) {
            this.httpHeaders.add(SCA, scaApproach.name());
            return this;
        }

        public ResponseHeadersBuilder location(@NotNull String selfLink) {
            this.httpHeaders.add(LOCATION, selfLink);
            return this;
        }

        public ResponseHeaders build() {
            return new ResponseHeaders(httpHeaders);
        }
    }
}
