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

package de.adorsys.aspsp.xs2a.service.consent.ais;

import de.adorsys.aspsp.xs2a.config.rest.consent.AisConsentRemoteUrls;
import de.adorsys.aspsp.xs2a.consent.api.ConsentActionRequest;
import de.adorsys.aspsp.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.aspsp.xs2a.service.mapper.ConsentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AisConsentService {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final AisConsentRemoteUrls remoteAisConsentUrls;
    private final ConsentMapper consentMapper;

    public String createConsent(CreateConsentReq request, String psuId, String tppId) {
        return consentRestTemplate.postForEntity(remoteAisConsentUrls.createAisConsent(), consentMapper.mapToAisConsentRequest(request, psuId, tppId), String.class).getBody();
    }

    public SpiAccountConsent getAccountConsentById(String consentId) {
        return consentRestTemplate.getForEntity(remoteAisConsentUrls.getAisConsentById(), SpiAccountConsent.class, consentId).getBody();
    }

    public SpiConsentStatus getAccountConsentStatusById(String consentId) {
        return consentRestTemplate.getForEntity(remoteAisConsentUrls.getAisConsentStatusById(), SpiConsentStatus.class, consentId).getBody();
    }

    public void revokeConsent(String consentId) {
        consentRestTemplate.put(remoteAisConsentUrls.updateAisConsentStatus(), null, consentId, SpiConsentStatus.REVOKED_BY_PSU);
    }

    public void consentActionLog(ConsentActionRequest request) {
        consentRestTemplate.postForEntity(remoteAisConsentUrls.consentActionLog(),request,void.class);
    }
}
