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
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.consent.config.TppStopListRemoteUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class TppStopListServiceRemote implements TppStopListService {
    private static final String TPP_AUTHORISATION_NUMBER_HEADER = "tpp-authorisation-number";
    private static final String INSTANCE = "Instance-ID";

    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final TppStopListRemoteUrls tppStopListRemoteUrls;

    @Override
    public CmsResponse<Boolean> checkIfTppBlocked(String tppAuthorisationNumber, String instanceId) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.ACCEPT, MediaType.ALL_VALUE);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(TPP_AUTHORISATION_NUMBER_HEADER, tppAuthorisationNumber);
        headers.add(INSTANCE, instanceId);

        Boolean body = consentRestTemplate.exchange(tppStopListRemoteUrls.checkIfTppBlocked(), HttpMethod.GET, new HttpEntity<>(headers), Boolean.class)
                           .getBody();

        return CmsResponse.<Boolean>builder()
                   .payload(body)
                   .build();
    }
}
