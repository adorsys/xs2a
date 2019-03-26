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
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.*;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiInitiateAisConsentResponse;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiVerifyScaAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

@Slf4j
@Service
public class AisConsentSpiMockImpl implements AisConsentSpi {
    private static final String TEST_ASPSP_DATA = "Test aspsp data";
    private static final String DECOUPLED_PSU_MESSAGE = "Please use your BankApp for transaction Authorisation";

    @Override
    public SpiResponse<SpiInitiateAisConsentResponse> initiateAisConsent(@NotNull SpiContextData contextData, SpiAccountConsent accountConsent, AspspConsentData initialAspspConsentData) {
        log.info("AccountSpi#requestAccountList: contextData {}, accountConsent {}, aspspConsentData {}", contextData, accountConsent, initialAspspConsentData);
        SpiAccountAccess access = new SpiAccountAccess();
        SpiAccountReference accountReference = new SpiAccountReference("11111-11118", "10023-999999999", "DE52500105173911841934",
                                                                       null, null, null, null, Currency.getInstance("EUR"));
        access.setAccounts(Collections.singletonList(accountReference));
        access.setBalances(Collections.singletonList(accountReference));
        access.setTransactions(Collections.singletonList(accountReference));

        return SpiResponse.<SpiInitiateAisConsentResponse>builder()
                   .payload(new SpiInitiateAisConsentResponse(access, false))
                   .aspspConsentData(initialAspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                   .success();
    }

    @Override
    public SpiResponse<SpiResponse.VoidResponse> revokeAisConsent(@NotNull SpiContextData contextData, SpiAccountConsent accountConsent, AspspConsentData aspspConsentData) {
        log.info("AccountSpi#revokeAisConsent: contextData {}, accountConsent {}, aspspConsentData {}", contextData, accountConsent, aspspConsentData);

        return SpiResponse.<SpiResponse.VoidResponse>builder()
                   .aspspConsentData(aspspConsentData)
                   .payload(SpiResponse.voidResponse())
                   .success();
    }

    @Override
    @NotNull
    public SpiResponse<SpiVerifyScaAuthorisationResponse> verifyScaAuthorisation(@NotNull SpiContextData contextData, @NotNull SpiScaConfirmation spiScaConfirmation, @NotNull SpiAccountConsent accountConsent, @NotNull AspspConsentData aspspConsentData) {
        log.info("AccountSpi#verifyScaAuthorisation: contextData {}, spiScaConfirmation {}, accountConsent {}, aspspConsentData {}", contextData, spiScaConfirmation, accountConsent, aspspConsentData);

        return SpiResponse.<SpiVerifyScaAuthorisationResponse>builder()
                   .payload(new SpiVerifyScaAuthorisationResponse(ConsentStatus.VALID))
                   .aspspConsentData(aspspConsentData)
                   .success();
    }

    @Override
    public SpiResponse<SpiAuthorisationStatus> authorisePsu(@NotNull SpiContextData contextData, @NotNull SpiPsuData psuLoginData, String password, SpiAccountConsent businessObject, @NotNull AspspConsentData aspspConsentData) {
        log.info("AisConsentSpi#authorisePsu: contextData {}, psuLoginData {}, password {}, businessObject {}, aspspConsentData ()", contextData, psuLoginData, password, businessObject, aspspConsentData);

        return SpiResponse.<SpiAuthorisationStatus>builder()
                   .aspspConsentData(aspspConsentData)
                   .payload(SpiAuthorisationStatus.SUCCESS)
                   .success();
    }

    @Override
    public SpiResponse<List<SpiAuthenticationObject>> requestAvailableScaMethods(@NotNull SpiContextData contextData, SpiAccountConsent businessObject, @NotNull AspspConsentData aspspConsentData) {
        log.info("AisConsentSpi#requestAvailableScaMethods: contextData {}, businessObject {}, aspspConsentData ()", contextData, businessObject, aspspConsentData);
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
    public SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(@NotNull SpiContextData contextData, @NotNull String authenticationMethodId, @NotNull SpiAccountConsent businessObject, @NotNull AspspConsentData aspspConsentData) {
        log.info("AisConsentSpi#requestAuthorisationCode: contextData {}, authenticationMethodId {}, businessObject {}, aspspConsentData {}", contextData, authenticationMethodId, businessObject, aspspConsentData);
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
    public SpiResponse<SpiAuthorisationDecoupledScaResponse> startScaDecoupled(@NotNull SpiContextData contextData, @NotNull String authorisationId, @Nullable String authenticationMethodId, @NotNull SpiAccountConsent businessObject, @NotNull AspspConsentData aspspConsentData) {
        log.info("AisConsentSpi#startScaDecoupled: contextData {}, authorisationId {}, authenticationMethodId {}, businessObject {}, aspspConsentData {}", contextData, authorisationId, authenticationMethodId, businessObject, aspspConsentData);

        return SpiResponse.<SpiAuthorisationDecoupledScaResponse>builder()
                   .payload(new SpiAuthorisationDecoupledScaResponse(DECOUPLED_PSU_MESSAGE))
                   .aspspConsentData(aspspConsentData)
                   .success();
    }
}
