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

import de.adorsys.aspsp.xs2a.spi.config.rest.AspspRemoteUrls;
import de.adorsys.aspsp.xs2a.spi.domain.SpiAspspAuthorisationData;
import de.adorsys.aspsp.xs2a.spi.impl.service.KeycloakInvokerService;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.exception.RestException;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthenticationObject;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.PaymentAuthorisationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
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
public class PaymentAuthorisationSpiImpl implements PaymentAuthorisationSpi {

    @Qualifier("aspspRestTemplate")
    private final RestTemplate aspspRestTemplate;
    private final AspspRemoteUrls aspspRemoteUrls;
    private final KeycloakInvokerService keycloakInvokerService;

    @Override
    @NotNull
    public SpiResponse<SpiAuthorisationStatus> authorisePsu(@NotNull SpiContextData spiContextData, @NotNull SpiPsuData psuLoginData, String password, SpiPayment payment, @NotNull AspspConsentData aspspConsentData) {
        Optional<SpiAspspAuthorisationData> accessToken = keycloakInvokerService.obtainAuthorisationData(psuLoginData.getPsuId(), password);
        SpiAuthorisationStatus spiAuthorisationStatus = accessToken.map(t -> SUCCESS)
                                                            .orElse(FAILURE);
        if (spiAuthorisationStatus == FAILURE) {
            return SpiResponse.<SpiAuthorisationStatus>builder()
                       .aspspConsentData(aspspConsentData)
                       .fail(SpiResponseStatus.UNAUTHORIZED_FAILURE);
        }

        return SpiResponse.<SpiAuthorisationStatus>builder()
                   .aspspConsentData(aspspConsentData)
                   .payload(spiAuthorisationStatus)
                   .success();
    }

    @Override
    @NotNull
    public SpiResponse<List<SpiAuthenticationObject>> requestAvailableScaMethods(@NotNull SpiContextData spiContextData, SpiPayment payment, @NotNull AspspConsentData aspspConsentData) {
        try {
            ResponseEntity<List<SpiAuthenticationObject>> aspspResponse = aspspRestTemplate.exchange(aspspRemoteUrls.getScaMethods(), HttpMethod.GET, null, new ParameterizedTypeReference<List<SpiAuthenticationObject>>() {
            }, spiContextData.getPsuData().getPsuId());

            List<SpiAuthenticationObject> spiScaMethods = Optional.ofNullable(aspspResponse.getBody())
                                                              .orElseGet(Collections::emptyList);

            return SpiResponse.<List<SpiAuthenticationObject>>builder()
                       .aspspConsentData(aspspConsentData)
                       .payload(spiScaMethods)
                       .success();
        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return SpiResponse.<List<SpiAuthenticationObject>>builder()
                           .aspspConsentData(aspspConsentData)
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }

            return SpiResponse.<List<SpiAuthenticationObject>>builder()
                       .aspspConsentData(aspspConsentData)
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    @Override
    @NotNull
    public SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(@NotNull SpiContextData spiContextData, @NotNull String authenticationMethodId, @NotNull SpiPayment payment, @NotNull AspspConsentData aspspConsentData) {
        try {
            aspspRestTemplate.exchange(aspspRemoteUrls.getGenerateTanConfirmation(), HttpMethod.POST, null, Void.class, spiContextData.getPsuData().getPsuId(), authenticationMethodId);

            return SpiResponse.<SpiAuthorizationCodeResult>builder()
                       .aspspConsentData(aspspConsentData)
                       // TODO We need to return real payload data from ASPSP https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/489
                       .payload(getDefaultSpiAuthorizationCodeResult())
                       .success();

        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return SpiResponse.<SpiAuthorizationCodeResult>builder()
                           .aspspConsentData(aspspConsentData)
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }

            if (e.getHttpStatus() == HttpStatus.BAD_REQUEST) {
                SpiAuthorizationCodeResult spiAuthorizationCodeResult = new SpiAuthorizationCodeResult();
                spiAuthorizationCodeResult.setChallengeData(new ChallengeData());
                return SpiResponse.<SpiAuthorizationCodeResult>builder()
                           .aspspConsentData(aspspConsentData)
                           .payload(spiAuthorizationCodeResult)
                           .success();
            }

            return SpiResponse.<SpiAuthorizationCodeResult>builder()
                       .aspspConsentData(aspspConsentData)
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    private SpiAuthorizationCodeResult getDefaultSpiAuthorizationCodeResult() {
        SpiAuthenticationObject method = new SpiAuthenticationObject();
        method.setAuthenticationMethodId("sms");
        method.setAuthenticationType("SMS_OTP");

        ChallengeData challengeData = new ChallengeData(null, "some data", "some link", 100, null, "info");

        SpiAuthorizationCodeResult resultTmp = new SpiAuthorizationCodeResult();
        resultTmp.setChallengeData(challengeData);
        resultTmp.setSelectedScaMethod(method);

        return resultTmp;
    }
}
