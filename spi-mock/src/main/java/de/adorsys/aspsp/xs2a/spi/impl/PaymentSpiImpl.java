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

import de.adorsys.aspsp.xs2a.service.mapper.consent.SpiCmsPisMapper;
import de.adorsys.aspsp.xs2a.spi.component.SpiMockJsonConverter;
import de.adorsys.aspsp.xs2a.spi.config.rest.AspspRemoteUrls;
import de.adorsys.aspsp.xs2a.spi.domain.SpiAspspAuthorisationData;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.aspsp.xs2a.spi.impl.service.KeycloakInvokerService;
import de.adorsys.aspsp.xs2a.spi.mapper.SpiPaymentMapper;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.xs2a.core.profile.PaymentProduct;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaMethod;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
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

import static de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus.FAILURE;
import static de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus.SUCCESS;
import static org.springframework.http.HttpStatus.CREATED;

@Component
@AllArgsConstructor
public class PaymentSpiImpl implements PaymentSpi {
    private final AspspRemoteUrls aspspRemoteUrls;
    @Qualifier("aspspRestTemplate")
    private final RestTemplate aspspRestTemplate;
    private final KeycloakInvokerService keycloakInvokerService;
    private final SpiMockJsonConverter jsonConverter;
    private final SpiPaymentMapper spiPaymentMapper;
    private final SpiCmsPisMapper spiCmsPisMapper;

    /**
     * For detailed description see {@link PaymentSpi#createPaymentInitiation(SpiSinglePayment, AspspConsentData)}
     */
    @Override
    public SpiResponse<SpiPaymentInitialisationResponse> createPaymentInitiation(SpiSinglePayment spiSinglePayment, AspspConsentData aspspConsentData) {
        ResponseEntity<SpiSinglePayment> responseEntity = aspspRestTemplate.postForEntity(aspspRemoteUrls.createPayment(), spiSinglePayment, SpiSinglePayment.class);

        SpiPaymentInitialisationResponse response =
            responseEntity.getStatusCode() == CREATED
                ? spiPaymentMapper.mapToSpiPaymentResponse(responseEntity.getBody())
                : spiPaymentMapper.mapToSpiPaymentResponse(spiSinglePayment);
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
                      .map(spiPaymentMapper::mapToSpiPaymentResponse)
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
                ? spiPaymentMapper.mapToSpiPaymentResponse(responseEntity.getBody())
                : spiPaymentMapper.mapToSpiPaymentResponse(periodicPayment);
        return new SpiResponse<>(response, aspspConsentData);
    }

    /**
     * For detailed description see {@link PaymentSpi#getPaymentStatusById(String, PaymentType, AspspConsentData)}
     */
    @Override
    public SpiResponse<SpiTransactionStatus> getPaymentStatusById(String paymentId, PaymentType paymentType, AspspConsentData aspspConsentData) {
        SpiTransactionStatus response = aspspRestTemplate.getForEntity(aspspRemoteUrls.getPaymentStatus(), SpiTransactionStatus.class, paymentId).getBody();
        return new SpiResponse<>(response, aspspConsentData);
    }

    /**
     * For detailed description see {@link PaymentSpi#getSinglePaymentById(PaymentType, PaymentProduct, String, AspspConsentData)}
     */
    @Override
    public SpiResponse<SpiSinglePayment> getSinglePaymentById(PaymentType paymentType, PaymentProduct paymentProduct, String paymentId, AspspConsentData aspspConsentData) {
        List<SpiSinglePayment> aspspResponse = aspspRestTemplate.exchange(aspspRemoteUrls.getPaymentById(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SpiSinglePayment>>() {
            },
            paymentType, paymentProduct, paymentId).getBody();
        SpiSinglePayment response = aspspResponse.get(0);
        return new SpiResponse<>(response, aspspConsentData);
    }

    /**
     * For detailed description see {@link PaymentSpi#getPeriodicPaymentById(PaymentType, PaymentProduct, String, AspspConsentData)}
     */
    @Override
    public SpiResponse<SpiPeriodicPayment> getPeriodicPaymentById(PaymentType paymentType, PaymentProduct paymentProduct, String paymentId, AspspConsentData aspspConsentData) {
        List<SpiPeriodicPayment> aspspResponse = aspspRestTemplate.exchange(aspspRemoteUrls.getPaymentById(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SpiPeriodicPayment>>() {
            },
            paymentType, paymentProduct, paymentId).getBody();

        SpiPeriodicPayment response = aspspResponse.get(0);
        return new SpiResponse<>(response, aspspConsentData);
    }

    /**
     * For detailed description see {@link PaymentSpi#getBulkPaymentById(PaymentType, PaymentProduct, String, AspspConsentData)}
     */
    @Override
    public SpiResponse<List<SpiSinglePayment>> getBulkPaymentById(PaymentType paymentType, PaymentProduct paymentProduct, String paymentId, AspspConsentData aspspConsentData) {
        List<SpiSinglePayment> aspspResponse = aspspRestTemplate.exchange(aspspRemoteUrls.getPaymentById(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SpiSinglePayment>>() {
            },
            paymentType, paymentProduct, paymentId).getBody();
        return new SpiResponse<>(aspspResponse, aspspConsentData);
    }

    /**
     * For detailed description see {@link PaymentSpi#authorisePsu(String, String, AspspConsentData)}
     */
    @Override
    public SpiResponse<SpiAuthorisationStatus> authorisePsu(String psuId, String password, AspspConsentData aspspConsentData) {
        Optional<SpiAspspAuthorisationData> accessToken = keycloakInvokerService.obtainAuthorisationData(psuId, password);
        SpiAuthorisationStatus spiAuthorisationStatus = accessToken.map(t -> SUCCESS)
                                                            .orElse(FAILURE);
        byte[] payload = accessToken.flatMap(jsonConverter::toJson)
                             .map(String::getBytes)
                             .orElse(null);
        return new SpiResponse<>(spiAuthorisationStatus, aspspConsentData.respondWith(payload));
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
                                               .orElseGet(Collections::emptyList);
        return new SpiResponse<>(spiScaMethods, aspspConsentData);
    }

    /**
     * For detailed description see {@link PaymentSpi#executePayment(PaymentType, List, AspspConsentData)}
     */
    @Override
    public SpiResponse<String> executePayment(PaymentType paymentType, List<PisPayment> payments, AspspConsentData aspspConsentData) {
        //TODO get rid of Cms dependent models shall be done in scope of https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332
        String paymentId;
        if (PaymentType.SINGLE == paymentType) {
            SpiPaymentInitialisationResponse paymentInitiation = createPaymentInitiation(spiCmsPisMapper.mapToSpiSinglePayment(payments.get(0)), aspspConsentData)
                                                                     .getPayload();
            paymentId = paymentInitiation.getPaymentId();
        } else if (PaymentType.PERIODIC == paymentType) {
            SpiPaymentInitialisationResponse paymentInitiation = initiatePeriodicPayment(spiCmsPisMapper.mapToSpiPeriodicPayment(payments.get(0)), aspspConsentData)
                                                                     .getPayload();
            paymentId = paymentInitiation.getPaymentId();
        } else {
            List<SpiPaymentInitialisationResponse> paymentInitiation = createBulkPayments(spiCmsPisMapper.mapToSpiBulkPayment(payments), aspspConsentData)
                                                                           .getPayload();
            paymentId = paymentInitiation.get(0).getPaymentId();
        }
        return new SpiResponse<>(paymentId, aspspConsentData);
    }

    /**
     * For detailed description see {@link PaymentSpi#performStrongUserAuthorisation(String, SpiScaMethod, AspspConsentData)}
     */
    @Override
    public SpiResponse<Void> performStrongUserAuthorisation(String psuId, SpiScaMethod choosenScaMethod, AspspConsentData aspspConsentData) {
        aspspRestTemplate.exchange(aspspRemoteUrls.getGenerateTanConfirmation(), HttpMethod.POST, null, Void.class, psuId, choosenScaMethod);
        return new SpiResponse<>(null, aspspConsentData);
    }

    @Override
    public SpiResponse<Void> applyStrongUserAuthorisation(SpiScaConfirmation confirmation, AspspConsentData aspspConsentData) {
        aspspRestTemplate.exchange(aspspRemoteUrls.applyStrongUserAuthorisation(), HttpMethod.PUT, new HttpEntity<>(confirmation), ResponseEntity.class);
        return new SpiResponse<>(null, aspspConsentData);
    }

}
