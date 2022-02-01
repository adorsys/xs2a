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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.service.TppService;
import de.adorsys.psd2.consent.config.CmsRestException;
import de.adorsys.psd2.consent.config.TppServiceRemoteUrls;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class TppServiceRemote implements TppService {

    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final TppServiceRemoteUrls tppServiceRemoteUrls;

    @Override
    public CmsResponse<Boolean> updateTppInfo(@NotNull TppInfo tppInfo) {
        try {
            ResponseEntity<Boolean> responseEntity = consentRestTemplate.exchange(tppServiceRemoteUrls.updateTppInfo(), HttpMethod.PUT, new HttpEntity<>(tppInfo), Boolean.class);
            return CmsResponse.<Boolean>builder()
                              .payload(responseEntity.getBody())
                              .build();
        } catch (CmsRestException e) {
            log.error("TPP not found, id: {}", tppInfo.getAuthorisationNumber());
        }

        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }
}
