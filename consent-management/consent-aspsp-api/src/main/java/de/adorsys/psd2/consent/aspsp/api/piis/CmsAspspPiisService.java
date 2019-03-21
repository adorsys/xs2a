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

package de.adorsys.psd2.consent.aspsp.api.piis;

import de.adorsys.psd2.xs2a.core.piis.PiisConsent;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CmsAspspPiisService {
    /**
     * Creates new PIIS consent. Consent gets status "Valid".
     *
     * @param psuIdData              PSU credentials data
     * @param tppInfo                TPP for which the consent will be created. If the value is omitted, the consent
     *                               will be created for all TPPs. If the TPP info object is present, it must contain
     *                               authorisation number and authority ID.
     * @param accounts               List of accounts for which the consent will be created
     * @param validUntil             Consent's expiration date
     * @param allowedFrequencyPerDay Maximum frequency for an access per day
     * @return Consent id if the consent was created
     *
     * @deprecated since 2.2, use {@link CmsAspspPiisService#createConsent(PsuIdData, CreatePiisConsentRequest)} instead
     */
    //TODO 2.5 Delete this method https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/759
    @Deprecated // Since 2.2
    Optional<String> createConsent(@NotNull PsuIdData psuIdData, @Nullable TppInfo tppInfo,
                                   @NotNull List<AccountReference> accounts, @NotNull LocalDate validUntil,
                                   int allowedFrequencyPerDay);

    /**
     * Creates new PIIS consent. Consent gets status "Valid".
     *
     * @param psuIdData              PSU credentials data
     * @param request                Request with all information for creating PIIS consent.
     * @return Consent id if the consent was created
     */
    Optional<String> createConsent(@NotNull PsuIdData psuIdData, @NotNull CreatePiisConsentRequest request);

    /**
     * Terminates PIIS Consent object by its ID. Consent gets status "Terminated by ASPSP".
     *
     * @param consentId  ID of Consent
     * @param instanceId Id of the particular service instance
     * @return <code>true</code> if consent was found and terminated. <code>false</code> otherwise.
     */
    boolean terminateConsent(@NotNull String consentId, @NotNull String instanceId);


    /**
     * Returns a list of PIIS Consent objects by PSU ID
     *
     * @param psuIdData  PSU credentials data
     * @param instanceId Id of the particular service instance
     * @return List of PIIS Consent objects corresponding to the given PSU
     */
    @NotNull
    List<PiisConsent> getConsentsForPsu(@NotNull PsuIdData psuIdData, @NotNull String instanceId);
}
