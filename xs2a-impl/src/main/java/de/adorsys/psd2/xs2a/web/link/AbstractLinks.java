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

package de.adorsys.psd2.xs2a.web.link;

import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;

@EqualsAndHashCode(callSuper = true)
class AbstractLinks extends Links {

    private final String httpUrl;

    AbstractLinks(String httpUrl) {
        this.httpUrl = httpUrl;
    }

    HrefType buildPath(String path, Object... params) {
        UriComponentsBuilder uriComponentsBuilder = StringUtils.startsWith(httpUrl, "/")
                                                        ? fromPath(httpUrl)
                                                        : fromHttpUrl(httpUrl);
        return new HrefType(uriComponentsBuilder
                                .path(path)
                                .buildAndExpand(params)
                                .toUriString());
    }

    protected boolean isScaStatusMethodSelected(AuthenticationObject chosenScaMethod, ScaStatus scaStatus) {
        return chosenScaMethod != null
                   && scaStatus == ScaStatus.SCAMETHODSELECTED;
    }

    protected boolean isScaStatusMethodAuthenticated(ScaStatus scaStatus) {
        return scaStatus == ScaStatus.PSUAUTHENTICATED;
    }

    protected boolean isScaStatusMethodIdentified(ScaStatus scaStatus) {
        return scaStatus == ScaStatus.PSUIDENTIFIED;
    }
}
