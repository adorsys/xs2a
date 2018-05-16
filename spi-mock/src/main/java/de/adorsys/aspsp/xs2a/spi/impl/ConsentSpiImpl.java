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
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
@AllArgsConstructor
public class ConsentSpiImpl implements ConsentSpi {
    private final RestTemplate restTemplate;
    private final RemoteSpiUrls remoteSpiUrls;

    @Override
    public String createAccountConsents(SpiAccountConsent consent) {
        ResponseEntity<String> response = restTemplate.postForEntity(remoteSpiUrls.createConsent(), consent, String.class);
        return response.getBody();
    }

    @Override
    public SpiAccountConsent getAccountConsentById(String consentId) {
        ResponseEntity<SpiAccountConsent> response = restTemplate.getForEntity(remoteSpiUrls.getConsentById(), SpiAccountConsent.class, consentId);
        return response.getBody();
    }

    @Override
    public void deleteAccountConsentsById(String consentId) {
        restTemplate.delete(remoteSpiUrls.deleteConsentById(), consentId);
    }

    @Override
    public void expireConsent(SpiAccountAccess access) {
        Optional.ofNullable(getAccountConsentByAccess(access))
            .ifPresent(consent -> {
                consent.setSpiConsentStatus(SpiConsentStatus.EXPIRED);
                createAccountConsents(consent);
            });
    }

    private SpiAccountConsent getAccountConsentByAccess(SpiAccountAccess access) {
        return restTemplate.getForEntity(remoteSpiUrls.getConsentByAccess(), SpiAccountConsent.class, access).getBody();
    }
}
