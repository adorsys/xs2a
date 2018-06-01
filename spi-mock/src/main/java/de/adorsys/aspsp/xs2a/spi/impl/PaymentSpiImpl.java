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
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.pis.PisConsentRequest;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import lombok.AllArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.CREATED;

@Component
@AllArgsConstructor
public class PaymentSpiImpl implements PaymentSpi {
    private final RestTemplate restTemplate;
    private final RemoteSpiUrls remoteSpiUrls;

    private final boolean redirectMode = true;

    @Override
    public SpiPaymentInitialisationResponse createPaymentInitiation(SpiSinglePayments spiSinglePayments, String paymentProduct, boolean tppRedirectPreferred) {
        String paymentId = UUID.randomUUID().toString();
        spiSinglePayments.setPaymentId(paymentId);
        return redirectMode
                   ? paymentForRedirectMode(spiSinglePayments)
                   : paymentForOauthMode(spiSinglePayments, tppRedirectPreferred);
    }

    private SpiPaymentInitialisationResponse paymentForRedirectMode(SpiSinglePayments spiSinglePayments) {
        String pisConsentId = createPisConsent(spiSinglePayments);
        return StringUtils.isEmpty(pisConsentId)
                   ? null
                   : mapToSpiPaymentResponseByPisConsentId(pisConsentId, true);
    }

    private SpiPaymentInitialisationResponse paymentForOauthMode(SpiSinglePayments spiSinglePayments, boolean tppRedirectPreferred) {
        ResponseEntity<SpiSinglePayments> responseEntity = restTemplate.postForEntity(remoteSpiUrls.createPayment(), spiSinglePayments, SpiSinglePayments.class);
        return responseEntity.getStatusCode() == CREATED
                   ? mapToSpiPaymentResponse(responseEntity.getBody(), tppRedirectPreferred)
                   : null;
    }

    @Override
    public List<SpiPaymentInitialisationResponse> createBulkPayments(List<SpiSinglePayments> payments, String paymentProduct, boolean tppRedirectPreferred) {
        ResponseEntity<List<SpiSinglePayments>> responseEntity = restTemplate.exchange(remoteSpiUrls.createBulkPayment(), HttpMethod.POST, new HttpEntity<>(payments, null), new ParameterizedTypeReference<List<SpiSinglePayments>>() {
        });
        return (responseEntity.getStatusCode() == CREATED)
                   ? responseEntity.getBody().stream()
                         .map(spiPaym -> mapToSpiPaymentResponse(spiPaym, tppRedirectPreferred))
                         .collect(Collectors.toList())
                   : Collections.emptyList();
    }

    @Override
    public SpiPaymentInitialisationResponse initiatePeriodicPayment(SpiPeriodicPayment periodicPayment, String paymentProduct, boolean tppRedirectPreferred) {
        ResponseEntity<SpiPeriodicPayment> responseEntity = restTemplate.postForEntity(remoteSpiUrls.createPeriodicPayment(), periodicPayment, SpiPeriodicPayment.class);
        return responseEntity.getStatusCode() == CREATED ? mapToSpiPaymentResponse(responseEntity.getBody(), tppRedirectPreferred) : null;
    }

    @Override
    public SpiTransactionStatus getPaymentStatusById(String paymentId, String paymentProduct) {
        return restTemplate.getForEntity(remoteSpiUrls.getPaymentStatus(), SpiTransactionStatus.class, paymentId).getBody();
    }

    private SpiPaymentInitialisationResponse mapToSpiPaymentResponse(SpiSinglePayments spiSinglePayments, boolean tppRedirectPreferred) {
        SpiPaymentInitialisationResponse paymentResponse = new SpiPaymentInitialisationResponse();
        paymentResponse.setTransactionStatus(SpiTransactionStatus.RCVD);
        paymentResponse.setPaymentId(spiSinglePayments.getPaymentId());
        paymentResponse.setTppRedirectPreferred(tppRedirectPreferred);

        return paymentResponse;
    }

    private SpiPaymentInitialisationResponse mapToSpiPaymentResponseByPisConsentId(String pisConsentId, boolean tppRedirectPreferred) {
        SpiPaymentInitialisationResponse paymentResponse = new SpiPaymentInitialisationResponse();
        paymentResponse.setTransactionStatus(SpiTransactionStatus.RCVD);
        paymentResponse.setPaymentId(pisConsentId);
        paymentResponse.setTppRedirectPreferred(tppRedirectPreferred);

        return paymentResponse;
    }

    private String createPisConsent(SpiSinglePayments spiSinglePayments) {
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(remoteSpiUrls.createPisConsent(), new PisConsentRequest(spiSinglePayments), String.class);
        return responseEntity.getBody();
    }
}
