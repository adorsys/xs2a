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

package de.adorsys.psd2.consent.service;

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

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TppServiceRemote implements TppService {

    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final TppServiceRemoteUrls tppServiceRemoteUrls;

    @Override
    public boolean updateTppInfo(@NotNull TppInfo tppInfo) {
        try {
            ResponseEntity<Boolean> responseEntity = consentRestTemplate.exchange(tppServiceRemoteUrls.updateTppInfo(), HttpMethod.PUT, new HttpEntity<>(tppInfo), Boolean.class);
            return Optional.ofNullable(responseEntity.getBody()).orElse(false);
        } catch (CmsRestException e) {
            log.error("TPP not found, id: {}", tppInfo.getAuthorisationNumber());
            return false;
        }
    }
}
