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

import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiCommonPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentExecutionResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CommonPaymentSpi;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class CommonPaymentSpiMockImpl implements CommonPaymentSpi {
    private static final String TEST_ASPSP_DATA = "Test aspsp data";

    @Override
    @NotNull
    public SpiResponse<SpiPaymentInitiationResponse> initiatePayment(@NotNull SpiContextData contextData, @NotNull SpiPaymentInfo payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("CommonPaymentSpi#initiatePayment: contextData {}, spiPaymentInfo {}, aspspConsentData {}", contextData, payment, aspspConsentDataProvider.loadAspspConsentData());
        SpiCommonPaymentInitiationResponse response = new SpiCommonPaymentInitiationResponse();
        response.setTransactionStatus(TransactionStatus.RCVD);
        response.setPaymentId(UUID.randomUUID().toString());
        response.setAspspAccountId("d0419f4f-54a5-47fd-ae59-af308601bb16");

        aspspConsentDataProvider.updateAspspConsentData(TEST_ASPSP_DATA.getBytes());

        return SpiResponse.<SpiPaymentInitiationResponse>builder()
                   .payload(response)
                   .success();
    }

    @Override
    @NotNull
    public SpiResponse<SpiPaymentInfo> getPaymentById(@NotNull SpiContextData contextData, @NotNull SpiPaymentInfo payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("CommonPaymentSpi#getPaymentById: contextData {}, spiPaymentInfo {}, aspspConsentData {}", contextData, payment, aspspConsentDataProvider.loadAspspConsentData());

        return SpiResponse.<SpiPaymentInfo>builder()
                   .payload(payment)
                   .success();
    }

    @Override
    @NotNull
    public SpiResponse<TransactionStatus> getPaymentStatusById(@NotNull SpiContextData contextData, @NotNull SpiPaymentInfo payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("CommonPaymentSpi#getPaymentStatusById: contextData {}, spiPaymentInfo {}, aspspConsentData {}", contextData, payment, aspspConsentDataProvider.loadAspspConsentData());

        return SpiResponse.<TransactionStatus>builder()
                   .payload(payment.getPaymentStatus())
                   .success();
    }

    @Override
    @NotNull
    public SpiResponse<SpiPaymentExecutionResponse> executePaymentWithoutSca(@NotNull SpiContextData contextData, @NotNull SpiPaymentInfo payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("CommonPaymentSpi#executePaymentWithoutSca: contextData {}, spiPaymentInfo {}, aspspConsentData {}", contextData, payment, aspspConsentDataProvider.loadAspspConsentData());

        return SpiResponse.<SpiPaymentExecutionResponse>builder()
                   .payload(new SpiPaymentExecutionResponse(TransactionStatus.ACCP))
                   .success();
    }

    @Override
    @NotNull
    public SpiResponse<SpiPaymentExecutionResponse> verifyScaAuthorisationAndExecutePayment(@NotNull SpiContextData contextData, @NotNull SpiScaConfirmation spiScaConfirmation, @NotNull SpiPaymentInfo payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("CommonPaymentSpi#verifyScaAuthorisationAndExecutePayment: contextData {}, spiScaConfirmation{}, spiPaymentInfo {}, aspspConsentData {}", contextData, spiScaConfirmation, payment, aspspConsentDataProvider.loadAspspConsentData());

        return SpiResponse.<SpiPaymentExecutionResponse>builder()
                   .payload(new SpiPaymentExecutionResponse(TransactionStatus.ACCP))
                   .success();
    }
}
