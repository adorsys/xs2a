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

import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;


public interface CmsPsuAisService {
    /**
     * Updates PSU Data in consent, based on the trusted information about PSU known to ASPSP (i.e. after authorisation)
     *
     * @param psuIdData PSU credentials data to put. If some fields are nullable, the existing values will be overwritten.
     * @param consentId External ID of Consent known to TPP and ASPSP
     * @return <code>true</code> if consent was found and data was updated. <code>false</code> otherwise.
     */
    boolean updatePsuDataInConsent(@NotNull PsuIdData psuIdData, @NotNull String consentId);

    /**
     * Returns AIS Consent object by its ID
     *
     * @param psuIdData PSU credentials data
     * @param consentId ID of Consent
     * @return Consent object if it was found and it corresponds to the user data given in parameter
     */
    @NotNull
    Optional<AisAccountConsent> getConsent(@NotNull PsuIdData psuIdData, @NotNull String consentId);

    /**
     * Updates a Status of AIS Consent Authorisation by its ID and PSU ID
     *
     * @param psuIdData PSU credentials data
     * @param consentId ID of Consent
     * @param authorisationId ID of Authorisation process
     * @param status    Status of Authorisation to be set
     * @return <code>true</code> if consent was found and status was updated. <code>false</code> otherwise.
     */
    boolean updateAuthorisationStatus(@NotNull PsuIdData psuIdData, @NotNull String consentId, @NotNull String authorisationId, @NotNull ScaStatus status);


    /**
     * Updates a Status of AIS Consent object by its ID and PSU ID
     *
     * @param psuIdData PSU credentials data
     * @param consentId ID of Consent
     * @param status    Status of Consent to be set
     * @return <code>true</code> if consent was found and status was updated. <code>false</code> otherwise.
     */
    boolean updateConsentStatus(@NotNull PsuIdData psuIdData, @NotNull String consentId, @NotNull ConsentStatus status);

    /**
     * Returns a list of AIS Consent objects by PSU ID
     *
     * @param psuIdData PSU credentials data
     * @return List of AIS Consent objects corresponding to the given PSU
     */
    @NotNull
    List<AisAccountConsent> getConsentsForPsu(@NotNull PsuIdData psuIdData);

    /**
     * Revokes AIS Consent object by its ID. Consent gets status "Revoked by PSU".
     *
     * @param psuIdData PSU credentials data
     * @param consentId ID of Consent
     * @return <code>true</code> if consent was found and revoked. <code>false</code> otherwise.
     */
    boolean revokeConsent(@NotNull PsuIdData psuIdData, @NotNull String consentId);
}
