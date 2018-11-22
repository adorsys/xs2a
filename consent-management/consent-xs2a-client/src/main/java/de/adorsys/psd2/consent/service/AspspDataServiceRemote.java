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

import de.adorsys.psd2.consent.api.AspspDataService;
import de.adorsys.psd2.consent.api.CmsAspspConsentDataBase64;
import de.adorsys.psd2.consent.config.AspspConsentDataRemoteUrls;
import de.adorsys.psd2.consent.config.CmsRestException;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AspspDataServiceRemote implements AspspDataService {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final AspspConsentDataRemoteUrls aspspConsentDataRemoteUrls;
    private final Base64AspspDataService base64AspspDataService;

    @Override
    public @NotNull Optional<AspspConsentData> readAspspConsentData(@NotNull String externalId) {
        try {
            ResponseEntity<CmsAspspConsentDataBase64> request = consentRestTemplate.getForEntity(aspspConsentDataRemoteUrls.getAspspConsentData(), CmsAspspConsentDataBase64.class, externalId);
            return Optional.of(request.getBody())
                       .map(this::mapToAspspConsentData);
        } catch (CmsRestException e) {
            log.warn("Failed to read AspspConsentData! Consent ID {}", externalId);
            return Optional.empty();
        }
    }

    @Override
    public boolean updateAspspConsentData(@NotNull AspspConsentData aspspConsentData) {
        String base64Payload = base64AspspDataService.encode(aspspConsentData.getAspspConsentData());
        String consentId = aspspConsentData.getConsentId();
        CmsAspspConsentDataBase64 cmsAspspConsentDataBase64 = new CmsAspspConsentDataBase64(consentId, base64Payload);
        try {
            return consentRestTemplate.exchange(aspspConsentDataRemoteUrls.updateAspspConsentData(), HttpMethod.PUT, new HttpEntity<>(cmsAspspConsentDataBase64), Void.class, consentId)
                       .getStatusCode() == HttpStatus.OK;
        } catch (CmsRestException e) {
            log.warn("Failed to update AspspConsentData! Consent ID {}", aspspConsentData.getConsentId());
            return false;
        }
    }

    @Override
    public boolean deleteAspspConsentData(@NotNull String externalId) {
        try {
            ResponseEntity<Boolean> request = consentRestTemplate.exchange(aspspConsentDataRemoteUrls.deleteAspspConsentData(), HttpMethod.DELETE, null, Boolean.class, externalId);
            return request.getBody();
        } catch (CmsRestException e) {
            log.warn("Failed to delete AspspConsentData! Consent ID {}", externalId);
            return false;
        }
    }

    private AspspConsentData mapToAspspConsentData(CmsAspspConsentDataBase64 consentData) {
        byte[] bytePayload = base64AspspDataService.decode(consentData.getAspspConsentDataBase64());
        return new AspspConsentData(bytePayload, consentData.getConsentId());
    }
}
