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
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentBulkPaymentRequest;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentPeriodicPaymentRequest;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PisConsentService {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final PisConsentRemoteUrls remotePisConsentUrls;

    /**
     * Sends a POST request to CMS to store created PIS consent for single payment
     *
     * @param singlePaymentId Payment id which will be stored in Pis consent
     * @return String identifier of created PIS consent for single payment
     */
    public String createPisConsentForSinglePaymentAndGetId(String singlePaymentId) {
        ResponseEntity<String> responseEntity = consentRestTemplate.postForEntity(remotePisConsentUrls.createPisConsent(), new PisConsentRequest(singlePaymentId), String.class);
        return responseEntity.getBody();
    }

    /**
     * Sends a POST request to CMS to store created PIS consent for bulk payment
     *
     * @param paymentIds List of payment ids which will be stored in Pis consent
     * @return String identifier of created PIS consent for bulk payment
     */
    public String createPisConsentForBulkPaymentAndGetId(List<String> paymentIds) {
        ResponseEntity<String> responseEntity = consentRestTemplate.postForEntity(remotePisConsentUrls.createPisBulkPaymentConsent(), new PisConsentBulkPaymentRequest(paymentIds), String.class);
        return responseEntity.getBody();
    }

    /**
     * Sends a POST request to CMS to store created PIS consent for periodic payment
     *
     * @param periodicPaymentId Periodic payment id which will be stored in Pis consent
     * @return String identifier of created PIS consent periodic payment
     */
    public String createPisConsentForPeriodicPaymentAndGetId(String periodicPaymentId) {
        ResponseEntity<String> responseEntity = consentRestTemplate.postForEntity(remotePisConsentUrls.createPisPeriodicPaymentConsent(), new PisConsentPeriodicPaymentRequest(periodicPaymentId), String.class);
        return responseEntity.getBody();
    }
}
