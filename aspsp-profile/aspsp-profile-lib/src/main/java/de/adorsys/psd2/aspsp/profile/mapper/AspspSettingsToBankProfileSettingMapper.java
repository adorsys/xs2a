/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

@Mapper(componentModel = "spring")
public interface AspspSettingsToBankProfileSettingMapper {

    void updateBankProfileSetting(AspspSettings aspspSettings, @MappingTarget BankProfileSetting bankProfileSetting);

    default String mapStartAuthorisationMode(StartAuthorisationMode startAuthorisationMode) {
        return startAuthorisationMode == null ? "AUTO" : startAuthorisationMode.getValue();
    }

    void updateAisAspspProfileBankSetting(AisAspspProfileSetting ais, @MappingTarget AisAspspProfileBankSetting aisAspspProfileBankSetting);

    void updatePisAspspProfileBankSetting(PisAspspProfileSetting pis, @MappingTarget PisAspspProfileBankSetting pisAspspProfileBankSetting);

    void updatePiisAspspProfileBankSetting(PiisAspspProfileSetting piis, @MappingTarget PiisAspspProfileBankSetting piisAspspProfileBankSetting);

    @Mapping(target = "startAuthorisationMode", source = "startAuthorisationMode", qualifiedByName = "mapStartAuthorisationMode")
    void updateCommonAspspProfileBankSetting(CommonAspspProfileSetting common, @MappingTarget CommonAspspProfileBankSetting commonAspspProfileBankSetting);
}
