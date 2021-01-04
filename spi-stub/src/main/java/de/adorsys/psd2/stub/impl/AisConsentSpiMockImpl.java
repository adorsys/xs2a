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

import de.adorsys.psd2.stub.impl.service.AuthorisationServiceMock;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.*;
import de.adorsys.psd2.xs2a.spi.domain.consent.*;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Currency;

@Slf4j
@Service
@AllArgsConstructor
public class AisConsentSpiMockImpl implements AisConsentSpi {
    private static final String DECOUPLED_PSU_MESSAGE = "Please use your BankApp for transaction Authorisation";
    private final AuthorisationServiceMock authorisationService;

    @Override
    public SpiResponse<SpiInitiateAisConsentResponse> initiateAisConsent(@NotNull SpiContextData contextData, SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("AccountSpi#initiateAisConsent: contextData {}, accountConsent-id {}", contextData, accountConsent.getId());
        SpiAccountAccess access = new SpiAccountAccess();
        SpiAccountReference accountReference = new SpiAccountReference("11111-11118", "10023-999999999", "DE52500105173911841934",
                                                                       null, null, null, null, Currency.getInstance("EUR"), null);
        access.setAccounts(Collections.singletonList(accountReference));
        access.setBalances(Collections.singletonList(accountReference));
        access.setTransactions(Collections.singletonList(accountReference));

        return SpiResponse.<SpiInitiateAisConsentResponse>builder()
                   .payload(new SpiInitiateAisConsentResponse(access, false, ""))
                   .build();
    }

    @Override
    public SpiResponse<SpiConsentStatusResponse> getConsentStatus(@NotNull SpiContextData contextData, @NotNull SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("AccountSpi#getConsentStatus: contextData {}, accountConsent-id {}", contextData, accountConsent.getId());

        return SpiResponse.<SpiConsentStatusResponse>builder()
                   .payload(new SpiConsentStatusResponse(accountConsent.getConsentStatus(), "Mocked PSU message from SPI for this consent"))
                   .build();
    }

    @Override
    public SpiResponse<SpiResponse.VoidResponse> revokeAisConsent(@NotNull SpiContextData contextData, SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("AccountSpi#revokeAisConsent: contextData {}, accountConsent-id {}", contextData, accountConsent.getId());

        return SpiResponse.<SpiResponse.VoidResponse>builder()
                   .payload(SpiResponse.voidResponse())
                   .build();
    }

    @Override
    @NotNull
    public SpiResponse<SpiVerifyScaAuthorisationResponse> verifyScaAuthorisation(@NotNull SpiContextData contextData, @NotNull SpiScaConfirmation spiScaConfirmation, @NotNull SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("AccountSpi#verifyScaAuthorisation: contextData {}, spiScaConfirmation {}, accountConsent-id {}", contextData, spiScaConfirmation, accountConsent.getId());

        return SpiResponse.<SpiVerifyScaAuthorisationResponse>builder()
                   .payload(new SpiVerifyScaAuthorisationResponse(ConsentStatus.VALID))
                   .build();
    }

    @Override
    public @NotNull SpiResponse<SpiConsentConfirmationCodeValidationResponse> checkConfirmationCode(@NotNull SpiContextData contextData, @NotNull SpiCheckConfirmationCodeRequest spiCheckConfirmationCodeRequest, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("AisConsentSpi#checkConfirmationCode: contextData {}, spiCheckConfirmationCodeRequest{}, authorisation-id {}", contextData, spiCheckConfirmationCodeRequest.getConfirmationCode(), spiCheckConfirmationCodeRequest.getAuthorisationId());

        return SpiResponse.<SpiConsentConfirmationCodeValidationResponse>builder()
                   .payload(new SpiConsentConfirmationCodeValidationResponse(ScaStatus.FINALISED, ConsentStatus.VALID))
                   .build();
    }

    @Override
    @NotNull
    public SpiResponse<SpiConsentConfirmationCodeValidationResponse> notifyConfirmationCodeValidation(@NotNull SpiContextData contextData, boolean confirmationCodeValidationResult, @NotNull SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return authorisationService.notifyConfirmationCodeValidation(confirmationCodeValidationResult);
    }

    @Override
    public SpiResponse<SpiPsuAuthorisationResponse> authorisePsu(@NotNull SpiContextData contextData, @NotNull String authorisationId, @NotNull SpiPsuData psuLoginData, String password, SpiAccountConsent businessObject, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("AisConsentSpi#authorisePsu: contextData {}, psuLoginData {}, businessObject-id {}", contextData, psuLoginData, businessObject.getId());

        return SpiResponse.<SpiPsuAuthorisationResponse>builder()
                   .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                   .build();
    }

    @Override
    public SpiResponse<SpiAvailableScaMethodsResponse> requestAvailableScaMethods(@NotNull SpiContextData contextData, SpiAccountConsent businessObject, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("AisConsentSpi#requestAvailableScaMethods: contextData {}, businessObject-id {}", contextData, businessObject.getId());
        return authorisationService.requestAvailableScaMethods();
    }

    @Override
    @NotNull
    public SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(@NotNull SpiContextData contextData, @NotNull String authenticationMethodId, @NotNull SpiAccountConsent businessObject, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("AisConsentSpi#requestAuthorisationCode: contextData {}, authenticationMethodId {}, businessObject-id {}", contextData, authenticationMethodId, businessObject.getId());
        return authorisationService.requestAuthorisationCode();
    }

    @Override
    @NotNull
    public SpiResponse<SpiAuthorisationDecoupledScaResponse> startScaDecoupled(@NotNull SpiContextData contextData, @NotNull String authorisationId, @Nullable String authenticationMethodId, @NotNull SpiAccountConsent businessObject, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("AisConsentSpi#startScaDecoupled: contextData {}, authorisationId {}, authenticationMethodId {}, businessObject-id {}", contextData, authorisationId, authenticationMethodId, businessObject.getId());
        return SpiResponse.<SpiAuthorisationDecoupledScaResponse>builder()
                   .payload(new SpiAuthorisationDecoupledScaResponse(DECOUPLED_PSU_MESSAGE))
                   .build();
    }

    @Override
    public @NotNull SpiResponse<Boolean> requestTrustedBeneficiaryFlag(@NotNull SpiContextData contextData, @NotNull SpiAccountConsent accountConsent, @NotNull String authorisationId, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return SpiResponse.<Boolean>builder()
                   .payload(true)
                   .build();
    }
}
