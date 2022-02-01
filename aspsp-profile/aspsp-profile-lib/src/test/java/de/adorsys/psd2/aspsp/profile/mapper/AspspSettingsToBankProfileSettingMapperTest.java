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
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AspspSettingsToBankProfileSettingMapperImpl.class})
 class AspspSettingsToBankProfileSettingMapperTest {

    @Autowired
    private AspspSettingsToBankProfileSettingMapper mapper;

    private JsonReader jsonReader = new JsonReader();
    private BankProfileSetting initialBankProfileSetting;
    private BankProfileSetting expectedBankProfileSetting;
    private AspspSettings aspspSettings;

    @BeforeEach
     void setUp() {
        initialBankProfileSetting = jsonReader.getObjectFromFile("json/mapper/initial-bank-profile-setting.json", BankProfileSetting.class);
        expectedBankProfileSetting = jsonReader.getObjectFromFile("json/mapper/expected-bank-profile-setting.json", BankProfileSetting.class);
        aspspSettings = jsonReader.getObjectFromFile("json/mapper/aspsp-settings.json", AspspSettings.class);
    }

    @Test
     void updateBankProfileSetting_success() {
        mapper.updateBankProfileSetting(aspspSettings, initialBankProfileSetting);
        assertEquals(expectedBankProfileSetting, initialBankProfileSetting);
    }
}
