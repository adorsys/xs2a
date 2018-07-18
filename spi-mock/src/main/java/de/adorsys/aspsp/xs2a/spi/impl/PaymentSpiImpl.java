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
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentType;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
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
     * For detailed description see {@link PaymentSpi#createPaymentInitiation(SpiSinglePayments)}
     */
    @Override
    public SpiPaymentInitialisationResponse createPaymentInitiation(SpiSinglePayments spiSinglePayments) {
        ResponseEntity<SpiSinglePayments> responseEntity = aspspRestTemplate.postForEntity(aspspRemoteUrls.createPayment(), spiSinglePayments, SpiSinglePayments.class);
        return responseEntity.getStatusCode() == CREATED
                   ? mapToSpiPaymentResponse(responseEntity.getBody())
                   : null;
    }

    /**
     * For detailed description see {@link PaymentSpi#createBulkPayments(List)}
     */
    @Override
    public List<SpiPaymentInitialisationResponse> createBulkPayments(List<SpiSinglePayments> payments) {
        ResponseEntity<List<SpiSinglePayments>> responseEntity = aspspRestTemplate.exchange(aspspRemoteUrls.createBulkPayment(), HttpMethod.POST, new HttpEntity<>(payments, null), new ParameterizedTypeReference<List<SpiSinglePayments>>() {
        });
        return (responseEntity.getStatusCode() == CREATED)
                   ? responseEntity.getBody().stream()
                         .map(this::mapToSpiPaymentResponse)
                         .collect(Collectors.toList())
                   : Collections.emptyList();
    }

    /**
     * For detailed description see {@link PaymentSpi#initiatePeriodicPayment(SpiPeriodicPayment)}
     */
    @Override
    public SpiPaymentInitialisationResponse initiatePeriodicPayment(SpiPeriodicPayment periodicPayment) {
        ResponseEntity<SpiPeriodicPayment> responseEntity = aspspRestTemplate.postForEntity(aspspRemoteUrls.createPeriodicPayment(), periodicPayment, SpiPeriodicPayment.class);
        return responseEntity.getStatusCode() == CREATED
                   ? mapToSpiPaymentResponse(responseEntity.getBody())
                   : null;
    }

    /**
     * For detailed description see {@link PaymentSpi#getPaymentStatusById(String, String)}
     */
    @Override
    public SpiTransactionStatus getPaymentStatusById(String paymentId, String paymentProduct) {
        return aspspRestTemplate.getForEntity(aspspRemoteUrls.getPaymentStatus(), SpiTransactionStatus.class, paymentId).getBody();
    }

    @Override
    public SpiSinglePayments getSinglePaymentById(SpiPaymentType paymentType, String paymentProduct, String paymentId) {
        return aspspRestTemplate.getForObject(aspspRemoteUrls.getPaymentById(), SpiSinglePayments.class, paymentType, paymentProduct, paymentId);
    }

    @Override
    public SpiPeriodicPayment getPeriodicPaymentById(SpiPaymentType paymentType, String paymentProduct, String paymentId) {
        return aspspRestTemplate.getForObject(aspspRemoteUrls.getPaymentById(), SpiPeriodicPayment.class, paymentType, paymentProduct, paymentId);
    }

    @Override
    public List<SpiSinglePayments> getBulkPaymentById(SpiPaymentType paymentType, String paymentProduct, String paymentId) {
        SpiSinglePayments response = aspspRestTemplate.getForObject(aspspRemoteUrls.getPaymentById(), SpiSinglePayments.class, paymentType, paymentProduct, paymentId);
        return Optional.ofNullable(response)
                   .map(Collections::singletonList)
                   .orElse(null);
    }

    private SpiPaymentInitialisationResponse mapToSpiPaymentResponse(SpiSinglePayments spiSinglePayments) {
        SpiPaymentInitialisationResponse paymentResponse = new SpiPaymentInitialisationResponse();
        paymentResponse.setTransactionStatus(SpiTransactionStatus.RCVD);
        paymentResponse.setPaymentId(spiSinglePayments.getPaymentId());
        return paymentResponse;
    }
}
