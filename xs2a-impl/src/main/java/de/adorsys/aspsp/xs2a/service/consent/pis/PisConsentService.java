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
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayment;
import de.adorsys.aspsp.xs2a.domain.pis.TppInfo;
import de.adorsys.aspsp.xs2a.service.mapper.PisConsentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

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
     * @param singlePayment  Payment data which will be stored in Pis consent
     * @param paymentId      Payment identifier
     * @param paymentProduct Payment product endpoint for payments e.g. for a SEPA Credit Transfer
     * @return String identifier of created PIS consent for single payment
     */
    public String createPisConsentForSinglePaymentAndGetId(SinglePayment singlePayment, String paymentId, TppInfo tppInfo, String paymentProduct) {
        PisConsentRequest request = pisConsentMapper.mapToPisConsentRequestForSinglePayment(singlePayment, paymentId, tppInfo, paymentProduct);

        ResponseEntity<String> responseEntity = consentRestTemplate.postForEntity(remotePisConsentUrls.createPisConsent(), request, String.class);
        return responseEntity.getBody();
    }

    /**
     * Sends a POST request to CMS to store created PIS consent for bulk payment
     *
     * @param paymentIdentifierMap Map of payments data which will be stored in Pis consent
     * @return String identifier of created PIS consent for bulk payment
     */
    public String createPisConsentForBulkPaymentAndGetId(Map<SinglePayment, PaymentInitialisationResponse> paymentIdentifierMap, TppInfo tppInfo, String paymentProduct) {
        PisConsentRequest request = pisConsentMapper.mapToPisConsentRequestForBulkPayment(paymentIdentifierMap, tppInfo, paymentProduct);

        ResponseEntity<String> responseEntity = consentRestTemplate.postForEntity(remotePisConsentUrls.createPisConsent(), request, String.class);
        return responseEntity.getBody();
    }

    /**
     * Sends a POST request to CMS to store created PIS consent for periodic payment
     *
     * @param periodicPayment Periodic payment data which will be stored in Pis consent
     * @return String identifier of created PIS consent periodic payment
     */
    public String createPisConsentForPeriodicPaymentAndGetId(PeriodicPayment periodicPayment, String paymentId, TppInfo tppInfo, String paymentProduct) {
        PisConsentRequest request = pisConsentMapper.mapToPisConsentRequestForPeriodicPayment(periodicPayment, paymentId, tppInfo, paymentProduct);

        ResponseEntity<String> responseEntity = consentRestTemplate.postForEntity(remotePisConsentUrls.createPisConsent(), request, String.class);
        return responseEntity.getBody();
    }
}
