/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.spi.service;

import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.error.SpiMessageErrorCode;
import de.adorsys.psd2.xs2a.spi.domain.error.SpiTppMessage;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiGetPaymentStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import org.jetbrains.annotations.NotNull;

/**
 * Interface to be used for payment SPI implementation
 */
public interface CommonPaymentSpi extends PaymentSpi<SpiPaymentInfo, SpiPaymentInitiationResponse> {
    @Override
    @NotNull
    default SpiResponse<SpiPaymentInitiationResponse> initiatePayment(@NotNull SpiContextData contextData, @NotNull SpiPaymentInfo payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return SpiResponse.<SpiPaymentInitiationResponse>builder()
                   .error(new SpiTppMessage(SpiMessageErrorCode.SERVICE_NOT_SUPPORTED))
                   .build();
    }

    @Override
    @NotNull
    default SpiResponse<SpiPaymentInfo> getPaymentById(@NotNull SpiContextData contextData, @NotNull String acceptMediaType, @NotNull SpiPaymentInfo payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return SpiResponse.<SpiPaymentInfo>builder()
                   .error(new SpiTppMessage(SpiMessageErrorCode.SERVICE_NOT_SUPPORTED))
                   .build();
    }

    @Override
    @NotNull
    default SpiResponse<SpiGetPaymentStatusResponse> getPaymentStatusById(@NotNull SpiContextData contextData, @NotNull String acceptMediaType, @NotNull SpiPaymentInfo payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return SpiResponse.<SpiGetPaymentStatusResponse>builder()
                   .error(new SpiTppMessage(SpiMessageErrorCode.SERVICE_NOT_SUPPORTED))
                   .build();
    }
}
