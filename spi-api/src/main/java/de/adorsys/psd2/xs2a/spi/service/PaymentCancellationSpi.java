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

import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentCancellationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentExecutionResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import org.jetbrains.annotations.NotNull;

public interface PaymentCancellationSpi extends AuthorisationSpi<SpiPayment> {
    /**
     * Initiates payment cancellation process
     *
     * @param contextData              holder of call's context data (e.g. about PSU and TPP)
     * @param payment                  Payment to be cancelled
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return Payment cancellation response with information about transaction status and whether authorisation of the request is required
     */
    @NotNull
    SpiResponse<SpiPaymentCancellationResponse> initiatePaymentCancellation(@NotNull SpiContextData contextData, @NotNull SpiPayment payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * Cancels payment without performing strong customer authentication
     *
     * @param contextData              holder of call's context data (e.g. about PSU and TPP)
     * @param payment                  Payment to be cancelled
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return Return a positive or negative response as part of SpiResponse
     */
    @NotNull
    SpiResponse<SpiResponse.VoidResponse> cancelPaymentWithoutSca(@NotNull SpiContextData contextData, @NotNull SpiPayment payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * Sends authorisation confirmation information (secure code or such) to ASPSP and if case of successful validation cancels payment at ASPSP.
     *
     * @param contextData              holder of call's context data (e.g. about PSU and TPP)
     * @param spiScaConfirmation       payment cancellation confirmation information
     * @param payment                  Payment to be cancelled
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return Return a positive or negative response as part of SpiResponse
     */
    @NotNull
    SpiResponse<SpiPaymentExecutionResponse> verifyScaAuthorisationAndCancelPaymentWithResponse(@NotNull SpiContextData contextData, @NotNull SpiScaConfirmation spiScaConfirmation, @NotNull SpiPayment payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);
}
