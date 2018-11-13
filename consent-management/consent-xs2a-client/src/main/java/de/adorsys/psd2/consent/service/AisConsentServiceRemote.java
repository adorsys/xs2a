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

import de.adorsys.psd2.consent.api.CmsAspspConsentDataBase64;
import de.adorsys.psd2.consent.api.ConsentType;
import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.consent.api.service.AisConsentService;
import de.adorsys.psd2.consent.config.AisConsentRemoteUrls;
import de.adorsys.psd2.consent.config.CommonAspspConsentDataRemoteUrls;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AisConsentServiceRemote implements AisConsentService {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final AisConsentRemoteUrls remoteAisConsentUrls;
    private final CommonAspspConsentDataRemoteUrls commonAspspConsentDataRemoteUrls;

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

    // TODO check response result
    @Override
    public boolean updateConsentStatusById(String consentId, ConsentStatus status) {
        consentRestTemplate.put(remoteAisConsentUrls.updateAisConsentStatus(), null, consentId, status);
        return true;
    }

    @Override
    public Optional<AisAccountConsent> getAisAccountConsentById(String consentId) {
        AisAccountConsent accountConsent = consentRestTemplate.getForEntity(remoteAisConsentUrls.getAisConsentById(), AisAccountConsent.class, consentId).getBody();
        return Optional.ofNullable(accountConsent);
    }

    @Override
    public void checkConsentAndSaveActionLog(AisConsentActionRequest request) {
        consentRestTemplate.postForEntity(remoteAisConsentUrls.consentActionLog(), request, Void.class);
    }

    @Override
    public Optional<String> updateAccountAccess(String consentId, AisAccountAccessInfo request) {
        CreateAisConsentResponse response = consentRestTemplate.exchange(remoteAisConsentUrls.updateAisAccountAccess(), HttpMethod.PUT,
            new HttpEntity<>(request), CreateAisConsentResponse.class, consentId).getBody();
        return Optional.ofNullable(response.getConsentId());
    }

    @Override
    public Optional<CmsAspspConsentDataBase64> getAspspConsentData(String consentId) {
        return Optional.ofNullable(consentRestTemplate.getForEntity(commonAspspConsentDataRemoteUrls.getAspspConsentDataByConsentId(), CmsAspspConsentDataBase64.class, consentId, ConsentType.AIS)
                                       .getBody());
    }

    @Override
    public Optional<String> saveAspspConsentDataInAisConsent(String consentId, CmsAspspConsentDataBase64 request) {
        CreateAisConsentResponse response = consentRestTemplate.exchange(commonAspspConsentDataRemoteUrls.updateAspspConsentData(), HttpMethod.PUT,
            new HttpEntity<>(request), CreateAisConsentResponse.class, consentId, ConsentType.AIS).getBody();
        return Optional.ofNullable(response.getConsentId());
    }

    @Override
    public Optional<String> createAuthorization(String consentId, AisConsentAuthorizationRequest request) {
        CreateAisConsentAuthorizationResponse response = consentRestTemplate.postForEntity(remoteAisConsentUrls.createAisConsentAuthorization(),
            request, CreateAisConsentAuthorizationResponse.class, consentId).getBody();

        return Optional.ofNullable(response)
                   .map(CreateAisConsentAuthorizationResponse::getAuthorizationId);
    }

    @Override
    public Optional<AisConsentAuthorizationResponse> getAccountConsentAuthorizationById(String authorizationId, String consentId) {
        return Optional.ofNullable(consentRestTemplate.getForEntity(remoteAisConsentUrls.getAisConsentAuthorizationById(), AisConsentAuthorizationResponse.class, consentId, authorizationId)
                                       .getBody());
    }

    @Override
    public boolean updateConsentAuthorization(String authorizationId, AisConsentAuthorizationRequest request) {
        consentRestTemplate.put(remoteAisConsentUrls.updateAisConsentAuthorization(), request, authorizationId);
        return true;
    }

    @Override
    public Optional<PsuIdData> getPsuDataByConsentId(String consentId) {
        return Optional.ofNullable(consentRestTemplate.getForEntity(remoteAisConsentUrls.getPsuDataByConsentId(), PsuIdData.class, consentId)
                                       .getBody());
    }
}
