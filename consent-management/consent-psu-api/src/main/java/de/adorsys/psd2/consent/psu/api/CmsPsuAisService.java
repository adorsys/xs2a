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
     * @param psuIdData          PSU credentials data
     * @param consentId          ID of Consent
     * @param authorisationId    ID of Authorisation process
     * @param status             Status of Authorisation to be set
     * @param instanceId         optional ID of particular service instance
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
     * @param psuIdData  PSU credentials data
     * @param instanceId optional ID of particular service instance
     * @param pageIndex index of current page
     * @param itemsPerPage quantity of consents on one page
     * @return List of AIS Consent objects corresponding to the given PSU
     */
    @NotNull
    List<CmsAisAccountConsent> getConsentsForPsu(@NotNull PsuIdData psuIdData, @NotNull String instanceId, Integer pageIndex, Integer itemsPerPage);

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
     * @param consentId  ID of Consent
     * @param instanceId optional ID of particular service instance
     * @param pageIndex index of current page
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
