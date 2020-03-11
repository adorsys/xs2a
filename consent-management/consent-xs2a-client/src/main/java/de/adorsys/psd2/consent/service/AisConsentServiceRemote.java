/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
import de.adorsys.psd2.consent.api.ais.AisConsentActionRequest;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.ais.UpdateAisConsentResponse;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.consent.config.AisConsentRemoteUrls;
import de.adorsys.psd2.consent.config.CmsRestException;
import de.adorsys.psd2.core.data.AccountAccess;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static de.adorsys.psd2.consent.api.CmsError.TECHNICAL_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class AisConsentServiceRemote implements AisConsentServiceEncrypted {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final AisConsentRemoteUrls remoteAisConsentUrls;

    @Override
    public CmsResponse<CmsResponse.VoidResponse> checkConsentAndSaveActionLog(AisConsentActionRequest request) {
        try {
            consentRestTemplate.postForEntity(remoteAisConsentUrls.consentActionLog(), request, Void.class);
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't check consent and save action log, HTTP response status: {}", cmsRestException.getHttpStatus());

            return CmsResponse.<CmsResponse.VoidResponse>builder()
                       .error(cmsRestException.getCmsError())
                       .build();
        }

        return CmsResponse.<CmsResponse.VoidResponse>builder()
                   .payload(CmsResponse.voidResponse())
                   .build();
    }

    @Override
    public CmsResponse<CmsConsent> updateAspspAccountAccess(String consentId, AccountAccess request) {
        try {
            UpdateAisConsentResponse response = consentRestTemplate.exchange(remoteAisConsentUrls.updateAisAccountAccess(), HttpMethod.PUT,
                                                                             new HttpEntity<>(request), UpdateAisConsentResponse.class, consentId).getBody();
            if (response != null) {
                return CmsResponse.<CmsConsent>builder()
                           .payload(response.getAisConsent())
                           .build();
            }
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't update ASPSP account access, HTTP response status: {}", cmsRestException.getHttpStatus());

            return CmsResponse.<CmsConsent>builder()
                       .error(cmsRestException.getCmsError())
                       .build();
        }

        return CmsResponse.<CmsConsent>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }
}
