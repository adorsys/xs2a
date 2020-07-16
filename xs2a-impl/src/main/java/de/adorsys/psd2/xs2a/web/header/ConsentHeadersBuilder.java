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
import de.adorsys.psd2.xs2a.domain.NotificationModeResponseHeaders;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConsentHeadersBuilder extends AbstractHeadersBuilder {

    @Autowired
    public ConsentHeadersBuilder(ScaApproachResolver scaApproachResolver) {
        super(scaApproachResolver);
    }

    /**
     * Builds response headers for successful create consent request
     *
     * @param authorisationId id of the authorisation, if it was created implicitly
     * @param selfLink        link to the newly created consent
     * @param notificationHeaders {@link NotificationModeResponseHeaders} notification headers
     * @return response headers
     */
    public ResponseHeaders buildCreateConsentHeaders(@Nullable String authorisationId, @NotNull String selfLink, @NotNull NotificationModeResponseHeaders notificationHeaders) {
        ResponseHeaders.ResponseHeadersBuilder responseHeadersBuilder = ResponseHeaders.builder();
        buildNotificationHeaders(responseHeadersBuilder, notificationHeaders);
        buildCreateConsentHeaders(responseHeadersBuilder, authorisationId, selfLink);
        return responseHeadersBuilder.build();
    }

    private void buildNotificationHeaders(ResponseHeaders.ResponseHeadersBuilder builder, @NotNull NotificationModeResponseHeaders notificationHeaders) {
        builder.notificationSupport(notificationHeaders.getAspspNotificationSupport());
        builder.notificationContent(notificationHeaders.getAspspNotificationContent());
    }

    private void buildCreateConsentHeaders(ResponseHeaders.ResponseHeadersBuilder builder, @Nullable String authorisationId, @NotNull String selfLink) {
        builder.location(selfLink);
        if (authorisationId != null) {
            ScaApproach scaApproach = scaApproachResolver.getScaApproach(authorisationId);
            builder.aspspScaApproach(scaApproach);
        }
    }

    public ResponseHeaders buildCreateConsentHeaders(@Nullable String authorisationId, @NotNull String selfLink) {
        ResponseHeaders.ResponseHeadersBuilder responseHeadersBuilder = ResponseHeaders.builder();
        buildCreateConsentHeaders(responseHeadersBuilder, authorisationId, selfLink);
        return responseHeadersBuilder.build();
    }

}
