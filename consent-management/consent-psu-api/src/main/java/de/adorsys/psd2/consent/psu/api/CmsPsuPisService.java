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

package de.adorsys.psd2.consent.psu.api;

import de.adorsys.psd2.consent.api.pis.CmsBasePaymentResponse;
import de.adorsys.psd2.consent.api.pis.CmsPaymentResponse;
import de.adorsys.psd2.consent.api.pis.UpdatePaymentRequest;
import de.adorsys.psd2.consent.psu.api.pis.CmsPisPsuDataAuthorisation;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.exception.RedirectUrlIsExpiredException;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public interface CmsPsuPisService {
    /**
     * Updates PSU Data in Payment, based on the trusted information about PSU known to ASPSP (i.e. after authorisation)
     *
     * @param psuIdData  PSU credentials data to put. If some fields are nullable, the existing values will be overwritten.
     * @param redirectId ID of redirect
     * @param instanceId optional ID of particular service instance
     * @return <code>true</code> if payment was found and data was updated. <code>false</code> otherwise.
     * @throws AuthorisationIsExpiredException if authorisation is expired
     */
    boolean updatePsuInPayment(@NotNull PsuIdData psuIdData, @NotNull String redirectId, @NotNull String instanceId) throws AuthorisationIsExpiredException;

    /**
     * Returns Payment object by its ID
     *
     * @param psuIdData  PSU credentials data
     * @param paymentId  ID of Payment
     * @param instanceId optional ID of particular service instance
     * @return Payment object if it was found and it corresponds to the user data given in parameter
     */
    @NotNull
    Optional<CmsBasePaymentResponse> getPayment(@NotNull PsuIdData psuIdData, @NotNull String paymentId, @NotNull String instanceId);

    /**
     * Checks redirect URL and corresponding authorisation on expiration and returns Payment Response object if authorisation is valid
     *
     * @param redirectId ID of redirect
     * @param instanceId optional ID of particular service instance
     * @return Payment Response object that includes payment, authorisation id and ok/nok tpp redirect urls, if the payment was found
     * @throws RedirectUrlIsExpiredException if redirect urls are expired
     */
    @NotNull
    Optional<CmsPaymentResponse> checkRedirectAndGetPayment(@NotNull String redirectId, @NotNull String instanceId) throws RedirectUrlIsExpiredException;

    /**
     * Checks redirect url and corresponding authorisation on expiration for payment cancellation and returns Payment Response object if authorisation is valid
     *
     * @param redirectId ID of redirect
     * @param instanceId optional ID of particular service instance
     * @return Payment Response object that includes payment, authorisation id and ok/nok tpp redirect urls, if the payment was found
     * @throws RedirectUrlIsExpiredException if redirect urls are expired
     */
    @NotNull
    Optional<CmsPaymentResponse> checkRedirectAndGetPaymentForCancellation(@NotNull String redirectId, @NotNull String instanceId) throws RedirectUrlIsExpiredException;

    /**
     * Returns Authorisation object by its ID
     *
     * @param authorisationId ID of authorisation
     * @param instanceId      optional ID of particular service instance
     * @return Authorisation object if it was found
     */
    @NotNull
    Optional<CmsPsuAuthorisation> getAuthorisationByAuthorisationId(@NotNull String authorisationId, @NotNull String instanceId);

    /**
     * Updates a Status of Payment's authorisation by its ID and PSU ID
     *
     * @param psuIdData                PSU credentials data
     * @param paymentId                ID of Payment
     * @param authorisationId          ID of Authorisation process
     * @param status                   Status of Authorisation to be set
     * @param instanceId               optional ID of particular service instance
     * @param authenticationDataHolder optional parameter for online-banking, chosen method ID and authentication data
     * @return <code>true</code> if payment was found and status was updated. <code>false</code> otherwise.
     * @throws AuthorisationIsExpiredException if authorisation is expired
     */
    boolean updateAuthorisationStatus(@NotNull PsuIdData psuIdData, @NotNull String paymentId, @NotNull String authorisationId, @NotNull ScaStatus status, @NotNull String instanceId, AuthenticationDataHolder authenticationDataHolder) throws AuthorisationIsExpiredException;

    /**
     * Updates a Status of Payment object by its ID and PSU ID
     *
     * @param paymentId  ID of Payment
     * @param status     Status of Payment to be set
     * @param instanceId optional ID of particular service instance
     * @return <code>true</code> if payment was found and status was updated. <code>false</code> otherwise.
     */
    boolean updatePaymentStatus(@NotNull String paymentId, @NotNull TransactionStatus status, @NotNull String instanceId);

    /**
     * Returns list of info objects about psu data and authorisation scaStatuses
     *
     * @param paymentId    ID of Payment
     * @param instanceId   optional ID of particular service instance
     * @param pageIndex    index of current page
     * @param itemsPerPage quantity of psu authorisations on one page
     * @return list of info objects about psu data and authorisation scaStatuses
     */
    Optional<List<CmsPisPsuDataAuthorisation>> getPsuDataAuthorisations(@NotNull String paymentId, @NotNull String instanceId, Integer pageIndex, Integer itemsPerPage);

    /**
     * Updates Payment object by its ID
     *
     * @param updatePisCommonPaymentRequest The Payment to be set
     * @return <code>true</code> if payment was found and updated. <code>false</code> otherwise.
     */
    boolean updatePayment(UpdatePaymentRequest updatePisCommonPaymentRequest);
}
