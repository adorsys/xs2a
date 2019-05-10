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

import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.consent.config.AisConsentRemoteUrls;
import de.adorsys.psd2.consent.config.CmsRestException;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

// TODO discuss error handling (e.g. 400 HttpCode response) https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/498
@Slf4j
@Service
@RequiredArgsConstructor
public class AisConsentServiceRemote implements AisConsentServiceEncrypted {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final AisConsentRemoteUrls remoteAisConsentUrls;

    @Override
    public Optional<String> createConsent(CreateAisConsentRequest request) {
        CreateAisConsentResponse createAisConsentResponse = consentRestTemplate.postForEntity(remoteAisConsentUrls.createAisConsent(), request, CreateAisConsentResponse.class).getBody();
        return Optional.ofNullable(createAisConsentResponse.getConsentId());
    }

    @Override
    public Optional<ConsentStatus> getConsentStatusById(String consentId) {
        AisConsentStatusResponse response = consentRestTemplate.getForEntity(remoteAisConsentUrls.getAisConsentStatusById(), AisConsentStatusResponse.class, consentId).getBody();
        return Optional.ofNullable(response.getConsentStatus());
    }

    @Override
    public boolean updateConsentStatusById(String consentId, ConsentStatus status) {
        try {
            consentRestTemplate.put(remoteAisConsentUrls.updateAisConsentStatus(), null, consentId, status);
            return true;
        } catch (CmsRestException cmsRestException) {
            log.warn("Cannot update consent status, the consent is already deleted or not found");
        }
        return false;
    }

    @Override
    public Optional<AisAccountConsent> getAisAccountConsentById(String consentId) {
        AisAccountConsent accountConsent = consentRestTemplate.getForEntity(remoteAisConsentUrls.getAisConsentById(), AisAccountConsent.class, consentId).getBody();
        return Optional.ofNullable(accountConsent);
    }

    @Override
    public Optional<AisAccountConsent> getInitialAisAccountConsentById(String consentId) {
        AisAccountConsent accountConsent = consentRestTemplate.getForEntity(remoteAisConsentUrls.getInitialAisConsentById(), AisAccountConsent.class, consentId).getBody();
        return Optional.ofNullable(accountConsent);
    }

    @Override
    public boolean findAndTerminateOldConsentsByNewConsentId(String newConsentId) {
        consentRestTemplate.delete(remoteAisConsentUrls.findAndTerminateOldConsentsByNewConsentId(), newConsentId);
        return true;
    }

    @Override
    public void checkConsentAndSaveActionLog(AisConsentActionRequest request) {
        consentRestTemplate.postForEntity(remoteAisConsentUrls.consentActionLog(), request, Void.class);
    }

    @Override
    public Optional<String> updateAspspAccountAccess(String consentId, AisAccountAccessInfo request) {
        CreateAisConsentResponse response = consentRestTemplate.exchange(remoteAisConsentUrls.updateAisAccountAccess(), HttpMethod.PUT,
                                                                         new HttpEntity<>(request), CreateAisConsentResponse.class, consentId).getBody();
        return Optional.ofNullable(response.getConsentId());
    }

    @Override
    public Optional<AisAccountConsent> updateAspspAccountAccessWithResponse(String consentId, AisAccountAccessInfo request) {
        UpdateAisConsentResponse response = consentRestTemplate.exchange(remoteAisConsentUrls.updateAisAccountAccess(), HttpMethod.PUT,
                                                                         new HttpEntity<>(request), UpdateAisConsentResponse.class, consentId).getBody();
        return Optional.ofNullable(response.getAisConsent());
    }

    @Override
    public Optional<List<PsuIdData>> getPsuDataByConsentId(String consentId) {
        return Optional.ofNullable(consentRestTemplate.exchange(remoteAisConsentUrls.getPsuDataByConsentId(),
                                                                HttpMethod.GET,
                                                                null,
                                                                new ParameterizedTypeReference<List<PsuIdData>>() {
                                                                },
                                                                consentId)
                                       .getBody());
    }

    @Override
    public boolean updateMultilevelScaRequired(String encryptedConsentId, boolean multilevelScaRequired) {
        return consentRestTemplate.exchange(remoteAisConsentUrls.updateMultilevelScaRequired(),
                                            HttpMethod.PUT, null, Boolean.class, encryptedConsentId, multilevelScaRequired)
                   .getBody();
    }
}
