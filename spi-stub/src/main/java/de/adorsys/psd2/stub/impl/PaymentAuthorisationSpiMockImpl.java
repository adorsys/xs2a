/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.stub.impl;

import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.*;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentAuthorisationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class PaymentAuthorisationSpiMockImpl implements PaymentAuthorisationSpi {
    private static final String DECOUPLED_PSU_MESSAGE = "Please use your BankApp for transaction Authorisation";

    @Override
    public SpiResponse<SpiPsuAuthorisationResponse> authorisePsu(@NotNull SpiContextData contextData, @NotNull SpiPsuData psuLoginData, String password, SpiPayment businessObject, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("PaymentAuthorisationSpi#authorisePsu: contextData {}, psuLoginData {}, password {}, businessObject {}", contextData, psuLoginData, password, businessObject);

        return SpiResponse.<SpiPsuAuthorisationResponse>builder()
                   .payload(new SpiPsuAuthorisationResponse( false, SpiAuthorisationStatus.SUCCESS))
                   .build();
    }

    @Override
    public SpiResponse<SpiAvailableScaMethodsResponse> requestAvailableScaMethods(@NotNull SpiContextData contextData, SpiPayment businessObject, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("PaymentAuthorisationSpi#requestAvailableScaMethods: contextData {}, businessObject {}", contextData, businessObject);
        List<AuthenticationObject> spiScaMethods = new ArrayList<>();
        AuthenticationObject sms = new AuthenticationObject();
        sms.setAuthenticationType("SMS_OTP");
        sms.setAuthenticationMethodId("sms");
        spiScaMethods.add(sms);
        AuthenticationObject push = new AuthenticationObject();
        push.setAuthenticationType("PUSH_OTP");
        push.setAuthenticationMethodId("push");
        push.setDecoupled(true);
        spiScaMethods.add(push);

        return SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                   .payload(new SpiAvailableScaMethodsResponse(false, spiScaMethods))
                   .build();
    }

    @Override
    @NotNull
    public SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(@NotNull SpiContextData contextData, @NotNull String authenticationMethodId, @NotNull SpiPayment businessObject, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("PaymentAuthorisationSpi#requestAuthorisationCode: contextData {}, authenticationMethodId {}, businessObject {}", contextData, authenticationMethodId, businessObject);
        SpiAuthorizationCodeResult spiAuthorizationCodeResult = new SpiAuthorizationCodeResult();
        AuthenticationObject method = new AuthenticationObject();
        method.setAuthenticationMethodId("sms");
        method.setAuthenticationType("SMS_OTP");
        spiAuthorizationCodeResult.setSelectedScaMethod(method);
        spiAuthorizationCodeResult.setChallengeData(new ChallengeData(null, Collections.singletonList("some data"), "some link", 100, null, "info"));

        return SpiResponse.<SpiAuthorizationCodeResult>builder()
                   .payload(spiAuthorizationCodeResult)
                   .build();
    }

    @Override
    @NotNull
    public SpiResponse<SpiAuthorisationDecoupledScaResponse> startScaDecoupled(@NotNull SpiContextData contextData, @NotNull String authorisationId, @Nullable String authenticationMethodId, @NotNull SpiPayment businessObject, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("PaymentAuthorisationSpi#startScaDecoupled: contextData {}, authorisationId {}, authenticationMethodId {}, businessObject {}", contextData, authorisationId, authenticationMethodId, businessObject);

        return SpiResponse.<SpiAuthorisationDecoupledScaResponse>builder()
                   .payload(new SpiAuthorisationDecoupledScaResponse(DECOUPLED_PSU_MESSAGE))
                   .build();
    }

    @Override
    public @NotNull SpiResponse<Boolean> requestTrustedBeneficiaryFlag(@NotNull SpiContextData contextData, @NotNull SpiPayment payment, @NotNull String authorisationId, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return SpiResponse.<Boolean>builder()
                   .payload(true)
                   .build();
    }
}
