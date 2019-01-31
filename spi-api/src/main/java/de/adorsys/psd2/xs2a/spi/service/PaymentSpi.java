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
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentExecutionResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import org.jetbrains.annotations.NotNull;

public interface PaymentSpi<T extends SpiPayment, R> {

    /**
     * Initiates payment
     *
     * @param contextData             holder of call's context data (e.g. about PSU and TPP)
     * @param payment                 T payment, that extends SpiPayment
     * @param initialAspspConsentData Encrypted data to be stored in the consent management system
     * @return Returns a positive or negative payment initiation response as a part of SpiResponse
     */
    @NotNull
    SpiResponse<R> initiatePayment(@NotNull SpiContextData contextData, @NotNull T payment, @NotNull AspspConsentData initialAspspConsentData);

    /**
     * Reads payment by id
     *
     * @param contextData      holder of call's context data (e.g. about PSU and TPP)
     * @param payment          T payment, that extends SpiPayment
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request
     * @return Returns T payment as a part of SpiResponse
     */
    @NotNull
    SpiResponse<T> getPaymentById(@NotNull SpiContextData contextData, @NotNull T payment, @NotNull AspspConsentData aspspConsentData);

    /**
     * Reads payment status by id
     *
     * @param contextData      holder of call's context data (e.g. about PSU and TPP)
     * @param payment          T payment, that extends SpiPayment
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request
     * @return Returns SpiTransactionStatus as a part of SpiResponse
     */
    @NotNull
    SpiResponse<SpiTransactionStatus> getPaymentStatusById(@NotNull SpiContextData contextData, @NotNull T payment, @NotNull AspspConsentData aspspConsentData);

    /**
     * Executes payment - to be used in a case, when none SCA methods exist
     *
     * @param contextData      holder of call's context data (e.g. about PSU and TPP)
     * @param payment          T payment, that extends SpiPayment
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request
     * @return Returns a response object, which contains the transaction status. For multilevel SCA, PATC status should be returned for all successful authorisation but the last
     */
    @NotNull
    SpiResponse<SpiPaymentExecutionResponse> executePaymentWithoutSca(@NotNull SpiContextData contextData, @NotNull T payment, @NotNull AspspConsentData aspspConsentData);

    /**
     * Sends authorisation confirmation information (secure code or such) to ASPSP and if case of successful validation executes payment at ASPSP. Used only with embedded SCA Approach.
     *
     * @param contextData        holder of call's context data (e.g. about PSU and TPP)
     * @param spiScaConfirmation payment confirmation information
     * @param payment            payment object
     * @param aspspConsentData   Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                           May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return Returns a response object, which contains the transaction status. For multilevel SCA, PATC status should be returned for all successful authorisations but the last
     */
    @NotNull
    SpiResponse<SpiPaymentExecutionResponse> verifyScaAuthorisationAndExecutePayment(@NotNull SpiContextData contextData, @NotNull SpiScaConfirmation spiScaConfirmation, @NotNull T payment, @NotNull AspspConsentData aspspConsentData);
}
