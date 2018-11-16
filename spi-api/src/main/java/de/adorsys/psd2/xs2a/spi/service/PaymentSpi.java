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
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse.VoidResponse;
import org.jetbrains.annotations.NotNull;

public interface PaymentSpi<T extends SpiPayment, R> {

    /**
     * Initiates payment
     *
     * @param psuData                 SpiPsuData container of authorisation data about PSU
     * @param payment                 T payment, that extends SpiPayment
     * @param initialAspspConsentData Encrypted data to be stored in the consent management system
     * @return Returns a positive or negative payment initiation response as a part of SpiResponse
     */
    @NotNull
    SpiResponse<R> initiatePayment(@NotNull SpiPsuData psuData, @NotNull T payment, @NotNull AspspConsentData initialAspspConsentData);

    /**
     * Reads payment by id
     *
     * @param psuData          SpiPsuData container of authorisation data about PSU
     * @param payment          T payment, that extends SpiPayment
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request
     * @return Returns T payment as a part of SpiResponse
     */
    @NotNull
    SpiResponse<T> getPaymentById(@NotNull SpiPsuData psuData, @NotNull T payment, @NotNull AspspConsentData aspspConsentData);

    /**
     * Reads payment status by id
     *
     * @param psuData          SpiPsuData container of authorisation data about PSU
     * @param payment          T payment, that extends SpiPayment
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request
     * @return Returns SpiTransactionStatus as a part of SpiResponse
     */
    @NotNull
    SpiResponse<SpiTransactionStatus> getPaymentStatusById(@NotNull SpiPsuData psuData, @NotNull T payment, @NotNull AspspConsentData aspspConsentData);

    /**
     * Executes payment - to be used in a case, when none SCA methods exist
     *
     * @param psuData          SpiPsuData container of authorisation data about PSU
     * @param payment          T payment, that extends SpiPayment
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request
     * @return Returns a positive or negative response as part of SpiResponse
     */
    @NotNull
    SpiResponse<VoidResponse> executePaymentWithoutSca(@NotNull SpiPsuData psuData, @NotNull T payment, @NotNull AspspConsentData aspspConsentData);

    /**
     * Sends authorisation confirmation information (secure code or such) to ASPSP and if case of successful validation executes payment at ASPSP. Used only with embedded SCA Approach.
     *
     * @param psuData            ASPSP identifier(s) of the psu
     * @param spiScaConfirmation payment confirmation information
     * @param payment            payment object
     * @param aspspConsentData   Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                           May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return Returns a positive or negative response as part of SpiResponse
     */
    @NotNull
    SpiResponse<VoidResponse> verifyScaAuthorisationAndExecutePayment(@NotNull SpiPsuData psuData, @NotNull SpiScaConfirmation spiScaConfirmation, @NotNull T payment, @NotNull AspspConsentData aspspConsentData);
}
