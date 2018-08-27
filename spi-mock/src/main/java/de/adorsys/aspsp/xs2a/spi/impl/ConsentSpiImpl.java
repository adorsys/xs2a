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

import de.adorsys.aspsp.xs2a.consent.api.ActionStatus;
import de.adorsys.aspsp.xs2a.consent.api.AisConsentStatusResponse;
import de.adorsys.aspsp.xs2a.consent.api.ConsentActionRequest;
import de.adorsys.aspsp.xs2a.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.aspsp.xs2a.consent.api.ais.CreateAisConsentResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.aspsp.xs2a.spi.config.consent.SpiAisConsentRemoteUrls;
import de.adorsys.aspsp.xs2a.spi.config.consent.SpiPisConsentRemoteUrls;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.aspsp.xs2a.spi.impl.mapper.SpiAisConsentMapper;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConsentSpiImpl implements ConsentSpi {

    @Qualifier("spiConsentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final SpiAisConsentRemoteUrls remoteAisConsentUrls;
    private final SpiPisConsentRemoteUrls remotePisConsentUrls;
    private final SpiAisConsentMapper aisConsentMapper;

    /**
     * For detailed description see {@link ConsentSpi#createConsent(CreateAisConsentRequest)}
     */
    @Override
    public String createConsent(CreateAisConsentRequest createAisConsentRequest) {
        CreateAisConsentResponse createAisConsentResponse = consentRestTemplate.postForEntity(remoteAisConsentUrls.createAisConsent(), createAisConsentRequest, CreateAisConsentResponse.class).getBody();

        return Optional.ofNullable(createAisConsentResponse)
                   .map(CreateAisConsentResponse::getConsentId)
                   .orElse(null);
    }

    /**
     * For detailed description see {@link ConsentSpi#getAccountConsentById(String)}
     */
    @Override
    public SpiAccountConsent getAccountConsentById(String consentId) {
        return consentRestTemplate.getForEntity(remoteAisConsentUrls.getAisConsentById(), SpiAccountConsent.class, consentId).getBody();
    }

    /**
     * For detailed description see {@link ConsentSpi#getAccountConsentStatusById(String)}
     */
    @Override
    public SpiConsentStatus getAccountConsentStatusById(String consentId) {
        AisConsentStatusResponse response = consentRestTemplate.getForEntity(remoteAisConsentUrls.getAisConsentStatusById(), AisConsentStatusResponse.class, consentId).getBody();
        return aisConsentMapper.mapToSpiConsentStatus(response.getConsentStatus())
                   .orElse(null);
    }

    /**
     * For detailed description see {@link ConsentSpi#revokeConsent(String)}
     */
    @Override
    public void revokeConsent(String consentId) {
        consentRestTemplate.put(remoteAisConsentUrls.updateAisConsentStatus(), null, consentId, SpiConsentStatus.REVOKED_BY_PSU);
    }

    /**
     * For detailed description see {@link ConsentSpi#consentActionLog(String, String, ActionStatus)}
     */
    @Override
    public void consentActionLog(String tppId, String consentId, ActionStatus actionStatus) {
        consentRestTemplate.postForEntity(remoteAisConsentUrls.consentActionLog(), new ConsentActionRequest(tppId, consentId, actionStatus), Void.class);
    }

    /**
     * For detailed description see {@link ConsentSpi#createPisConsentForSinglePaymentAndGetId(PisConsentRequest)}
     */
    @Override
    public String createPisConsentForSinglePaymentAndGetId(PisConsentRequest pisConsentRequest) {
        CreatePisConsentResponse createPisConsentResponse = consentRestTemplate.postForEntity(remotePisConsentUrls.createPisConsent(), pisConsentRequest, CreatePisConsentResponse.class).getBody();

        return Optional.ofNullable(createPisConsentResponse)
                   .map(CreatePisConsentResponse::getConsentId)
                   .orElse(null);
    }

    /**
     * For detailed description see {@link ConsentSpi#createPisConsentForBulkPaymentAndGetId(PisConsentRequest)}
     */
    @Override
    public String createPisConsentForBulkPaymentAndGetId(PisConsentRequest pisConsentRequest) {
        CreatePisConsentResponse createPisConsentResponse = consentRestTemplate.postForEntity(remotePisConsentUrls.createPisConsent(), pisConsentRequest, CreatePisConsentResponse.class).getBody();

        return Optional.ofNullable(createPisConsentResponse)
                   .map(CreatePisConsentResponse::getConsentId)
                   .orElse(null);
    }

    /**
     * For detailed description see {@link ConsentSpi#createPisConsentForPeriodicPaymentAndGetId(PisConsentRequest)}
     */
    @Override
    public String createPisConsentForPeriodicPaymentAndGetId(PisConsentRequest pisConsentRequest) {
        CreatePisConsentResponse createPisConsentResponse = consentRestTemplate.postForEntity(remotePisConsentUrls.createPisConsent(), pisConsentRequest, CreatePisConsentResponse.class).getBody();

        return Optional.ofNullable(createPisConsentResponse)
                   .map(CreatePisConsentResponse::getConsentId)
                   .orElse(null);
    }
}
