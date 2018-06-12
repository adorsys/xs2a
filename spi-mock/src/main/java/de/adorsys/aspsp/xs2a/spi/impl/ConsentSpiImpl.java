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

package de.adorsys.aspsp.xs2a.spi.impl;

import de.adorsys.aspsp.xs2a.spi.config.RemoteSpiUrls;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@AllArgsConstructor
public class ConsentSpiImpl implements ConsentSpi {
    private final RestTemplate restTemplate;
    private final RemoteSpiUrls remoteSpiUrls;
//TODO Subject to removal upon team agreement
/*    @Override
    public SpiAccountConsent getAccountConsentById(String consentId) {
        return restTemplate.getForEntity(remoteSpiUrls.getAisConsentById(), SpiAccountConsent.class, consentId).getBody();
    }

    @Override
    public SpiConsentStatus getAccountConsentStatusById(String consentId) {
        return restTemplate.getForEntity(remoteSpiUrls.getAisConsentStatusById(), SpiConsentStatus.class, consentId).getBody();
    }

    @Override
    public void deleteAccountConsentById(String consentId) {
        restTemplate.put(remoteSpiUrls.updateAisConsentStatus(), null, consentId, SpiConsentStatus.REVOKED_BY_PSU);
    }

    @Override
    public Map<String, Set<AccessAccountInfo>> checkValidityByConsent(AvailableAccessRequest request) {
        HttpEntity<AvailableAccessRequest> requestHttpEntity = new HttpEntity<>(request);
        return restTemplate.exchange(
            remoteSpiUrls.checkAccessByConsentId(), HttpMethod.POST, requestHttpEntity,
            new ParameterizedTypeReference<Map<String, Set<AccessAccountInfo>>>() {
            }).getBody();
    }*/
}
