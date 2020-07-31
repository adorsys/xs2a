/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import de.adorsys.psd2.consent.api.piis.v2.CmsConfirmationOfFundsConsent;
import de.adorsys.psd2.consent.api.piis.v2.CmsConfirmationOfFundsResponse;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.exception.RedirectUrlIsExpiredException;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface CmsPsuConfirmationOfFundsService {

    /**
     * Updates a Status of Confirmation of Funds Consent Authorisation by its ID and PSU ID
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
    boolean updateAuthorisationStatus(@NotNull PsuIdData psuIdData, @NotNull String consentId, @NotNull String authorisationId,
                                      @NotNull ScaStatus status, @NotNull String instanceId,
                                      AuthenticationDataHolder authenticationDataHolder) throws AuthorisationIsExpiredException;

    /**
     * Returns CMS confirmation of funds consent identifier by redirect ID if redirect ID has not expired
     *
     * @param redirectId ID of redirect
     * @param instanceId optional ID of particular service instance
     * @return CMS confirmation of funds consent identifier if it has been found
     * @throws RedirectUrlIsExpiredException if redirect urls are expired
     */
    Optional<CmsConfirmationOfFundsResponse> checkRedirectAndGetConsent(String redirectId, String instanceId) throws RedirectUrlIsExpiredException;


    /**
     * Updates PSU Data in Confirmation of Funds Consent, based on the trusted information about PSU known to ASPSP (i.e. after authorisation)
     *
     * @param psuIdData       PSU credentials data to put. If some fields are nullable, the existing values will be overwritten.
     * @param authorisationId ID of authorisation
     * @param instanceId      optional ID of particular service instance
     * @return <code>true</code> if consent was found and data was updated. <code>false</code> otherwise.
     * @throws AuthorisationIsExpiredException if authorisation is expired
     */
    boolean updatePsuDataInConsent(@NotNull PsuIdData psuIdData, @NotNull String authorisationId, @NotNull String instanceId) throws AuthorisationIsExpiredException;

    /**
     * Returns Authorisation object by its ID
     *
     * @param authorisationId ID of authorisation
     * @param instanceId      optional ID of particular service instance
     * @return Authorisation object if it was found
     */
    @NotNull
    Optional<CmsPsuConfirmationOfFundsAuthorisation> getAuthorisationByAuthorisationId(@NotNull String authorisationId, @NotNull String instanceId) throws AuthorisationIsExpiredException;

    /**
     * Updates a Status of Confirmation of Funds Consent by its ID
     *
     * @param consentId  ID of Consent
     * @param status     Status of Confirmation of Funds Consent
     * @param instanceId optional ID of particular service instance
     * @return <code>true</code> if consent was found and status was updated. <code>false</code> otherwise.
     */
    boolean updateConsentStatus(@NotNull String consentId, @NotNull ConsentStatus status, @NotNull String instanceId);

    /**
     * Returns confirmation of funds Consent object by its ID
     *
     * @param psuIdData  PSU credentials data
     * @param consentId  ID of Consent
     * @param instanceId optional ID of particular service instance
     * @return Consent object if it was found and it corresponds to the user data given in parameter
     */
    @NotNull
    Optional<CmsConfirmationOfFundsConsent> getConsent(@NotNull PsuIdData psuIdData, @NotNull String consentId, @NotNull String instanceId);

}
