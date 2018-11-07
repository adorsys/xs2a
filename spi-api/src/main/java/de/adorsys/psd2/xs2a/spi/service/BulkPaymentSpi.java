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

import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiBulkPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Interface to be used for bulk payment SPI implementation
 */
public interface BulkPaymentSpi extends PaymentSpi<SpiBulkPayment, SpiBulkPaymentInitiationResponse> {
    @NotNull
    @Override
    default SpiResponse<SpiBulkPaymentInitiationResponse> initiatePayment(@NotNull SpiPsuData psuData, @NotNull SpiBulkPayment payment, @NotNull AspspConsentData initialAspspConsentData) {
        return SpiResponse.<SpiBulkPaymentInitiationResponse>builder().fail(SpiResponseStatus.NOT_SUPPORTED);
    }

    @Override
    @NotNull
    default SpiResponse<SpiBulkPayment> getPaymentById(@NotNull SpiPsuData psuData, @NotNull SpiBulkPayment payment, @NotNull AspspConsentData aspspConsentData) {
        return SpiResponse.<SpiBulkPayment>builder().fail(SpiResponseStatus.NOT_SUPPORTED);
    }

    @Override
    @NotNull
    default SpiResponse<SpiTransactionStatus> getPaymentStatusById(@NotNull SpiPsuData psuData, @NotNull SpiBulkPayment payment, @NotNull AspspConsentData aspspConsentData) {
        return SpiResponse.<SpiTransactionStatus>builder().fail(SpiResponseStatus.NOT_SUPPORTED);
    }
}
