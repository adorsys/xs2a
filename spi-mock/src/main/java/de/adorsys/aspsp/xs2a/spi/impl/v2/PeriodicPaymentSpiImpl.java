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

package de.adorsys.aspsp.xs2a.spi.impl.v2;

import de.adorsys.aspsp.xs2a.component.JsonConverter;
import de.adorsys.aspsp.xs2a.spi.config.rest.AspspRemoteUrls;
import de.adorsys.aspsp.xs2a.spi.domain.SpiResponse;
import de.adorsys.aspsp.xs2a.spi.domain.SpiResponseStatus;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiAspspAuthorisationData;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiScaMethod;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentType;
import de.adorsys.aspsp.xs2a.spi.domain.v2.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.impl.service.KeycloakInvokerService;
import de.adorsys.aspsp.xs2a.spi.mapper.SpiPaymentMapper;
import de.adorsys.aspsp.xs2a.spi.mapper.v2.NewSpiPaymentMapper;
import de.adorsys.aspsp.xs2a.spi.service.v2.PeriodicPaymentSpi;
import org.jetbrains.annotations.NotNull;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiAuthorisationStatus.FAILURE;
import static de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiAuthorisationStatus.SUCCESS;

@Service
@AllArgsConstructor
public class PeriodicPaymentSpiImpl implements PeriodicPaymentSpi {
    @Qualifier("aspspRestTemplate")
    private final RestTemplate aspspRestTemplate;
    private final NewSpiPaymentMapper newSpiPaymentMapper;
    private final SpiPaymentMapper spiPaymentMapper;
    private final AspspRemoteUrls aspspRemoteUrls;
    private final KeycloakInvokerService keycloakInvokerService;
    private final JsonConverter jsonConverter;

    @NotNull
    @NotNull
    @Override
    public SpiResponse<SpiPaymentInitialisationResponse> initiatePayment(SpiPeriodicPayment payment, AspspConsentData initialAspspConsentData) {
        de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment request = newSpiPaymentMapper.mapToAspspSpiPeriodicPayment(payment);
        ResponseEntity<de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment> aspspResponse =
            aspspRestTemplate.postForEntity(aspspRemoteUrls.createPeriodicPayment(), request, de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment.class);

        if (aspspResponse.getStatusCode() != HttpStatus.CREATED) {
            return SpiResponse.<SpiPaymentInitialisationResponse>builder().fail(SpiResponseStatus.TECHNICAL_FAILURE);
        }

        SpiPaymentInitialisationResponse spiPaymentInitialisationResponse = spiPaymentMapper.mapToSpiPaymentResponse(aspspResponse.getBody());

        return new SpiResponse<>(spiPaymentInitialisationResponse, initialAspspConsentData);
    public SpiResponse<SpiPaymentInitialisationResponse> initiatePayment(@NotNull SpiPeriodicPayment payment, @NotNull AspspConsentData initialAspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse executePaymentWithoutSca(SpiPaymentType spiPaymentType, SpiPeriodicPayment payment, AspspConsentData aspspConsentData) {
        //TODO Rework after purpose is clarified
        return initiatePayment(payment, aspspConsentData);
    public SpiResponse<SpiResponse.VoidResponse> executePaymentWithoutSca(SpiPaymentType spiPaymentType, SpiPeriodicPayment payment, AspspConsentData aspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse<SpiPeriodicPayment> getPaymentById(SpiPeriodicPayment payment, AspspConsentData aspspConsentData) {
        ResponseEntity<List<de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment>> aspspResponse =
            aspspRestTemplate.exchange(aspspRemoteUrls.getPaymentById(), HttpMethod.GET, null, new ParameterizedTypeReference<List<de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment>>() {
            }, payment.getPaymentType(), payment.getPaymentProduct(), payment.getPaymentId());

        if (aspspResponse.getStatusCode() != HttpStatus.OK) {
            return SpiResponse.<SpiPeriodicPayment>builder().fail(SpiResponseStatus.TECHNICAL_FAILURE);
        }

        de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment periodic = aspspResponse.getBody().get(0);
        SpiPeriodicPayment spiPeriodicPayment = newSpiPaymentMapper.mapToSpiPeriodicPayment(periodic, payment.getPaymentProduct());

        return new SpiResponse<>(spiPeriodicPayment, aspspConsentData);
    }

    @Override
    public SpiResponse<SpiTransactionStatus> getPaymentStatusById(SpiPeriodicPayment payment, AspspConsentData aspspConsentData) {
        ResponseEntity<SpiTransactionStatus> aspspResponse = aspspRestTemplate.getForEntity(aspspRemoteUrls.getPaymentStatus(), SpiTransactionStatus.class, payment.getPaymentId());

        if (aspspResponse.getStatusCode() != HttpStatus.OK) {
            return SpiResponse.<SpiTransactionStatus>builder().fail(SpiResponseStatus.TECHNICAL_FAILURE);
        }

        SpiTransactionStatus status = aspspResponse.getBody();

        return new SpiResponse<>(status, aspspConsentData);
    }

    @Override
    public SpiResponse<SpiAuthorisationStatus> authorisePsu(String psuId, String password, SpiPeriodicPayment payment, AspspConsentData aspspConsentData) {
        Optional<SpiAspspAuthorisationData> accessToken = keycloakInvokerService.obtainAuthorisationData(psuId, password);
        SpiAuthorisationStatus spiAuthorisationStatus = accessToken.map(t -> SUCCESS)
                                                            .orElse(FAILURE);
        byte[] payload = accessToken.flatMap(jsonConverter::toJson)
                             .map(String::getBytes)
                             .orElse(null);

        return new SpiResponse<>(spiAuthorisationStatus, aspspConsentData.respondWith(payload));
    }

    @Override
    public SpiResponse<List<SpiScaMethod>> requestAvailableScaMethods(String psuId, SpiPeriodicPayment payment, AspspConsentData aspspConsentData) {
        ResponseEntity<List<SpiScaMethod>> aspspResponse = aspspRestTemplate.exchange(aspspRemoteUrls.getScaMethods(), HttpMethod.GET, null, new ParameterizedTypeReference<List<SpiScaMethod>>() {
        }, psuId);

        if (!EnumSet.of(HttpStatus.OK, HttpStatus.NO_CONTENT).contains(aspspResponse.getStatusCode())) {
            return SpiResponse.<List<SpiScaMethod>>builder().fail(SpiResponseStatus.TECHNICAL_FAILURE);
        }

        List<SpiScaMethod> spiScaMethods = Optional.ofNullable(aspspResponse.getBody())
                                               .orElseGet(Collections::emptyList);

        return new SpiResponse<>(spiScaMethods, aspspConsentData);
    }

    @Override
    public SpiResponse requestAuthorisationCode(String psuId, SpiScaMethod scaMethod, SpiPeriodicPayment payment, AspspConsentData aspspConsentData) {
        ResponseEntity<Void> aspspResponse = aspspRestTemplate.exchange(aspspRemoteUrls.getGenerateTanConfirmation(), HttpMethod.POST, null, Void.class, psuId, scaMethod);

        if (aspspResponse.getStatusCode() != HttpStatus.OK) {
            return SpiResponse.builder().fail(SpiResponseStatus.TECHNICAL_FAILURE);
        }

        return new SpiResponse<>(null, aspspConsentData);
    public SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(String psuId, SpiScaMethod scaMethod, SpiPeriodicPayment businessObject, AspspConsentData aspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse verifyAuthorisationCodeAndExecuteRequest(SpiScaConfirmation spiScaConfirmation, SpiPeriodicPayment payment, AspspConsentData aspspConsentData) {
        ResponseEntity<ResponseEntity> aspspResponse = aspspRestTemplate.exchange(aspspRemoteUrls.applyStrongUserAuthorisation(), HttpMethod.PUT, new HttpEntity<>(spiScaConfirmation), ResponseEntity.class);

        if (aspspResponse.getStatusCode() != HttpStatus.OK) {
            return SpiResponse.builder().fail(SpiResponseStatus.TECHNICAL_FAILURE);
        }

        return new SpiResponse<>(null, aspspConsentData);
    public SpiResponse<SpiResponse.VoidResponse> verifyAuthorisationCodeAndExecuteRequest(SpiScaConfirmation spiScaConfirmation, SpiPeriodicPayment businessObject, AspspConsentData aspspConsentData) {
        return null;
    }
}
