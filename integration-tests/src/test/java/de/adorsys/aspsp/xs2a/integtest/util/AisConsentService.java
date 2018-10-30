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

package de.adorsys.aspsp.xs2a.integtest.util;

import de.adorsys.aspsp.xs2a.integtest.config.rest.consent.AisConsentRemoteUrls;
import de.adorsys.psd2.consent.api.AccountInfo;
import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentResponse;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Collections;

@Service
public class AisConsentService {

    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final AisConsentRemoteUrls remoteAisConsentUrls;

    @Autowired
    public AisConsentService(RestTemplate consentRestTemplate, AisConsentRemoteUrls remoteAisConsentUrls){
        this.consentRestTemplate = consentRestTemplate;
        this.remoteAisConsentUrls = remoteAisConsentUrls;
    }

    /**
     * Sends a post request to the consent management for creating a consent
     *
     * @return Consent Id
     */
    public String createConsent() {
        HttpEntity entity = getConsentEntity();
        CreateAisConsentResponse createAisConsentResponse = consentRestTemplate.postForEntity(remoteAisConsentUrls.createAisConsent(), entity,  CreateAisConsentResponse.class).getBody();

        return createAisConsentResponse.getConsentId();
    }

    /**
     * Creates a new Http Entity with a consent as body and Content-Type, Accept as headers
     *
     * @return Http Entity containing the consent and the headers
     */
    private HttpEntity getConsentEntity() {
        CreateAisConsentRequest aisConsentRequest = new CreateAisConsentRequest();
        AisAccountAccessInfo info = new AisAccountAccessInfo();
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setCurrency("EUR");
        accountInfo.setIban("DE89370400440532013000");
        info.setAccounts(Collections.singletonList(accountInfo));
        aisConsentRequest.setAccess(info);
        aisConsentRequest.setCombinedServiceIndicator(false);
        aisConsentRequest.setFrequencyPerDay(4);
        aisConsentRequest.setPsuId("aspsp");
        aisConsentRequest.setRecurringIndicator(false);
        aisConsentRequest.setTppId("12345987");
        aisConsentRequest.setValidUntil(LocalDate.now().plusDays(30));
        aisConsentRequest.setTppRedirectPreferred(true);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "application/json");

        return new HttpEntity<>(aisConsentRequest, headers);
    }

    /**
     *
     * @param consentId as a String
     * @param consentStatus which represents the new Consent Status
     */
    public void changeAccountConsentStatus (@NotNull String consentId, ConsentStatus consentStatus) {
        consentRestTemplate.put(remoteAisConsentUrls.updateAisConsentStatus(), null, consentId, consentStatus);
    }
}

