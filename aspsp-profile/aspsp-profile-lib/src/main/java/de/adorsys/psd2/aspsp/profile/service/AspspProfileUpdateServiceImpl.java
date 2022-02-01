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

import de.adorsys.psd2.aspsp.profile.config.BankProfileSetting;
import de.adorsys.psd2.aspsp.profile.config.ProfileConfigurations;
import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.mapper.AspspSettingsToBankProfileSettingMapper;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AspspProfileUpdateServiceImpl implements AspspProfileUpdateService {

    private final ProfileConfigurations profileConfigurations;
    private final AspspSettingsToBankProfileSettingMapper profileSettingMapper;

    /**
     * Update sca approach
     *
     * @param scaApproaches the new value of scaApproach list
     */
    @Override
    public void updateScaApproaches(List<ScaApproach> scaApproaches, String instanceId) {
        profileConfigurations.getSetting(instanceId)
            .getCommon()
            .setScaApproachesSupported(scaApproaches);
    }

    /**
     * Update all aspsp settings (frequency per day, combined service indicator, available payment products, available payment types,
     * is tpp signature required, PIS redirect URL, AIS redirect URL, multicurrency account level, is bank offered consent supported,
     * available booking statuses, supported account reference fields, consent lifetime, transaction lifetime, allPsd2 support,
     * transactions without balances support, signing basket support, is payment cancellation authorisation mandated, piis consent support,
     * delta report support, redirect url expiration time, type of authorisation start, etc.) except SCA approach
     *
     * @param aspspSettings new aspsp specific settings which to be stored in profile
     */
    @Override
    public void updateAspspSettings(@NotNull AspspSettings aspspSettings, String instanceId) {
        BankProfileSetting setting = profileConfigurations.getSetting(instanceId);
        profileSettingMapper.updateBankProfileSetting(aspspSettings, setting);
    }

    @Override
    public void enableMultitenancy(Boolean multitenancyEnabled) {
        profileConfigurations.setMultitenancyEnabled(multitenancyEnabled);
    }
}
