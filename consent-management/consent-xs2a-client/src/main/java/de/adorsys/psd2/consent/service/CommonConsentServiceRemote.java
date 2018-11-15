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
import de.adorsys.psd2.consent.api.ais.CreateAisConsentResponse;
import de.adorsys.psd2.consent.api.service.CommonConsentService;
import de.adorsys.psd2.consent.config.CommonAspspConsentDataRemoteUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommonConsentServiceRemote implements CommonConsentService {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final CommonAspspConsentDataRemoteUrls commonAspspConsentDataRemoteUrls;

    @Override
    public Optional<CmsAspspConsentDataBase64> getAspspConsentDataByConsentId(String consentId, ConsentType consentType) {
        return Optional.ofNullable(consentRestTemplate.getForEntity(commonAspspConsentDataRemoteUrls.getAspspConsentDataByConsentId(), CmsAspspConsentDataBase64.class, consentId, consentType)
                                       .getBody());
    }

    @Override
    public Optional<CmsAspspConsentDataBase64> getAspspConsentDataByPaymentId(String paymentId) {
        return Optional.ofNullable(consentRestTemplate.getForEntity(commonAspspConsentDataRemoteUrls.getAspspConsentDataByPaymentId(), CmsAspspConsentDataBase64.class, paymentId)
                                       .getBody());
    }

    @Override
    public Optional<String> saveAspspConsentData(String consentId, CmsAspspConsentDataBase64 request, ConsentType consentType) {
        CreateAisConsentResponse response = consentRestTemplate.exchange(commonAspspConsentDataRemoteUrls.updateAspspConsentData(), HttpMethod.PUT,
            new HttpEntity<>(request), CreateAisConsentResponse.class, consentId, consentType).getBody();
        return Optional.ofNullable(response.getConsentId());
    }

    @Override
    public boolean deleteAspspConsentDataByConsentId(String consentId, ConsentType consentType) {
        consentRestTemplate.exchange(commonAspspConsentDataRemoteUrls.deleteAspspConsentData(), HttpMethod.DELETE, null, Boolean.class, consentId, consentType);
        return true;
    }
}
