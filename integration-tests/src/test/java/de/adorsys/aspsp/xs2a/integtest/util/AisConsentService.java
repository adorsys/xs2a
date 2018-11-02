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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.aspsp.xs2a.integtest.config.rest.consent.AisConsentRemoteUrls;
import de.adorsys.psd2.consent.api.AccountInfo;
import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentResponse;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.resourceToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AisConsentService {

    @Qualifier("consent")
    private final RestTemplate consentRestTemplate;
    private final AisConsentRemoteUrls remoteAisConsentUrls;

    @Autowired
    public AisConsentService(RestTemplate consentRestTemplate, AisConsentRemoteUrls remoteAisConsentUrls){
        this.consentRestTemplate = consentRestTemplate;
        this.remoteAisConsentUrls = remoteAisConsentUrls;
    }

    @Autowired
    private ObjectMapper mapper;

    /**
     * Sends a post request to the consent management for creating a consent
     *
     * @return Consent Id
     */
    public String createConsentWithStatus(String consentStatus, String filename) throws IOException {
        HttpEntity entity = getConsentFromJsonData(filename);
        log.info("////entity request consent////  "+entity.toString());
        CreateAisConsentResponse createAisConsentResponse = consentRestTemplate.postForEntity(remoteAisConsentUrls.createAisConsent(), entity,  CreateAisConsentResponse.class).getBody();

        Optional<ConsentStatus> consentStatusOptional = ConsentStatus.fromValue(consentStatus);
        changeAccountConsentStatus(createAisConsentResponse.getConsentId(), consentStatusOptional.get());

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
        aisConsentRequest.setPsuData(new PsuIdData("aspsp", null, null, null));
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

    private HttpEntity getConsentFromJsonData(String dataFileName) throws IOException {
        CreateAisConsentRequest aisConsentRequest = mapper.readValue(resourceToString("/data-input/ais/" + dataFileName, UTF_8), CreateAisConsentRequest.class);
        aisConsentRequest.setValidUntil(LocalDate.now().plusDays(90));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "application/json");

        return new HttpEntity<>(aisConsentRequest, headers);
    }


}

