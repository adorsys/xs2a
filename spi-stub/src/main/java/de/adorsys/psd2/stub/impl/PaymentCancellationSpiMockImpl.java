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

import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.*;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentCancellationResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentCancellationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PaymentCancellationSpiMockImpl implements PaymentCancellationSpi {
    //TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/merge_requests/1190
    private static final String DECOUPLED_PSU_MESSAGE = "Please use your BankApp for transaction Authorisation";

    @Override
    @NotNull
    public SpiResponse<SpiPaymentCancellationResponse> initiatePaymentCancellation(@NotNull SpiContextData contextData, @NotNull SpiPayment payment, @NotNull AspspConsentData aspspConsentData) {
        log.info("PaymentCancellationSpi#initiatePaymentCancellation: contextData {}, payment {}, aspspConsentData {}", contextData, payment, aspspConsentData);
        SpiPaymentCancellationResponse response = new SpiPaymentCancellationResponse();
        response.setTransactionStatus(TransactionStatus.ACCP);

        return SpiResponse.<SpiPaymentCancellationResponse>builder()
                   .aspspConsentData(aspspConsentData)
                   .payload(response)
                   .success();
    }

    @Override
    @NotNull
    public SpiResponse<SpiResponse.VoidResponse> cancelPaymentWithoutSca(@NotNull SpiContextData contextData, @NotNull SpiPayment payment, @NotNull AspspConsentData aspspConsentData) {
        log.info("PaymentCancellationSpi#cancelPaymentWithoutSca: contextData {}, payment {}, aspspConsentData {}", contextData, payment, aspspConsentData);

        return SpiResponse.<SpiResponse.VoidResponse>builder()
                   .aspspConsentData(aspspConsentData)
                   .payload(SpiResponse.voidResponse())
                   .success();
    }

    @Override
    @NotNull
    public SpiResponse<SpiResponse.VoidResponse> verifyScaAuthorisationAndCancelPayment(@NotNull SpiContextData contextData, @NotNull SpiScaConfirmation spiScaConfirmation, @NotNull SpiPayment payment, @NotNull AspspConsentData aspspConsentData) {
        log.info("PaymentCancellationSpi#cancelPaymentWithoutSca: contextData {}, spiScaConfirmation {}, payment {}, aspspConsentData {}", contextData, spiScaConfirmation, payment, aspspConsentData);

        return SpiResponse.<SpiResponse.VoidResponse>builder()
                   .aspspConsentData(aspspConsentData)
                   .payload(SpiResponse.voidResponse())
                   .success();
    }

    @Override
    public SpiResponse<SpiAuthorisationStatus> authorisePsu(@NotNull SpiContextData contextData, @NotNull SpiPsuData psuLoginData, String password, SpiPayment businessObject, @NotNull AspspConsentData aspspConsentData) {
        log.info("PaymentCancellationSpi#authorisePsu: contextData {}, psuLoginData {}, password {}, businessObject {}, aspspConsentData ()", contextData, psuLoginData, password, businessObject, aspspConsentData);

        return SpiResponse.<SpiAuthorisationStatus>builder()
                   .aspspConsentData(aspspConsentData)
                   .payload(SpiAuthorisationStatus.SUCCESS)
                   .success();
    }

    @Override
    public SpiResponse<List<SpiAuthenticationObject>> requestAvailableScaMethods(@NotNull SpiContextData contextData, SpiPayment businessObject, @NotNull AspspConsentData aspspConsentData) {
        log.info("PaymentCancellationSpi#requestAvailableScaMethods: contextData {}, businessObject {}, aspspConsentData ()", contextData, businessObject, aspspConsentData);
        List<SpiAuthenticationObject> spiScaMethods = new ArrayList<>();
        SpiAuthenticationObject sms = new SpiAuthenticationObject();
        sms.setAuthenticationType("SMS_OTP");
        sms.setAuthenticationMethodId("sms");
        spiScaMethods.add(sms);
        SpiAuthenticationObject push = new SpiAuthenticationObject();
        push.setAuthenticationType("PUSH_OTP");
        push.setAuthenticationMethodId("push");
        push.setDecoupled(true);
        spiScaMethods.add(push);

        return SpiResponse.<List<SpiAuthenticationObject>>builder()
                   .aspspConsentData(aspspConsentData)
                   .payload(spiScaMethods)
                   .success();
    }

    @Override
    @NotNull
    public SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(@NotNull SpiContextData contextData, @NotNull String authenticationMethodId, @NotNull SpiPayment businessObject, @NotNull AspspConsentData aspspConsentData) {
        log.info("PaymentCancellationSpi#requestAuthorisationCode: contextData {}, authenticationMethodId {}, businessObject {}, aspspConsentData {}", contextData, authenticationMethodId, businessObject, aspspConsentData);
        SpiAuthorizationCodeResult spiAuthorizationCodeResult = new SpiAuthorizationCodeResult();
        SpiAuthenticationObject method = new SpiAuthenticationObject();
        method.setAuthenticationMethodId("sms");
        method.setAuthenticationType("SMS_OTP");
        spiAuthorizationCodeResult.setSelectedScaMethod(method);
        spiAuthorizationCodeResult.setChallengeData(new ChallengeData(null, "some data", "some link", 100, null, "info"));

        return SpiResponse.<SpiAuthorizationCodeResult>builder()
                   .aspspConsentData(aspspConsentData)
                   .payload(spiAuthorizationCodeResult)
                   .success();
    }

    @Override
    @NotNull
    public SpiResponse<SpiAuthorisationDecoupledScaResponse> startScaDecoupled(@NotNull SpiContextData contextData, @NotNull String authorisationId, @Nullable String authenticationMethodId, @NotNull SpiPayment businessObject, @NotNull AspspConsentData aspspConsentData) {
        log.info("PaymentCancellationSpi#startScaDecoupled: contextData {}, authorisationId {}, authenticationMethodId {}, businessObject {}, aspspConsentData {}", contextData, authorisationId, authenticationMethodId, businessObject, aspspConsentData);

        return SpiResponse.<SpiAuthorisationDecoupledScaResponse>builder()
                   .payload(new SpiAuthorisationDecoupledScaResponse(DECOUPLED_PSU_MESSAGE))
                   .aspspConsentData(aspspConsentData)
                   .success();
    }
}
