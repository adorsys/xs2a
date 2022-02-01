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

package de.adorsys.psd2.consent.api;

import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface AspspDataService {
    /**
     * Reads an AspspConsentData object by the Consent ID / Payment ID
     *
     * @param id AIS/PIIS Consent ID / Payment ID that identifies the data
     * @return <code>AspspConsentData</code> if consent was found. <code>false</code> otherwise.
     */
    @NotNull
    Optional<AspspConsentData> readAspspConsentData(@NotNull String id);

    /**
     * Writes/Updates an AspspConsentData by the consent ID given in it
     *
     * @param aspspConsentData aspspConsentData to be put. If aspspConsentData is null it will be removed
     * @return <code>true</code> if consent was found and data was updated. <code>false</code> otherwise.
     */
    boolean updateAspspConsentData(@NotNull AspspConsentData aspspConsentData);

    /**
     * Deletes an AspspConsentData object by the Consent ID / Payment ID
     *
     * @param id AIS/PIIS Consent ID / Payment ID that identifies the data
     * @return <code>true</code> if consent was found and data was deleted. <code>false</code> otherwise.
     */
    boolean deleteAspspConsentData(@NotNull String id);
}
