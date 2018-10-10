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

package de.adorsys.aspsp.xs2a.spi.service.v2;

import de.adorsys.aspsp.xs2a.spi.domain.SpiResponse;
import de.adorsys.aspsp.xs2a.spi.domain.SpiResponse.VoidResponse;
import de.adorsys.aspsp.xs2a.spi.domain.SpiResponseStatus;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiScaMethod;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentType;
import de.adorsys.aspsp.xs2a.spi.domain.v2.SpiSinglePayment;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface to be used for single payment SPI implementation
 */
public interface SinglePaymentSpi extends PaymentSpi<SpiSinglePayment, SpiSinglePayment> {
    @NotNull
    @Override
    default SpiResponse<SpiSinglePayment> initiatePayment(SpiSinglePayment payment, @NotNull AspspConsentData aspspConsentData) {
        return SpiResponse.<SpiSinglePayment>builder().fail(SpiResponseStatus.NOT_SUPPORTED);
    }

    @Override
    default SpiResponse<VoidResponse> executePaymentWithoutSca(SpiPaymentType spiPaymentType, SpiSinglePayment payment, AspspConsentData aspspConsentData) {
        return SpiResponse.<VoidResponse>builder().fail(SpiResponseStatus.NOT_SUPPORTED);
    }

    @Override
    default SpiResponse<SpiSinglePayment> getPaymentById(SpiSinglePayment payment, AspspConsentData aspspConsentData) {
        return SpiResponse.<SpiSinglePayment>builder().fail(SpiResponseStatus.NOT_SUPPORTED);
    }

    @Override
    default SpiResponse<SpiTransactionStatus> getPaymentStatusById(SpiSinglePayment payment, AspspConsentData aspspConsentData) {
        return SpiResponse.<SpiTransactionStatus>builder().fail(SpiResponseStatus.NOT_SUPPORTED);
    }

    @Override
    default SpiResponse<SpiAuthorisationStatus> authorisePsu(String psuId, String password, SpiSinglePayment businessObject, AspspConsentData aspspConsentData) {
        return SpiResponse.<SpiAuthorisationStatus>builder().fail(SpiResponseStatus.NOT_SUPPORTED);
    }

    @Override
    default SpiResponse<List<SpiScaMethod>> requestAvailableScaMethods(String psuId, SpiSinglePayment businessObject, AspspConsentData aspspConsentData) {
        return SpiResponse.<List<SpiScaMethod>>builder().fail(SpiResponseStatus.NOT_SUPPORTED);
    }

    @Override
    default SpiResponse<VoidResponse> requestAuthorisationCode(String psuId, SpiScaMethod scaMethod, SpiSinglePayment businessObject, AspspConsentData aspspConsentData) {
        return SpiResponse.<VoidResponse>builder().fail(SpiResponseStatus.NOT_SUPPORTED);
    }

    @Override
    default SpiResponse<VoidResponse> verifyAuthorisationCodeAndExecuteRequest(SpiScaConfirmation spiScaConfirmation, SpiSinglePayment businessObject, AspspConsentData aspspConsentData) {
        return SpiResponse.<VoidResponse>builder().fail(SpiResponseStatus.NOT_SUPPORTED);
    }
}
