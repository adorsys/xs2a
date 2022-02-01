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

import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.CmsAisAccountConsent;
import de.adorsys.psd2.consent.api.ais.CmsAisConsentResponse;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisConsentAccessRequest;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisPsuDataAuthorisation;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.exception.RedirectUrlIsExpiredException;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;


public interface CmsPsuAisService {
    /**
     * Updates PSU Data in consent, based on the trusted information about PSU known to ASPSP (i.e. after authorisation)
     *
     * @param psuIdData       PSU credentials data to put. If some fields are nullable, the existing values will be overwritten.
     * @param authorisationId ID of authorisation
     * @param instanceId      optional ID of particular service instance
     * @return <code>true</code> if consent was found and data was updated. <code>false</code> otherwise.
     * @throws AuthorisationIsExpiredException if authorisation is expired
     */
    boolean updatePsuDataInConsent(@NotNull PsuIdData psuIdData, @NotNull String authorisationId, @NotNull String instanceId) throws AuthorisationIsExpiredException;

    /**
     * Returns AIS Consent object by its ID
     *
     * @param psuIdData  PSU credentials data
     * @param consentId  ID of Consent
     * @param instanceId optional ID of particular service instance
     * @return Consent object if it was found and it corresponds to the user data given in parameter
     */
    @NotNull
    Optional<CmsAisAccountConsent> getConsent(@NotNull PsuIdData psuIdData, @NotNull String consentId, @NotNull String instanceId);

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
     * Updates a Status of AIS Consent Authorisation by its ID and PSU ID
     *
     * @param psuIdData                PSU credentials data
     * @param consentId                ID of Consent
     * @param authorisationId          ID of Authorisation process
     * @param status                   Status of Authorisation to be set
     * @param instanceId               optional ID of particular service instance
     * @param authenticationDataHolder optional parameter for online-banking, chosen method ID and authentication data
     * @return <code>true</code> if consent was found and status was updated. <code>false</code> otherwise.
     * @throws AuthorisationIsExpiredException if authorisation is expired
     */
    boolean updateAuthorisationStatus(@NotNull PsuIdData psuIdData, @NotNull String consentId, @NotNull String authorisationId, @NotNull ScaStatus status, @NotNull String instanceId, AuthenticationDataHolder authenticationDataHolder) throws AuthorisationIsExpiredException;

    /**
     * Puts a Status of AIS Consent object by its ID to VALID
     *
     * @param consentId  ID of Consent
     * @param instanceId optional ID of particular service instance
     * @return <code>true</code> if consent was found and status was updated. <code>false</code> otherwise.
     * @throws WrongChecksumException in case of any attempt to change definite consent fields after its status became valid.
     */
    boolean confirmConsent(@NotNull String consentId, @NotNull String instanceId) throws WrongChecksumException;

    /**
     * Puts a Status of AIS Consent object by its ID and PSU ID to REJECTED
     *
     * @param consentId  ID of Consent
     * @param instanceId optional ID of particular service instance
     * @return <code>true</code> if consent was found and status was updated. <code>false</code> otherwise.
     * @throws WrongChecksumException in case of any attempt to change definite consent fields after its status became valid.
     */
    boolean rejectConsent(@NotNull String consentId, @NotNull String instanceId) throws WrongChecksumException;

    /**
     * Returns a list of AIS Consent objects by PSU ID
     *
     * @param psuIdData         PSU credentials data
     * @param instanceId        optional ID of particular service instance
     * @param additionalTppInfo additional tpp info
     * @param statuses          consent statuses
     * @param accountNumbers    account numbers (IBAN, BBAN...)
     * @param pageIndex         index of current page
     * @param itemsPerPage      quantity of consents on one page
     * @return List of AIS Consent objects corresponding to the given PSU
     */
    @NotNull
    List<CmsAisAccountConsent> getConsentsForPsuAndAdditionalTppInfo(@NotNull PsuIdData psuIdData, @NotNull String instanceId,
                                                                     @Nullable String additionalTppInfo,  @Nullable List<String> statuses,
                                                                     @Nullable List<String> accountNumbers,
                                                                     Integer pageIndex, Integer itemsPerPage);

    /**
     * Revokes AIS Consent object by its ID. Consent gets status "Revoked by PSU".
     *
     * @param consentId  ID of Consent
     * @param instanceId optional ID of particular service instance
     * @return <code>true</code> if consent was found and revoked. <code>false</code> otherwise.
     * @throws WrongChecksumException in case of any attempt to change definite consent fields after its status became valid.
     */
    boolean revokeConsent(@NotNull String consentId, @NotNull String instanceId) throws WrongChecksumException;

    /**
     * Returns CMS AIS consent identifier by redirect ID if redirect ID has not expired
     *
     * @param redirectId ID of redirect
     * @param instanceId optional ID of particular service instance
     * @return CMS AIS consent identifier if it has been found
     * @throws RedirectUrlIsExpiredException if redirect urls are expired
     */
    @NotNull
    Optional<CmsAisConsentResponse> checkRedirectAndGetConsent(@NotNull String redirectId, @NotNull String instanceId) throws RedirectUrlIsExpiredException;

    /**
     * Stores account access to Consent in CMS. Any existing account accesses will be removed and overwritten.
     *
     * @param consentId            ID of Consent, in which AccountAccess shall be saved
     * @param accountAccessRequest AccountAccess object with lists of AccountReferences. If empty, corresponding accesses to be removed.
     * @param instanceId           optional ID of particular service instance
     * @return false if Consent with this ID not found or in wrong state (Rejected, Revoked, Expired, Terminated by TPP or Terminated by ASPSP). True if consent was found.
     */
    boolean updateAccountAccessInConsent(@NotNull String consentId, @NotNull CmsAisConsentAccessRequest accountAccessRequest, @NotNull String instanceId);

    /**
     * Returns list of info objects about psu data and authorisation scaStatuses
     *
     * @param consentId    ID of Consent
     * @param instanceId   optional ID of particular service instance
     * @param pageIndex    index of current page
     * @param itemsPerPage quantity of consents on one page
     * @return list of info objects about psu data and authorisation scaStatuses
     */
    Optional<List<CmsAisPsuDataAuthorisation>> getPsuDataAuthorisations(@NotNull String consentId, @NotNull String instanceId, Integer pageIndex, Integer itemsPerPage);

    /**
     * Puts a Status of AIS Consent object by its ID and PSU ID to PARTIALLY_AUTHORISED
     *
     * @param consentId  ID of Consent
     * @param instanceId optional ID of particular service instance
     * @return <code>true</code> if consent was found and status was updated. <code>false</code> otherwise.
     * @throws WrongChecksumException in case of any attempt to change definite consent fields after its status became valid.
     */
    boolean authorisePartiallyConsent(@NotNull String consentId, @NotNull String instanceId) throws WrongChecksumException;
}
