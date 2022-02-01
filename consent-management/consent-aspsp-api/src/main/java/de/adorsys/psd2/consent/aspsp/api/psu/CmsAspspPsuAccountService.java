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

package de.adorsys.psd2.consent.aspsp.api.psu;

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CmsAspspPsuAccountService {

    /**
     * Sets status RevokedByPsu of all AIS and PIIS consents for current PSU which has account
     *
     * @param aspspAccountId Bank specific account identifier
     * @param psuIdData      Optional Psu information criteria
     * @param instanceId     Mandatory id of particular service instance
     * @return <code>true</code> if all consent for this psu were revoked. <code>false</code> otherwise.
     */
    boolean revokeAllConsents(@Nullable String aspspAccountId, @NotNull PsuIdData psuIdData, @Nullable String instanceId);
}
