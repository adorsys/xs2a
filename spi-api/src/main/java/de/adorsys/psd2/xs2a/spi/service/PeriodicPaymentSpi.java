/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.spi.service;

import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiGetPaymentStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPeriodicPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import org.jetbrains.annotations.NotNull;

/**
 * Interface to be used for periodic payment SPI implementation
 */
public interface PeriodicPaymentSpi extends PaymentSpi<SpiPeriodicPayment, SpiPeriodicPaymentInitiationResponse> {
    @Override
    @NotNull
    default SpiResponse<SpiPeriodicPaymentInitiationResponse> initiatePayment(@NotNull SpiContextData contextData, @NotNull SpiPeriodicPayment payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return SpiResponse.<SpiPeriodicPaymentInitiationResponse>builder()
                   .error(new TppMessage(MessageErrorCode.SERVICE_NOT_SUPPORTED))
                   .build();
    }

    @Override
    @NotNull
    default SpiResponse<SpiPeriodicPayment> getPaymentById(@NotNull SpiContextData contextData, @NotNull String acceptMediaType, @NotNull SpiPeriodicPayment payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return SpiResponse.<SpiPeriodicPayment>builder()
                   .error(new TppMessage(MessageErrorCode.SERVICE_NOT_SUPPORTED))
                   .build();
    }

    @Override
    @NotNull
    default SpiResponse<SpiGetPaymentStatusResponse> getPaymentStatusById(@NotNull SpiContextData contextData, @NotNull String acceptMediaType, @NotNull SpiPeriodicPayment payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return SpiResponse.<SpiGetPaymentStatusResponse>builder()
                   .error(new TppMessage(MessageErrorCode.SERVICE_NOT_SUPPORTED))
                   .build();
    }
}
