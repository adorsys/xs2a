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

package de.adorsys.psd2.consent.aspsp.api.piis;

import de.adorsys.psd2.consent.api.piis.v1.CmsPiisConsent;
import de.adorsys.psd2.consent.aspsp.api.PageData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public interface CmsAspspPiisService {
    /**
     * Creates new PIIS consent. Consent gets status "Valid".
     *
     * @param psuIdData PSU credentials data
     * @param request   Request with all information for creating PIIS consent.
     * @return Consent ID if the consent was created
     */
    Optional<String> createConsent(@NotNull PsuIdData psuIdData, @NotNull CreatePiisConsentRequest request, @NotNull String instanceId);

    /**
     * Terminates PIIS Consent object by its ID. Consent gets status "Terminated by ASPSP".
     *
     * @param consentId  ID of Consent
     * @param instanceId ID of the particular service instance
     * @return <code>true</code> if consent was found and terminated. <code>false</code> otherwise.
     */
    boolean terminateConsent(@NotNull String consentId, @NotNull String instanceId);


    /**
     * Returns a list of PIIS Consent objects by PSU ID
     *
     * @param psuIdData  PSU credentials data
     * @param instanceId ID of the particular service instance
     * @param pageIndex index of current page
     * @param itemsPerPage quantity of consents on one page
     * @return List of PIIS Consent objects corresponding to the given PSU
     */
    @NotNull
    PageData<List<CmsPiisConsent>> getConsentsForPsu(@NotNull PsuIdData psuIdData, @NotNull String instanceId, Integer pageIndex, Integer itemsPerPage);
}
