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
