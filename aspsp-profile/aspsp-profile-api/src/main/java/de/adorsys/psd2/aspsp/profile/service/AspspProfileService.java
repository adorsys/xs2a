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

public interface AspspProfileService {
    /**
     * Reads all aspsp settings (frequency per day, combined service indicator, available payment products, available payment types,
     * is tpp signature required, PIS redirect URL, AIS redirect URL, multicurrency account level, is bank offered consent supported,
     * available booking statuses, supported account reference fields, consent lifetime, transaction lifetime, allPsd2 support,
     * transactions without balances support, signing basket support, is payment cancellation authorisation mandated, piis consent support,
     * delta report support, redirect url expiration time and type of authorisation start) except SCA approach
     *
     * @return aspsp specific settings method which is stored in profile
     */
    AspspSettings getAspspSettings(String instanceId);

    /**
     * Reads list of sca approaches
     *
     * @return list of sca approaches ordered by priority which is stored in profile
     */
    List<ScaApproach> getScaApproaches(String instanceId);

    boolean isMultitenancyEnabled();
}
