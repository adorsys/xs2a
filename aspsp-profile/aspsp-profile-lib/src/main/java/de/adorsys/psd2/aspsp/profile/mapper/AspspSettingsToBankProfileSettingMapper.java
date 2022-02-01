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

package de.adorsys.psd2.aspsp.profile.mapper;

import de.adorsys.psd2.aspsp.profile.config.BankProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.domain.ais.AisAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.ais.AisAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.common.CommonAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.common.CommonAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisAspspProfileSetting;
import de.adorsys.psd2.xs2a.core.profile.StartAuthorisationMode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AspspSettingsToBankProfileSettingMapper {

    void updateBankProfileSetting(AspspSettings aspspSettings, @MappingTarget BankProfileSetting bankProfileSetting);

    @Named("mapStartAuthorisationMode")
    default String mapStartAuthorisationMode(StartAuthorisationMode startAuthorisationMode) {
        return startAuthorisationMode == null ? "AUTO" : startAuthorisationMode.getValue();
    }

    void updateAisAspspProfileBankSetting(AisAspspProfileSetting ais, @MappingTarget AisAspspProfileBankSetting aisAspspProfileBankSetting);

    void updatePisAspspProfileBankSetting(PisAspspProfileSetting pis, @MappingTarget PisAspspProfileBankSetting pisAspspProfileBankSetting);

    void updatePiisAspspProfileBankSetting(PiisAspspProfileSetting piis, @MappingTarget PiisAspspProfileBankSetting piisAspspProfileBankSetting);

    @Mapping(target = "startAuthorisationMode", source = "startAuthorisationMode", qualifiedByName = "mapStartAuthorisationMode")
    void updateCommonAspspProfileBankSetting(CommonAspspProfileSetting common, @MappingTarget CommonAspspProfileBankSetting commonAspspProfileBankSetting);
}
