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
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPeriodicPayment;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisSinglePayment;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentBulkPaymentRequest;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentPeriodicPaymentRequest;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
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
    private final PaymentMapper paymentMapper;

    public String createPisConsentForSinglePaymentAndGetId(SinglePayments singlePayment) {
        PisSinglePayment pisSinglePayment = paymentMapper.mapToPisSinglePayment(singlePayment);
        ResponseEntity<String> responseEntity = consentRestTemplate.postForEntity(remotePisConsentUrls.createPisConsent(), new PisConsentRequest(pisSinglePayment), String.class);
        return responseEntity.getBody();
    }

    public String createPisConsentForBulkPaymentAndGetId(List<SinglePayments> payments) {
        List<PisSinglePayment> pisPayments = paymentMapper.mapToPisSinglePaymentList(payments);
        ResponseEntity<String> responseEntity = consentRestTemplate.postForEntity(remotePisConsentUrls.createPisBulkPaymentConsent(), new PisConsentBulkPaymentRequest(pisPayments), String.class);
        return responseEntity.getBody();
    }

    public String createPisConsentForPeriodicPaymentAndGetId(PeriodicPayment periodicPayment) {
        PisPeriodicPayment pisPeriodicPayment = paymentMapper.mapToPisPeriodicPayment(periodicPayment);
        ResponseEntity<String> responseEntity = consentRestTemplate.postForEntity(remotePisConsentUrls.createPisPeriodicPaymentConsent(), new PisConsentPeriodicPaymentRequest(pisPeriodicPayment), String.class);
        return responseEntity.getBody();
    }
}
