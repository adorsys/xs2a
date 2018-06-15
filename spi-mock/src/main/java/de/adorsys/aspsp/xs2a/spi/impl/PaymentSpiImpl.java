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

import de.adorsys.aspsp.xs2a.spi.config.AspspRemoteUrls;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.pis.PisConsentBulkPaymentRequest;
import de.adorsys.aspsp.xs2a.spi.domain.consent.pis.PisConsentPeriodicPaymentRequest;
import de.adorsys.aspsp.xs2a.spi.domain.consent.pis.PisConsentRequest;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.CREATED;

@Component
@AllArgsConstructor
public class PaymentSpiImpl implements PaymentSpi {
    private final AspspRemoteUrls aspspRemoteUrls;
    @Qualifier("aspspRestTemplate")
    private final RestTemplate aspspRestTemplate;

    private final boolean redirectMode = true; // todo remake business logic according task https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/100

    @Override
    public SpiPaymentInitialisationResponse createPaymentInitiation(SpiSinglePayments spiSinglePayments, String paymentProduct, boolean tppRedirectPreferred) {
        return redirectMode
                   ? singlePaymentForRedirectMode(spiSinglePayments, tppRedirectPreferred)
                   : singlePaymentForOauthMode(spiSinglePayments, tppRedirectPreferred);
    }

    @Override
    public List<SpiPaymentInitialisationResponse> createBulkPayments(List<SpiSinglePayments> payments, String paymentProduct, boolean tppRedirectPreferred) {
        return redirectMode
                   ? bulkPaymentForRedirectMode(payments, paymentProduct, tppRedirectPreferred)
                   : bulkPaymentForOauthMode(payments, paymentProduct, tppRedirectPreferred);
    }

    @Override
    public SpiPaymentInitialisationResponse initiatePeriodicPayment(SpiPeriodicPayment periodicPayment, String paymentProduct, boolean tppRedirectPreferred) {
        return redirectMode
                   ? periodicPaymentForRedirectMode(periodicPayment, paymentProduct, tppRedirectPreferred)
                   : periodicPaymentForOauthMode(periodicPayment, paymentProduct, tppRedirectPreferred);
    }

    @Override
    public SpiTransactionStatus getPaymentStatusById(String paymentId, String paymentProduct) {
        return aspspRestTemplate.getForEntity(aspspRemoteUrls.getPaymentStatus(), SpiTransactionStatus.class, paymentId).getBody();
    }

    private SpiPaymentInitialisationResponse singlePaymentForRedirectMode(SpiSinglePayments spiSinglePayments, boolean tppRedirectPreferred) {
        String pisConsentId = createPisConsent(spiSinglePayments);

        return !StringUtils.isBlank(pisConsentId)
                   ? createSinglePaymentAndGetResponse(spiSinglePayments, tppRedirectPreferred)
                   : null;
    }

    private SpiPaymentInitialisationResponse singlePaymentForOauthMode(SpiSinglePayments spiSinglePayments, boolean tppRedirectPreferred) {
        return createSinglePaymentAndGetResponse(spiSinglePayments, tppRedirectPreferred);
    }

    private List<SpiPaymentInitialisationResponse> bulkPaymentForRedirectMode(List<SpiSinglePayments> payments, String paymentProduct, boolean tppRedirectPreferred) {
        String pisConsentId = createPisConsentForBulkPayment(payments);

        return !StringUtils.isBlank(pisConsentId)
                   ? createBulkPaymentAndGetResponse(payments, paymentProduct, tppRedirectPreferred)
                   : null;
    }

    private List<SpiPaymentInitialisationResponse> bulkPaymentForOauthMode(List<SpiSinglePayments> payments, String paymentProduct, boolean tppRedirectPreferred) {
        return createBulkPaymentAndGetResponse(payments, paymentProduct, tppRedirectPreferred);
    }

    private SpiPaymentInitialisationResponse periodicPaymentForRedirectMode(SpiPeriodicPayment periodicPayment, String paymentProduct, boolean tppRedirectPreferred) {
        String pisConsentId = createPisConsentForPeriodicPayment(periodicPayment);

        return !StringUtils.isBlank(pisConsentId)
                   ? createPeriodicPaymentAndGetResponse(periodicPayment, paymentProduct, tppRedirectPreferred)
                   : null;
    }

    private SpiPaymentInitialisationResponse periodicPaymentForOauthMode(SpiPeriodicPayment periodicPayment, String paymentProduct, boolean tppRedirectPreferred) {
        return createPeriodicPaymentAndGetResponse(periodicPayment, paymentProduct, tppRedirectPreferred);
    }

    private SpiPaymentInitialisationResponse createSinglePaymentAndGetResponse(SpiSinglePayments spiSinglePayments, boolean tppRedirectPreferred) {
        ResponseEntity<SpiSinglePayments> responseEntity = aspspRestTemplate.postForEntity(aspspRemoteUrls.createPayment(), spiSinglePayments, SpiSinglePayments.class);
        return responseEntity.getStatusCode() == CREATED
                   ? mapToSpiPaymentResponse(responseEntity.getBody(), tppRedirectPreferred)
                   : null;
    }

    public List<SpiPaymentInitialisationResponse> createBulkPaymentAndGetResponse(List<SpiSinglePayments> payments, String paymentProduct, boolean tppRedirectPreferred) {
        ResponseEntity<List<SpiSinglePayments>> responseEntity = aspspRestTemplate.exchange(aspspRemoteUrls.createBulkPayment(), HttpMethod.POST, new HttpEntity<>(payments, null), new ParameterizedTypeReference<List<SpiSinglePayments>>() {
        });
        return (responseEntity.getStatusCode() == CREATED)
                   ? responseEntity.getBody().stream()
                         .map(spiPaym -> mapToSpiPaymentResponse(spiPaym, tppRedirectPreferred))
                         .collect(Collectors.toList())
                   : Collections.emptyList();
    }

    public SpiPaymentInitialisationResponse createPeriodicPaymentAndGetResponse(SpiPeriodicPayment periodicPayment, String paymentProduct, boolean tppRedirectPreferred) {
        ResponseEntity<SpiPeriodicPayment> responseEntity = aspspRestTemplate.postForEntity(aspspRemoteUrls.createPeriodicPayment(), periodicPayment, SpiPeriodicPayment.class);
        return responseEntity.getStatusCode() == CREATED ? mapToSpiPaymentResponse(responseEntity.getBody(), tppRedirectPreferred) : null;
    }

    private SpiPaymentInitialisationResponse mapToSpiPaymentResponse(SpiSinglePayments spiSinglePayments, boolean tppRedirectPreferred) {
        SpiPaymentInitialisationResponse paymentResponse = new SpiPaymentInitialisationResponse();
        paymentResponse.setTransactionStatus(SpiTransactionStatus.RCVD);
        paymentResponse.setPaymentId(spiSinglePayments.getPaymentId());
        paymentResponse.setTppRedirectPreferred(tppRedirectPreferred);

        return paymentResponse;
    }

    private String createPisConsent(SpiSinglePayments spiSinglePayments) {
        ResponseEntity<String> responseEntity = aspspRestTemplate.postForEntity(aspspRemoteUrls.createPisConsent(), new PisConsentRequest(spiSinglePayments), String.class);
        return responseEntity.getBody();
    }

    private String createPisConsentForBulkPayment(List<SpiSinglePayments> payments) {
        ResponseEntity<String> responseEntity = aspspRestTemplate.postForEntity(aspspRemoteUrls.createPisBulkPaymentConsent(), new PisConsentBulkPaymentRequest(payments), String.class);
        return responseEntity.getBody();
    }

    private String createPisConsentForPeriodicPayment(SpiPeriodicPayment periodicPayment) {
        ResponseEntity<String> responseEntity = aspspRestTemplate.postForEntity(aspspRemoteUrls.createPisPeriodicPaymentConsent(), new PisConsentPeriodicPaymentRequest(periodicPayment), String.class);
        return responseEntity.getBody();
    }
}
