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

package de.adorsys.psd2.xs2a.spi.service;

import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse.VoidResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaMethod;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentType;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface to be used for bulk payment SPI implementation
 */
public interface BulkPaymentSpi extends PaymentSpi<SpiBulkPayment, List<SpiPaymentInitialisationResponse>> {
    @NotNull
    @Override
    default SpiResponse<List<SpiPaymentInitialisationResponse>> initiatePayment(@NotNull SpiBulkPayment payment, @NotNull AspspConsentData aspspConsentData) {
        return SpiResponse.<List<SpiPaymentInitialisationResponse>>builder().fail(SpiResponseStatus.NOT_SUPPORTED);
    }

    @Override
    default SpiResponse<VoidResponse> executePaymentWithoutSca(SpiPaymentType spiPaymentType, SpiBulkPayment payment, AspspConsentData aspspConsentData) {
        return SpiResponse.<VoidResponse>builder().fail(SpiResponseStatus.NOT_SUPPORTED);
    }

    @Override
    default SpiResponse<SpiBulkPayment> getPaymentById(SpiBulkPayment payment, AspspConsentData aspspConsentData) {
        return SpiResponse.<SpiBulkPayment>builder().fail(SpiResponseStatus.NOT_SUPPORTED);
    }

    @Override
    default SpiResponse<SpiTransactionStatus> getPaymentStatusById(SpiBulkPayment payment, AspspConsentData aspspConsentData) {
        return SpiResponse.<SpiTransactionStatus>builder().fail(SpiResponseStatus.NOT_SUPPORTED);
    }

    @Override
    default SpiResponse<SpiAuthorisationStatus> authorisePsu(String psuId, String password, SpiBulkPayment businessObject, AspspConsentData aspspConsentData) {
        return SpiResponse.<SpiAuthorisationStatus>builder().fail(SpiResponseStatus.NOT_SUPPORTED);
    }

    @Override
    default SpiResponse<List<SpiScaMethod>> requestAvailableScaMethods(String psuId, SpiBulkPayment businessObject, AspspConsentData aspspConsentData) {
        return SpiResponse.<List<SpiScaMethod>>builder().fail(SpiResponseStatus.NOT_SUPPORTED);
    }

    @Override
    default SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(String psuId, SpiScaMethod scaMethod, SpiBulkPayment businessObject, AspspConsentData aspspConsentData) {
       return SpiResponse.<SpiAuthorizationCodeResult>builder().fail(SpiResponseStatus.NOT_SUPPORTED);
    }

    @Override
    default SpiResponse<VoidResponse> verifyAuthorisationCodeAndExecuteRequest(SpiScaConfirmation spiScaConfirmation, SpiBulkPayment businessObject, AspspConsentData aspspConsentData) {
        return SpiResponse.<VoidResponse>builder().fail(SpiResponseStatus.NOT_SUPPORTED);
    }
}
