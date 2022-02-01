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
        if (aspspConsentData.isEmptyConsentData()) {
            log.warn("Failed to update AspspConsentData. AspspConsentData is empty");
            return false;
        }
        String base64Payload = base64AspspDataService.encode(aspspConsentData.getAspspConsentDataBytes());
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
