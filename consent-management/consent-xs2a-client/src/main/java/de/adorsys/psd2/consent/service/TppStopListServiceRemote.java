/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.consent.config.TppStopListRemoteUrls;
import de.adorsys.psd2.xs2a.core.tpp.TppUniqueParamsHolder;
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
    private static final String AUTHORITY_ID_HEADER = "authority-id";

    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final TppStopListRemoteUrls tppStopListRemoteUrls;

    @Override
    public boolean checkIfTppBlocked(TppUniqueParamsHolder tppUniqueParams) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.ACCEPT, MediaType.ALL_VALUE);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(TPP_AUTHORISATION_NUMBER_HEADER, tppUniqueParams.getAuthorisationNumber());
        headers.add(AUTHORITY_ID_HEADER, tppUniqueParams.getAuthorityId());

        return consentRestTemplate.exchange(tppStopListRemoteUrls.checkIfTppBlocked(), HttpMethod.GET, new HttpEntity<>(headers), Boolean.class)
                   .getBody();
    }
}
