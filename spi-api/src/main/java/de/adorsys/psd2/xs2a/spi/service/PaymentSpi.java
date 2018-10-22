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

import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import org.jetbrains.annotations.NotNull;

// TODO add javadoc https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/438
interface PaymentSpi<T extends SpiPayment, R> {

    @NotNull
    SpiResponse<R> initiatePayment(@NotNull SpiPsuData psuData, @NotNull T payment, @NotNull AspspConsentData initialAspspConsentData);

    @NotNull
    SpiResponse<T> getPaymentById(@NotNull SpiPsuData psuData, @NotNull T payment, @NotNull AspspConsentData aspspConsentData);

    @NotNull
    SpiResponse<SpiTransactionStatus> getPaymentStatusById(@NotNull SpiPsuData psuData, @NotNull T payment, @NotNull AspspConsentData aspspConsentData);

    @NotNull
    SpiResponse<SpiResponse.VoidResponse> executePaymentWithoutSca(@NotNull SpiPsuData psuData, @NotNull T payment, @NotNull AspspConsentData aspspConsentData);

    /**
     * Sends authorisation confirmation information (secure code or such) to ASPSP and if case of successful validation executes payment at ASPSP. Used only with embedded SCA Approach.
     *
     * @param psuData            ASPSP identifier(s) of the psu
     * @param spiScaConfirmation payment confirmation information
     * @param payment            payment object
     * @param aspspConsentData   Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                           May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return Return a positive or negative response as part of SpiResponse
     */
    @NotNull
    SpiResponse<SpiResponse.VoidResponse> verifyScaAuthorisationAndExecutePayment(@NotNull SpiPsuData psuData, @NotNull SpiScaConfirmation spiScaConfirmation, @NotNull T payment, @NotNull AspspConsentData aspspConsentData);
}
