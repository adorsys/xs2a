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
import de.adorsys.aspsp.xs2a.exception.RestException;
import de.adorsys.aspsp.xs2a.spi.config.rest.AspspRemoteUrls;
import de.adorsys.aspsp.xs2a.spi.domain.SpiAspspAuthorisationData;
import de.adorsys.aspsp.xs2a.spi.impl.service.KeycloakInvokerService;
import de.adorsys.aspsp.xs2a.spi.mapper.SpiPeriodicPaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaMethod;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.PaymentAuthorisationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.RequiredArgsConstructor;
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
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus.FAILURE;
import static de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus.SUCCESS;

@Service
@RequiredArgsConstructor
public class PaymentAuthorisationSpiImpl implements PaymentAuthorisationSpi<SpiPayment> {
    @Qualifier("aspspRestTemplate")
    private final RestTemplate aspspRestTemplate;
    private final AspspRemoteUrls aspspRemoteUrls;
    private final SpiPeriodicPaymentMapper spiPeriodicPaymentMapper;
    private final KeycloakInvokerService keycloakInvokerService;
    private final JsonConverter jsonConverter;

    @Override
    @NotNull
    public SpiResponse<SpiResponse.VoidResponse> executePaymentWithoutSca(@NotNull SpiPsuData psuData, @NotNull SpiPayment payment, @NotNull AspspConsentData aspspConsentData) {
        return SpiResponse.<SpiResponse.VoidResponse>builder().fail(SpiResponseStatus.NOT_SUPPORTED);
    }

    @Override
    @NotNull
    public SpiResponse<SpiAuthorisationStatus> authorisePsu(@NotNull SpiPsuData psuData, String password, SpiPayment businessObject, AspspConsentData aspspConsentData) {
        Optional<SpiAspspAuthorisationData> accessToken = keycloakInvokerService.obtainAuthorisationData(psuData.getPsuId(), password);
        SpiAuthorisationStatus spiAuthorisationStatus = accessToken.map(t -> SUCCESS)
                                                            .orElse(FAILURE);
        byte[] payload = accessToken.flatMap(jsonConverter::toJson)
                             .map(String::getBytes)
                             .orElse(null);

        return SpiResponse.<SpiAuthorisationStatus>builder()
                   .aspspConsentData(aspspConsentData.respondWith(payload))
                   .payload(spiAuthorisationStatus)
                   .success();
    }

    @Override
    @NotNull
    public SpiResponse<List<SpiScaMethod>> requestAvailableScaMethods(@NotNull SpiPsuData psuData, SpiPayment businessObject, AspspConsentData aspspConsentData) {
        ResponseEntity<List<SpiScaMethod>> aspspResponse = aspspRestTemplate.exchange(aspspRemoteUrls.getScaMethods(), HttpMethod.GET, null, new ParameterizedTypeReference<List<SpiScaMethod>>() {
        }, psuData.getPsuId());

        List<SpiScaMethod> spiScaMethods = Optional.ofNullable(aspspResponse.getBody())
                                               .orElseGet(Collections::emptyList);

        return SpiResponse.<List<SpiScaMethod>>builder()
                   .aspspConsentData(aspspConsentData)
                   .payload(spiScaMethods)
                   .success();
    }

    @Override
    @NotNull
    public SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(@NotNull SpiPsuData psuData, @NotNull SpiScaMethod scaMethod, @NotNull SpiPayment businessObject, @NotNull AspspConsentData aspspConsentData) {
        try {
            aspspRestTemplate.exchange(aspspRemoteUrls.getGenerateTanConfirmation(), HttpMethod.POST, null, Void.class, psuData.getPsuId(), scaMethod);

            return SpiResponse.<SpiAuthorizationCodeResult>builder()
                       .aspspConsentData(aspspConsentData)
                       .success();

        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {

                return SpiResponse.<SpiAuthorizationCodeResult>builder()
                           .aspspConsentData(aspspConsentData)
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }

            return SpiResponse.<SpiAuthorizationCodeResult>builder()
                       .aspspConsentData(aspspConsentData)
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    @Override
    @NotNull
    public SpiResponse<SpiResponse.VoidResponse> verifyAuthorisationCodeAndExecuteRequest(@NotNull SpiPsuData psuData, @NotNull SpiScaConfirmation spiScaConfirmation, @NotNull SpiPayment businessObject, @NotNull AspspConsentData aspspConsentData) {
        try {
            aspspRestTemplate.exchange(aspspRemoteUrls.applyStrongUserAuthorisation(), HttpMethod.PUT, new HttpEntity<>(spiScaConfirmation), ResponseEntity.class);

            return SpiResponse.<SpiResponse.VoidResponse>builder()
                       .aspspConsentData(aspspConsentData)
                       .success();

        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {

                return SpiResponse.<SpiResponse.VoidResponse>builder()
                           .aspspConsentData(aspspConsentData)
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }

            return SpiResponse.<SpiResponse.VoidResponse>builder()
                       .aspspConsentData(aspspConsentData)
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }
}
