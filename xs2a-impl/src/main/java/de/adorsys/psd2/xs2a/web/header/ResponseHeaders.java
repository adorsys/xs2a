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

package de.adorsys.psd2.xs2a.web.header;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
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
        private static final String NOTIFICATION_SUPPORT = "ASPSP-Notification-Support";
        private static final String NOTIFICATION_CONTENT = "ASPSP-Notification-Content";

        private HttpHeaders httpHeaders = new HttpHeaders();

        public ResponseHeadersBuilder aspspScaApproach(@NotNull ScaApproach scaApproach) {
            this.httpHeaders.add(SCA, scaApproach.name());
            return this;
        }

        public ResponseHeadersBuilder location(@NotNull String selfLink) {
            this.httpHeaders.add(LOCATION, selfLink);
            return this;
        }

        public ResponseHeadersBuilder notificationSupport(Boolean notificationSupport) {
            if (notificationSupport != null){
                this.httpHeaders.add(NOTIFICATION_SUPPORT, notificationSupport.toString());
            }
            return this;
        }

        public ResponseHeadersBuilder notificationContent(String notificationContent) {
            if (StringUtils.isNoneBlank(notificationContent)) {
                this.httpHeaders.add(NOTIFICATION_CONTENT, notificationContent);
            }
            return this;
        }

        public ResponseHeaders build() {
            return new ResponseHeaders(httpHeaders);
        }
    }
}
