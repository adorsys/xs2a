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

import de.adorsys.psd2.consent.api.CmsResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static de.adorsys.psd2.consent.api.CmsError.TECHNICAL_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class AisConsentServiceRemote implements AisConsentServiceEncrypted {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final AisConsentRemoteUrls remoteAisConsentUrls;

    @Override
    public CmsResponse<CreateAisConsentResponse> createConsent(CreateAisConsentRequest request) {
        try {
            ResponseEntity<CreateAisConsentResponse> createAisConsentResponse = consentRestTemplate.postForEntity(remoteAisConsentUrls.createAisConsent(), request, CreateAisConsentResponse.class);
            return CmsResponse.<CreateAisConsentResponse>builder()
                       .payload(createAisConsentResponse.getBody())
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.warn("Remote consent creation failed");
        }
        return CmsResponse.<CreateAisConsentResponse>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<ConsentStatus> getConsentStatusById(String consentId) {
        try {
            AisConsentStatusResponse response = consentRestTemplate.getForEntity(remoteAisConsentUrls.getAisConsentStatusById(), AisConsentStatusResponse.class, consentId).getBody();
            if (response != null) {
                return CmsResponse.<ConsentStatus>builder()
                           .payload(response.getConsentStatus())
                           .build();
            }
        } catch (CmsRestException cmsRestException) {
            log.warn("Remote get consent status by id failed");
        }

        return CmsResponse.<ConsentStatus>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<Boolean> updateConsentStatusById(String consentId, ConsentStatus status) {
        try {
            consentRestTemplate.put(remoteAisConsentUrls.updateAisConsentStatus(), null, consentId, status);
            return CmsResponse.<Boolean>builder()
                       .payload(true)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.warn("Cannot update consent status, the consent is already deleted or not found");
        }

        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }

    @Override
    public CmsResponse<AisAccountConsent> getAisAccountConsentById(String consentId) {
        ResponseEntity<AisAccountConsent> accountConsent = consentRestTemplate.getForEntity(remoteAisConsentUrls.getAisConsentById(), AisAccountConsent.class, consentId);
        if (accountConsent.getStatusCode() == HttpStatus.OK) {
            return CmsResponse.<AisAccountConsent>builder()
                       .payload(accountConsent.getBody())
                       .build();
        }

        log.warn("Remote get account consent by id failed");
        return CmsResponse.<AisAccountConsent>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<Boolean> findAndTerminateOldConsentsByNewConsentId(String newConsentId) {
        consentRestTemplate.delete(remoteAisConsentUrls.findAndTerminateOldConsentsByNewConsentId(), newConsentId);
        return CmsResponse.<Boolean>builder()
                   .payload(true)
                   .build();
    }

    @Override
    public CmsResponse<CmsResponse.VoidResponse> checkConsentAndSaveActionLog(AisConsentActionRequest request) {
        consentRestTemplate.postForEntity(remoteAisConsentUrls.consentActionLog(), request, Void.class);

        return CmsResponse.<CmsResponse.VoidResponse>builder()
                   .payload(CmsResponse.voidResponse())
                   .build();
    }

    @Override
    public CmsResponse<String> updateAspspAccountAccess(String consentId, AisAccountAccessInfo request) {
        CreateAisConsentResponse response = consentRestTemplate.exchange(remoteAisConsentUrls.updateAisAccountAccess(), HttpMethod.PUT,
                                                                         new HttpEntity<>(request), CreateAisConsentResponse.class, consentId).getBody();
        if (response != null) {
            return CmsResponse.<String>builder()
                       .payload(response.getConsentId())
                       .build();
        }

        log.warn("Remote update aspsp account access failed");
        return CmsResponse.<String>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<AisAccountConsent> updateAspspAccountAccessWithResponse(String consentId, AisAccountAccessInfo request) {
        try {
            UpdateAisConsentResponse response = consentRestTemplate.exchange(remoteAisConsentUrls.updateAisAccountAccess(), HttpMethod.PUT,
                                                                             new HttpEntity<>(request), UpdateAisConsentResponse.class, consentId).getBody();
            if (response != null) {
                return CmsResponse.<AisAccountConsent>builder()
                           .payload(response.getAisConsent())
                           .build();
            }
        } catch (CmsRestException cmsRestException) {
            log.warn("Remote update aspsp account access with response failed");
        }

        return CmsResponse.<AisAccountConsent>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<List<PsuIdData>> getPsuDataByConsentId(String consentId) {
        try {
            List<PsuIdData> response = consentRestTemplate.exchange(remoteAisConsentUrls.getPsuDataByConsentId(),
                                                                    HttpMethod.GET,
                                                                    null,
                                                                    new ParameterizedTypeReference<List<PsuIdData>>() {
                                                                    },
                                                                    consentId)
                                           .getBody();
            return CmsResponse.<List<PsuIdData>>builder()
                       .payload(response)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.warn("Remote get psu data by consent id failed");
        }

        return CmsResponse.<List<PsuIdData>>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<Boolean> updateMultilevelScaRequired(String encryptedConsentId, boolean multilevelScaRequired) {
        try {
            Boolean updateResponse = consentRestTemplate.exchange(remoteAisConsentUrls.updateMultilevelScaRequired(),
                                                                  HttpMethod.PUT, null, Boolean.class, encryptedConsentId, multilevelScaRequired)
                                         .getBody();
            return CmsResponse.<Boolean>builder()
                       .payload(updateResponse)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.warn("Remote update multilevel sca required failed");
        }

        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }
}
