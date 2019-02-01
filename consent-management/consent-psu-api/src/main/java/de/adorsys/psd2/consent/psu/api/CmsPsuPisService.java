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

package de.adorsys.psd2.consent.psu.api;

import de.adorsys.psd2.consent.api.pis.CmsPayment;
import de.adorsys.psd2.consent.api.pis.CmsPaymentResponse;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface CmsPsuPisService {
    /**
     * Updates PSU Data in Payment, based on the trusted information about PSU known to ASPSP (i.e. after authorisation)
     *
     * @param psuIdData  PSU credentials data to put. If some fields are nullable, the existing values will be overwritten.
     * @param redirectId ID of redirect
     * @return <code>true</code> if payment was found and data was updated. <code>false</code> otherwise.
     */
    boolean updatePsuInPayment(@NotNull PsuIdData psuIdData, @NotNull String redirectId, @NotNull String instanceId);

    /**
     * Returns Payment object by its ID
     *
     * @param psuIdData PSU credentials data
     * @param paymentId ID of Payment
     * @return Payment object if it was found and it corresponds to the user data given in parameter
     */
    @NotNull
    Optional<CmsPayment> getPayment(@NotNull PsuIdData psuIdData, @NotNull String paymentId, @NotNull String instanceId);

    /**
     * Checks redirect url and corresponding authorisation on expiration and returns Payment Response object if authorisation is valid
     *
     * @param redirectId ID of redirect
     * @return Payment Response object that includes payment, authorisation id and ok/nok tpp redirect urls, if the payment was found
     */
    @NotNull
    Optional<CmsPaymentResponse> checkRedirectAndGetPayment(@NotNull String redirectId, @NotNull String instanceId);

    /**
     * Checks redirect url and corresponding authorisation on expiration for payment cancellation and returns Payment Response object if authorisation is valid
     *
     * @param redirectId ID of redirect
     * @return Payment Response object that includes payment, authorisation id and ok/nok tpp redirect urls, if the payment was found
     */
    @NotNull
    Optional<CmsPaymentResponse> checkRedirectAndGetPaymentForCancellation(@NotNull String redirectId, @NotNull String instanceId);

    /**
     * Updates a Status of Payment's autorisation by its ID and PSU ID
     *
     * @param psuIdData       PSU credentials data
     * @param paymentId       ID of Payment
     * @param authorisationId ID of Authorisation process
     * @param status          Status of Authorisation to be set
     * @return <code>true</code> if payment was found and status was updated. <code>false</code> otherwise.
     */
    boolean updateAuthorisationStatus(@NotNull PsuIdData psuIdData, @NotNull String paymentId, @NotNull String authorisationId, @NotNull ScaStatus status, @NotNull String instanceId);

    /**
     * Updates a Status of Payment object by its ID and PSU ID
     *
     * @param paymentId ID of Payment
     * @param status    Status of Payment to be set
     * @return <code>true</code> if payment was found and status was updated. <code>false</code> otherwise.
     */
    boolean updatePaymentStatus(@NotNull String paymentId, @NotNull TransactionStatus status, @NotNull String instanceId);
}
