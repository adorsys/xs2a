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
import de.adorsys.aspsp.xs2a.spi.domain.SpiResponse;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentType;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.aspsp.xs2a.spi.domain.psu.SpiScaMethod;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.CREATED;

@Component
@AllArgsConstructor
public class PaymentSpiImpl implements PaymentSpi {
    private final AspspRemoteUrls aspspRemoteUrls;
    @Qualifier("aspspRestTemplate")
    private final RestTemplate aspspRestTemplate;

    /**
     * For detailed description see {@link PaymentSpi#createPaymentInitiation(SpiSinglePayment, AspspConsentData)}
     */
    @Override
    public SpiResponse<SpiPaymentInitialisationResponse> createPaymentInitiation(SpiSinglePayment spiSinglePayment, AspspConsentData aspspConsentData) {
        ResponseEntity<SpiSinglePayment> responseEntity = aspspRestTemplate.postForEntity(aspspRemoteUrls.createPayment(), spiSinglePayment, SpiSinglePayment.class);
        SpiPaymentInitialisationResponse response =
            responseEntity.getStatusCode() == CREATED
                ? mapToSpiPaymentResponse(responseEntity.getBody())
                : null;
        return new SpiResponse<>(response, new AspspConsentData()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
    }

    /**
     * For detailed description see {@link PaymentSpi#createBulkPayments(List, AspspConsentData)}
     */
    @Override
    public SpiResponse<List<SpiPaymentInitialisationResponse>> createBulkPayments(List<SpiSinglePayment> payments, AspspConsentData aspspConsentData) {
        ResponseEntity<List<SpiSinglePayment>> responseEntity = aspspRestTemplate.exchange(aspspRemoteUrls.createBulkPayment(), HttpMethod.POST, new HttpEntity<>(payments, null), new ParameterizedTypeReference<List<SpiSinglePayment>>() {
        });
        List<SpiPaymentInitialisationResponse> response =
            (responseEntity.getStatusCode() == CREATED)
                ? responseEntity.getBody().stream()
                      .map(this::mapToSpiPaymentResponse)
                      .collect(Collectors.toList())
                : Collections.emptyList();
        return new SpiResponse<>(response, new AspspConsentData()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
    }

    /**
     * For detailed description see {@link PaymentSpi#initiatePeriodicPayment(SpiPeriodicPayment, AspspConsentData)}
     */
    @Override
    public SpiResponse<SpiPaymentInitialisationResponse> initiatePeriodicPayment(SpiPeriodicPayment periodicPayment, AspspConsentData aspspConsentData) {
        ResponseEntity<SpiPeriodicPayment> responseEntity = aspspRestTemplate.postForEntity(aspspRemoteUrls.createPeriodicPayment(), periodicPayment, SpiPeriodicPayment.class);
        SpiPaymentInitialisationResponse response =
            responseEntity.getStatusCode() == CREATED
                ? mapToSpiPaymentResponse(responseEntity.getBody())
                : null;
        return new SpiResponse<>(response, new AspspConsentData()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
    }

    /**
     * For detailed description see {@link PaymentSpi#getPaymentStatusById(String, SpiPaymentType, AspspConsentData)}
     */
    @Override
    public SpiResponse<SpiTransactionStatus> getPaymentStatusById(String paymentId, SpiPaymentType paymentType, AspspConsentData aspspConsentData) {
        SpiTransactionStatus response = aspspRestTemplate.getForEntity(aspspRemoteUrls.getPaymentStatus(), SpiTransactionStatus.class, paymentId).getBody();
        return new SpiResponse<>(response, new AspspConsentData()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
    }

    @Override
    public SpiResponse<SpiSinglePayment> getSinglePaymentById(SpiPaymentType paymentType, String paymentProduct, String paymentId, AspspConsentData aspspConsentData) {
        SpiSinglePayment response = aspspRestTemplate.getForObject(aspspRemoteUrls.getPaymentById(), SpiSinglePayment.class, paymentType, paymentProduct, paymentId);
        return new SpiResponse<>(response,  new AspspConsentData()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
    }

    @Override
    public SpiResponse<SpiPeriodicPayment> getPeriodicPaymentById(SpiPaymentType paymentType, String paymentProduct, String paymentId, AspspConsentData aspspConsentData) {
        SpiPeriodicPayment response = aspspRestTemplate.getForObject(aspspRemoteUrls.getPaymentById(), SpiPeriodicPayment.class, paymentType, paymentProduct, paymentId);
        return new SpiResponse<>(response,  new AspspConsentData()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
    }

    @Override
    public SpiResponse<List<SpiSinglePayment>> getBulkPaymentById(SpiPaymentType paymentType, String paymentProduct, String paymentId, AspspConsentData aspspConsentData) {
        SpiSinglePayment aspspResponse = aspspRestTemplate.getForObject(aspspRemoteUrls.getPaymentById(), SpiSinglePayment.class, paymentType, paymentProduct, paymentId);
        List<SpiSinglePayment> response =
            Optional.ofNullable(aspspResponse)
                .map(Collections::singletonList)
                .orElse(null);
        return new SpiResponse<>(response,  new AspspConsentData()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
    }

    @Override
    public SpiResponse<List<SpiScaMethod>> getScaMethodsByName(String name) {
        ResponseEntity<List<SpiScaMethod>> response = aspspRestTemplate.exchange(aspspRemoteUrls.getScaMethods(), HttpMethod.GET, null, new ParameterizedTypeReference<List<SpiScaMethod>>() {
        });
        return new SpiResponse<>(response.getBody(), new AspspConsentData());
    }

    private SpiPaymentInitialisationResponse mapToSpiPaymentResponse(SpiSinglePayment spiSinglePayment) {
        SpiPaymentInitialisationResponse paymentResponse = new SpiPaymentInitialisationResponse();
        paymentResponse.setTransactionStatus(SpiTransactionStatus.RCVD);
        paymentResponse.setPaymentId(spiSinglePayment.getPaymentId());
        return paymentResponse;
    }
}
