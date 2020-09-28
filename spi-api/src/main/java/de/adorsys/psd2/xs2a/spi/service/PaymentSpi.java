/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiCheckConfirmationCodeRequest;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiGetPaymentStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentConfirmationCodeValidationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentExecutionResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import org.jetbrains.annotations.NotNull;

public interface PaymentSpi<T extends SpiPayment, R> {

    /**
     * Initiates payment
     *
     * @param contextData              holder of call's context data (e.g. about PSU and TPP)
     * @param payment                  T payment, that extends SpiPayment
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return Returns a positive or negative payment initiation response as a part of SpiResponse
     */
    @NotNull
    SpiResponse<R> initiatePayment(@NotNull SpiContextData contextData, @NotNull T payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * Reads payment by id
     *
     * @param contextData              holder of call's context data (e.g. about PSU and TPP)
     * @param acceptMediaType          requested by TPP response media type e.g. application/json. This string may contain several content-types according to HTTP "Accept"-Header format.
     * @param payment                  T payment, that extends SpiPayment
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return Returns T payment as a part of SpiResponse
     */
    @NotNull
    SpiResponse<T> getPaymentById(@NotNull SpiContextData contextData, @NotNull String acceptMediaType, @NotNull T payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * Reads payment status by id
     *
     * @param contextData              holder of call's context data (e.g. about PSU and TPP)
     * @param acceptMediaType          requested by TPP response media type e.g. application/json. This string may contain several content-types according to HTTP "Accept"-Header format.
     *                                 If desired media type is not possible to provide, NOT_SUPPORTED error should be returned. To provide formats other than JSON, use {@link SpiGetPaymentStatusResponse#paymentStatusRaw}
     * @param payment                  T payment, that extends SpiPayment
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return Returns a response that contains the current transaction status and a flag indicating the availability of funds, as a part of SpiResponse
     */
    @NotNull
    SpiResponse<SpiGetPaymentStatusResponse> getPaymentStatusById(@NotNull SpiContextData contextData, @NotNull String acceptMediaType, @NotNull T payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * Executes payment - to be used in a case, when none SCA methods exist
     *
     * @param contextData              holder of call's context data (e.g. about PSU and TPP)
     * @param payment                  T payment, that extends SpiPayment
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return Returns a response object, which contains the transaction status. For multilevel SCA, PATC status should be returned for all successful authorisation but the last
     */
    @NotNull
    SpiResponse<SpiPaymentExecutionResponse> executePaymentWithoutSca(@NotNull SpiContextData contextData, @NotNull T payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * Sends authorisation confirmation information (secure code or such) to ASPSP and if case of successful validation executes payment at ASPSP. Used only with embedded SCA Approach.
     *
     * @param contextData              holder of call's context data (e.g. about PSU and TPP)
     * @param spiScaConfirmation       payment confirmation information
     * @param payment                  payment object
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     *                                 May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return Returns a response object, which contains the transaction status. For multilevel SCA, PATC status should be returned for all successful authorisations but the last
     */
    @NotNull
    SpiResponse<SpiPaymentExecutionResponse> verifyScaAuthorisationAndExecutePaymentWithPaymentResponse(@NotNull SpiContextData contextData, @NotNull SpiScaConfirmation spiScaConfirmation, @NotNull T payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * Checks confirmation data at the ASPSP side in case of XS2A not supporting validation of this data. Used only with redirect SCA Approach.
     *
     * @param contextData                     holder of call's context data (e.g. about PSU and TPP)
     * @param spiCheckConfirmationCodeRequest object with confirmation code and authorisation ID
     * @param aspspConsentDataProvider        Provides access to read/write encrypted data to be stored in the consent management system
     * @return Returns a response object, which contains checking result - if the confirmation data was correct or not
     */
    @NotNull
    SpiResponse<SpiPaymentConfirmationCodeValidationResponse> checkConfirmationCode(@NotNull SpiContextData contextData, @NotNull SpiCheckConfirmationCodeRequest spiCheckConfirmationCodeRequest, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * Notify ASPSP about validation result of confirmation code on XS2A side
     *
     * @param contextData                      holder of call's context data (e.g. about PSU and TPP)
     * @param confirmationCodeValidationResult validation result of confirmation code on XS2A side
     * @param payment                          payment object
     * @param isCancellation                   boolean representing if the notification related to payment cancellation
     * @param aspspConsentDataProvider         Provides access to read/write encrypted data to be stored in the consent management system
     * @return Returns a response object, which contains SCA status of authorisation and Transaction status of payment
     */
    @NotNull
    SpiResponse<SpiPaymentConfirmationCodeValidationResponse> notifyConfirmationCodeValidation(@NotNull SpiContextData contextData, boolean confirmationCodeValidationResult, @NotNull T payment, boolean isCancellation, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);
}
