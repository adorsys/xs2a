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

package de.adorsys.aspsp.xs2a.service.consent.pis;

import de.adorsys.aspsp.xs2a.config.rest.consent.PisConsentRemoteUrls;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.aspsp.xs2a.service.mapper.PisConsentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PisConsentService {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final PisConsentRemoteUrls remotePisConsentUrls;
    private final PisConsentMapper pisConsentMapper;

    /**
     * Sends a POST request to CMS to store created PIS consent for single payment
     *
     * @param createConsentRequest Provides transporting data when creating an consent
     * @return String identifier of created PIS consent for single payment
     */
    public String createPisConsentForSinglePaymentAndGetId(CreateConsentRequest createConsentRequest, String paymentId) {
        PisConsentRequest request = pisConsentMapper.mapToPisConsentRequestForSinglePayment(createConsentRequest, paymentId);

        CreatePisConsentResponse createPisConsentResponse = consentRestTemplate.postForEntity(remotePisConsentUrls.createPisConsent(), request, CreatePisConsentResponse.class).getBody();

        return Optional.ofNullable(createPisConsentResponse)
                   .map(CreatePisConsentResponse::getConsentId)
                   .orElse(null);
    }

    /**
     * Sends a POST request to CMS to store created PIS consent for bulk payment
     *
     * @param createConsentRequest Provides transporting data when creating an consent
     * @return String identifier of created PIS consent for bulk payment
     */
    public String createPisConsentForBulkPaymentAndGetId(CreateConsentRequest createConsentRequest) {
        PisConsentRequest request = pisConsentMapper.mapToPisConsentRequestForBulkPayment(createConsentRequest);

        ResponseEntity<String> responseEntity = consentRestTemplate.postForEntity(remotePisConsentUrls.createPisConsent(), request, String.class);
        return responseEntity.getBody();
    }

    /**
     * Sends a POST request to CMS to store created PIS consent for periodic payment
     *
     * @param createConsentRequest Provides transporting data when creating an consent
     * @return String identifier of created PIS consent periodic payment
     */
    public String createPisConsentForPeriodicPaymentAndGetId(CreateConsentRequest createConsentRequest, String paymentId) {
        PisConsentRequest request = pisConsentMapper.mapToPisConsentRequestForPeriodicPayment(createConsentRequest, paymentId);

        ResponseEntity<String> responseEntity = consentRestTemplate.postForEntity(remotePisConsentUrls.createPisConsent(), request, String.class);
        return responseEntity.getBody();
    }
}
