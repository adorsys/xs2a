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

import de.adorsys.aspsp.xs2a.component.JsonConverter;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPayment;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPaymentType;
import de.adorsys.aspsp.xs2a.domain.security.AspspAuthorisationData;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aPisConsentMapper;
import de.adorsys.aspsp.xs2a.spi.config.rest.AspspRemoteUrls;
import de.adorsys.aspsp.xs2a.spi.domain.SpiResponse;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.payment.*;
import de.adorsys.aspsp.xs2a.spi.domain.psu.SpiScaMethod;
import de.adorsys.aspsp.xs2a.spi.impl.service.KeycloakInvokerService;
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

import static de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiAuthorisationStatus.FAILURE;
import static de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiAuthorisationStatus.SUCCESS;
import static org.springframework.http.HttpStatus.CREATED;

@Component
@AllArgsConstructor
public class PaymentSpiImpl implements PaymentSpi {
    private final AspspRemoteUrls aspspRemoteUrls;
    @Qualifier("aspspRestTemplate")
    private final RestTemplate aspspRestTemplate;
    private final KeycloakInvokerService keycloakInvokerService;
    private final Xs2aPisConsentMapper xs2aPisConsentMapper;
    private final JsonConverter jsonConverter;

    /**
     * For detailed description see {@link PaymentSpi#createPaymentInitiation(SpiSinglePayment, AspspConsentData)}
     */
    @Override
    public SpiResponse<SpiPaymentInitialisationResponse> createPaymentInitiation(SpiSinglePayment spiSinglePayment, AspspConsentData aspspConsentData) {
        ResponseEntity<SpiSinglePayment> responseEntity = aspspRestTemplate.postForEntity(aspspRemoteUrls.createPayment(), spiSinglePayment, SpiSinglePayment.class);
        return getSpiPaymentInitialisationResponseSpiResponse(spiSinglePayment, responseEntity, aspspConsentData);
    }

    private SpiResponse<SpiPaymentInitialisationResponse> getSpiPaymentInitialisationResponseSpiResponse(SpiSinglePayment spiSinglePayment, ResponseEntity<SpiSinglePayment> responseEntity, AspspConsentData aspspConsentData) {
        SpiPaymentInitialisationResponse response =
            responseEntity.getStatusCode() == CREATED
                ? mapToSpiPaymentResponse(responseEntity.getBody())
                : mapToSpiPaymentResponse(spiSinglePayment);
        return new SpiResponse<>(response, aspspConsentData);
    }

    /**
     * For detailed description see {@link PaymentSpi#createBulkPayments(SpiBulkPayment, AspspConsentData)}
     */
    @Override
    public SpiResponse<List<SpiPaymentInitialisationResponse>> createBulkPayments(SpiBulkPayment spiBulkPayment, AspspConsentData aspspConsentData) {
        ResponseEntity<List<SpiSinglePayment>> responseEntity = aspspRestTemplate.exchange(aspspRemoteUrls.createBulkPayment(), HttpMethod.POST,
            new HttpEntity<>(spiBulkPayment, null), new ParameterizedTypeReference<List<SpiSinglePayment>>() {
            });
        List<SpiPaymentInitialisationResponse> response =
            (responseEntity.getStatusCode() == CREATED)
                ? responseEntity.getBody().stream()
                      .map(this::mapToSpiPaymentResponse)
                      .collect(Collectors.toList())
                : Collections.emptyList();
        return new SpiResponse<>(response, aspspConsentData);
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
                : mapToSpiPaymentResponse(periodicPayment);
        return new SpiResponse<>(response, aspspConsentData);
    }

    /**
     * For detailed description see {@link PaymentSpi#getPaymentStatusById(String, SpiPaymentType, AspspConsentData)}
     */
    @Override
    public SpiResponse<SpiTransactionStatus> getPaymentStatusById(String paymentId, SpiPaymentType paymentType, AspspConsentData aspspConsentData) {
        SpiTransactionStatus response = aspspRestTemplate.getForEntity(aspspRemoteUrls.getPaymentStatus(), SpiTransactionStatus.class, paymentId).getBody();
        return new SpiResponse<>(response, aspspConsentData);
    }

    /**
     * For detailed description see {@link PaymentSpi#getSinglePaymentById(SpiPaymentType, String, String, AspspConsentData)}
     */
    @Override
    public SpiResponse<SpiSinglePayment> getSinglePaymentById(SpiPaymentType paymentType, String paymentProduct, String paymentId, AspspConsentData aspspConsentData) {
        List<SpiSinglePayment> aspspResponse = aspspRestTemplate.exchange(aspspRemoteUrls.getPaymentById(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SpiSinglePayment>>() {},
            paymentType, paymentProduct, paymentId).getBody();
        SpiSinglePayment response = aspspResponse.get(0);
        return new SpiResponse<>(response, aspspConsentData);
    }

    /**
     * For detailed description see {@link PaymentSpi#getPeriodicPaymentById(SpiPaymentType, String, String, AspspConsentData)}
     */
    @Override
    public SpiResponse<SpiPeriodicPayment> getPeriodicPaymentById(SpiPaymentType paymentType, String paymentProduct, String paymentId, AspspConsentData aspspConsentData) {
        List<SpiPeriodicPayment> aspspResponse = aspspRestTemplate.exchange(aspspRemoteUrls.getPaymentById(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SpiPeriodicPayment>>() {},
            paymentType, paymentProduct, paymentId).getBody();

        SpiPeriodicPayment response = aspspResponse.get(0);
        return new SpiResponse<>(response, aspspConsentData);
    }

    /**
     * For detailed description see {@link PaymentSpi#getBulkPaymentById(SpiPaymentType, String, String, AspspConsentData)}
     */
    @Override
    public SpiResponse<List<SpiSinglePayment>> getBulkPaymentById(SpiPaymentType paymentType, String paymentProduct, String paymentId, AspspConsentData aspspConsentData) {
        List<SpiSinglePayment> aspspResponse = aspspRestTemplate.exchange(aspspRemoteUrls.getPaymentById(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SpiSinglePayment>>() {},
            paymentType, paymentProduct, paymentId).getBody();
        return new SpiResponse<>(aspspResponse, aspspConsentData);
    }

    /**
     * For detailed description see {@link PaymentSpi#authorisePsu(String, String, AspspConsentData)}
     */
    @Override
    public SpiResponse<SpiAuthorisationStatus> authorisePsu(String psuId, String password, AspspConsentData aspspConsentData) {
        Optional<AspspAuthorisationData> accessToken = keycloakInvokerService.obtainAuthorisationData(psuId, password);
        SpiAuthorisationStatus spiAuthorisationStatus = accessToken.map(t -> SUCCESS)
                                                            .orElse(FAILURE);
        byte[] payload = accessToken.flatMap(jsonConverter::toJson)
                             .map(String::getBytes)
                             .orElse(null);
        return new SpiResponse<>(spiAuthorisationStatus, new AspspConsentData(payload));
    }

    /**
     * For detailed description see {@link PaymentSpi#readAvailableScaMethod(String, AspspConsentData)}
     */
    @Override
    public SpiResponse<List<SpiScaMethod>> readAvailableScaMethod(String psuId, AspspConsentData aspspConsentData) {
        ResponseEntity<List<SpiScaMethod>> response = aspspRestTemplate.exchange(
            aspspRemoteUrls.getScaMethods(), HttpMethod.GET, null, new ParameterizedTypeReference<List<SpiScaMethod>>() {
            }, psuId);
        List<SpiScaMethod> spiScaMethods = Optional.ofNullable(response.getBody())
                                               .orElse(Collections.emptyList());
        return new SpiResponse<>(spiScaMethods, aspspConsentData);
    }

    /**
     * For detailed description see {@link PaymentSpi#executePayment(PisPaymentType, List, AspspConsentData)}
     */
    @Override
    public SpiResponse<String> executePayment(PisPaymentType paymentType, List<PisPayment> payments, AspspConsentData aspspConsentData) {
        String paymentId = null;
        if (PisPaymentType.SINGLE == paymentType) {
            SpiPaymentInitialisationResponse paymentInitiation = createPaymentInitiation(xs2aPisConsentMapper.mapToSpiSinglePayment(payments.get(0)), aspspConsentData)
                                                                     .getPayload();
            paymentId = paymentInitiation.getPaymentId();
        }
        return new SpiResponse<>(paymentId, aspspConsentData);
    }

    /**
     * For detailed description see {@link PaymentSpi#performStrongUserAuthorisation(String, AspspConsentData)}
     */
    @Override
    public void performStrongUserAuthorisation(String psuId, AspspConsentData aspspConsentData) {
        aspspRestTemplate.exchange(aspspRemoteUrls.getGenerateTanConfirmation(), HttpMethod.POST, null, Void.class, psuId);
    }

    @Override
    public void applyStrongUserAuthorisation(SpiPaymentConfirmation confirmation, AspspConsentData aspspConsentData) {
        aspspRestTemplate.exchange(aspspRemoteUrls.applyStrongUserAuthorisation(), HttpMethod.PUT, new HttpEntity<>(confirmation), ResponseEntity.class);
    }

    private SpiPaymentInitialisationResponse mapToSpiPaymentResponse(SpiSinglePayment spiSinglePayment) {
        SpiPaymentInitialisationResponse paymentResponse = new SpiPaymentInitialisationResponse();
        paymentResponse.setSpiTransactionFees(null);
        paymentResponse.setSpiTransactionFeeIndicator(false);
        paymentResponse.setScaMethods(null);
        paymentResponse.setTppRedirectPreferred(false);
        if (spiSinglePayment.getPaymentId() == null) {
            paymentResponse.setTransactionStatus(SpiTransactionStatus.RJCT);
            paymentResponse.setPaymentId(spiSinglePayment.getEndToEndIdentification());
            paymentResponse.setPsuMessage(null);
            paymentResponse.setTppMessages(new String[]{"PAYMENT_FAILED"});
        } else {
            paymentResponse.setTransactionStatus(SpiTransactionStatus.RCVD);
            paymentResponse.setPaymentId(spiSinglePayment.getPaymentId());
        }
        return paymentResponse;
    }
}
