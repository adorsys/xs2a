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
import de.adorsys.psd2.stub.impl.service.SpiMockData;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.*;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentCancellationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentExecutionResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentCancellationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class PaymentCancellationSpiMockImpl implements PaymentCancellationSpi {
    private static final String DECOUPLED_PSU_MESSAGE = "Please use your BankApp for transaction Authorisation";
    private static final String PSU_MESSAGE = "Mocked PSU message from SPI for this payment";

    private final AuthorisationServiceMock authorisationService;

    @Override
    public SpiResponse<SpiStartAuthorisationResponse> startAuthorisation(@NotNull SpiContextData contextData, @NotNull ScaApproach scaApproach, @NotNull ScaStatus scaStatus, @NotNull String authorisationId, SpiPayment businessObject, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return SpiResponse.<SpiStartAuthorisationResponse>builder()
                   .payload(new SpiStartAuthorisationResponse(scaApproach, scaStatus, SpiMockData.PSU_MESSAGE_START_AUTHORISATION, SpiMockData.TPP_MESSAGES_START_AUTHORISATION))
                   .build();
    }

    @Override
    @NotNull
    public SpiResponse<SpiPaymentCancellationResponse> initiatePaymentCancellation(@NotNull SpiContextData contextData, @NotNull SpiPayment payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("PaymentCancellationSpi#initiatePaymentCancellation: contextData {}, payment {}, aspspConsentData {}", contextData, payment, aspspConsentDataProvider.loadAspspConsentData());
        SpiPaymentCancellationResponse response = new SpiPaymentCancellationResponse();
        response.setTransactionStatus(TransactionStatus.ACCP);

        return SpiResponse.<SpiPaymentCancellationResponse>builder()
                   .payload(response)
                   .build();
    }

    @Override
    @NotNull
    public SpiResponse<SpiResponse.VoidResponse> cancelPaymentWithoutSca(@NotNull SpiContextData contextData, @NotNull SpiPayment payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("PaymentCancellationSpi#cancelPaymentWithoutSca: contextData {}, payment {}, aspspConsentData {}", contextData, payment, aspspConsentDataProvider.loadAspspConsentData());

        return SpiResponse.<SpiResponse.VoidResponse>builder()
                   .payload(SpiResponse.voidResponse())
                   .build();
    }

    @Override
    @NotNull
    public SpiResponse<SpiPaymentExecutionResponse> verifyScaAuthorisationAndCancelPaymentWithResponse(@NotNull SpiContextData contextData, @NotNull SpiScaConfirmation spiScaConfirmation, @NotNull SpiPayment payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("PaymentCancellationSpi#cancelPaymentWithoutSca: contextData {}, spiScaConfirmation {}, payment {}, aspspConsentData {}", contextData, spiScaConfirmation, payment, aspspConsentDataProvider.loadAspspConsentData());

        return SpiResponse.<SpiPaymentExecutionResponse>builder()
                   .payload(new SpiPaymentExecutionResponse(SpiAuthorisationStatus.SUCCESS))
                   .build();
    }

    @Override
    public SpiResponse<SpiPsuAuthorisationResponse> authorisePsu(@NotNull SpiContextData contextData, @NotNull String authorisationId, @NotNull SpiPsuData psuLoginData, String password, SpiPayment businessObject, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("PaymentCancellationSpi#authorisePsu: contextData {}, psuLoginData {}, password {}, businessObject {}, aspspConsentData {}", contextData, psuLoginData, password, businessObject, aspspConsentDataProvider.loadAspspConsentData());

        return SpiResponse.<SpiPsuAuthorisationResponse>builder()
                   .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                   .build();
    }

    @Override
    public SpiResponse<SpiAvailableScaMethodsResponse> requestAvailableScaMethods(@NotNull SpiContextData contextData, SpiPayment businessObject, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("PaymentCancellationSpi#requestAvailableScaMethods: contextData {}, businessObject {}, aspspConsentData {}", contextData, businessObject, aspspConsentDataProvider.loadAspspConsentData());
        return authorisationService.requestAvailableScaMethods();
    }

    @Override
    @NotNull
    public SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(@NotNull SpiContextData contextData, @NotNull String authenticationMethodId, @NotNull SpiPayment businessObject, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("PaymentCancellationSpi#requestAuthorisationCode: contextData {}, authenticationMethodId {}, businessObject {}, aspspConsentData {}", contextData, authenticationMethodId, businessObject, aspspConsentDataProvider.loadAspspConsentData());
        return authorisationService.requestAuthorisationCode();
    }

    @Override
    @NotNull
    public SpiResponse<SpiAuthorisationDecoupledScaResponse> startScaDecoupled(@NotNull SpiContextData contextData, @NotNull String authorisationId, @Nullable String authenticationMethodId, @NotNull SpiPayment businessObject, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("PaymentCancellationSpi#startScaDecoupled: contextData {}, authorisationId {}, authenticationMethodId {}, businessObject {}, aspspConsentData {}", contextData, authorisationId, authenticationMethodId, businessObject, aspspConsentDataProvider.loadAspspConsentData());

        return SpiResponse.<SpiAuthorisationDecoupledScaResponse>builder()
                   .payload(new SpiAuthorisationDecoupledScaResponse(ScaStatus.SCAMETHODSELECTED, DECOUPLED_PSU_MESSAGE))
                   .build();
    }

    @Override
    public SpiResponse<SpiScaStatusResponse> getScaStatus(@NotNull ScaStatus scaStatus, @NotNull SpiContextData contextData,
                                                          @NotNull String authorisationId,
                                                          @NotNull SpiPayment businessObject,
                                                          @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return SpiResponse.<SpiScaStatusResponse>builder()
                   .payload(new SpiScaStatusResponse(scaStatus, true, PSU_MESSAGE,
                                                     SpiMockData.SPI_LINKS,
                                                     SpiMockData.TPP_MESSAGES))
                   .build();
    }
}
