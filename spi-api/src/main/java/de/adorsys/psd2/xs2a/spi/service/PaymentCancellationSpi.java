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

import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentCancellationResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import org.jetbrains.annotations.NotNull;

public interface PaymentCancellationSpi extends AuthorisationSpi<SpiPayment> {
    /**
     * Initiates payment cancellation process
     *
     * @param psuData          ASPSP identifier(s) of the psu
     * @param payment          Payment to be cancelled
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     * @return Payment cancellation response with information about transaction status and whether authorisation of the request is required
     */
    @NotNull
    SpiResponse<SpiPaymentCancellationResponse> initiatePaymentCancellation(@NotNull SpiPsuData psuData, @NotNull SpiPayment payment, @NotNull AspspConsentData aspspConsentData);

    /**
     * Cancels payment without performing strong customer authentication
     *
     * @param psuData          ASPSP identifier(s) of the psu
     * @param payment          Payment to be cancelled
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return Return a positive or negative response as part of SpiResponse
     */
    @NotNull
    SpiResponse<SpiResponse.VoidResponse> cancelPaymentWithoutSca(@NotNull SpiPsuData psuData, @NotNull SpiPayment payment, @NotNull AspspConsentData aspspConsentData);

    /**
     * Sends authorisation confirmation information (secure code or such) to ASPSP and if case of successful validation cancels payment at ASPSP.
     *
     * @param psuData            ASPSP identifier(s) of the psu
     * @param spiScaConfirmation payment cancellation confirmation information
     * @param payment            Payment to be cancelled
     * @param aspspConsentData   Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                           May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return Return a positive or negative response as part of SpiResponse
     */
    @NotNull
    SpiResponse<SpiResponse.VoidResponse> verifyScaAuthorisationAndCancelPayment(@NotNull SpiPsuData psuData, @NotNull SpiScaConfirmation spiScaConfirmation, @NotNull SpiPayment payment, @NotNull AspspConsentData aspspConsentData);
}
