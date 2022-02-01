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

package de.adorsys.psd2.aspsp.profile.service;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;

import java.util.List;

public interface AspspProfileUpdateService {
    /**
     * Updates SCA approaches in ASPSP profile by replacing all existing approaches with new ones
     *
     * @param scaApproaches SCA approaches to be set in the profile
     */
    void updateScaApproaches(List<ScaApproach> scaApproaches, String instanceId);

    /**
     * Updates ASPSP settings in ASPSP profile by replacing all existing values with new ones
     *
     * @param aspspSettings ASPSP Settings to be set in the profile
     */
    void updateAspspSettings(AspspSettings aspspSettings, String instanceId);

    void enableMultitenancy(Boolean multitenancyEnabled);
}
