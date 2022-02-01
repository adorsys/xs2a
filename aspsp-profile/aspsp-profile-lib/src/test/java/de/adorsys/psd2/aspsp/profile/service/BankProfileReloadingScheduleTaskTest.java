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
import de.adorsys.psd2.aspsp.profile.config.ProfileConfiguration;
import de.adorsys.psd2.aspsp.profile.config.ProfileConfigurations;
import de.adorsys.psd2.aspsp.profile.domain.ais.*;
import de.adorsys.psd2.aspsp.profile.domain.common.CommonAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.sb.SbAspspProfileBankSetting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankProfileReloadingScheduleTaskTest {
    @Mock
    private BankProfileReadingService bankProfileReadingService;
    @Mock
    private ProfileConfigurations profileConfigurations;

    @InjectMocks
    private BankProfileReloadingScheduleTask bankProfileReloadingScheduleTask;

    @BeforeEach
    void setUp() {
        ProfileConfiguration singleConfiguration = buildNewProfileConfiguration();
        when(bankProfileReadingService.getProfileConfigurations())
            .thenReturn(new ProfileConfigurations(false, singleConfiguration, Collections.singletonMap("bank1", singleConfiguration)));
    }

    @Test
    void updateProfileConfiguration() {
        bankProfileReloadingScheduleTask.updateProfileConfiguration();

        ProfileConfiguration newProfileConfiguration = buildNewProfileConfiguration();

        ArgumentCaptor<ProfileConfigurations> profileConfigurationsArgumentCaptor = ArgumentCaptor.forClass(ProfileConfigurations.class);
        verify(profileConfigurations, times(1)).updateSettings(profileConfigurationsArgumentCaptor.capture());
        verify(profileConfigurations, times(1)).setDefaultProperties();

        assertEquals(bankProfileReadingService.getProfileConfigurations().getSingleConfiguration(), newProfileConfiguration);
        assertEquals(profileConfigurationsArgumentCaptor.getValue().getSingleConfiguration().getSetting(), newProfileConfiguration.getSetting());
    }

    private ProfileConfiguration buildNewProfileConfiguration() {
        ProfileConfiguration profileConfiguration = new ProfileConfiguration();
        profileConfiguration.setSetting(new BankProfileSetting(buildAisAspspProfileBankSetting(), buildPisAspspProfileBankSetting(), buildPiisAspspProfileBankSetting(), buildSbAspspProfileBankSetting(), buildCommonAspspProfileBankSetting()));
        return profileConfiguration;
    }

    private AisAspspProfileBankSetting buildAisAspspProfileBankSetting() {
        return new AisAspspProfileBankSetting(new ConsentTypeBankSetting(), new AisRedirectLinkBankSetting(), new AisTransactionBankSetting(), new DeltaReportBankSetting(), new OneTimeConsentScaBankSetting());
    }

    private PisAspspProfileBankSetting buildPisAspspProfileBankSetting() {
        return new PisAspspProfileBankSetting();
    }

    private PiisAspspProfileBankSetting buildPiisAspspProfileBankSetting() {
        return new PiisAspspProfileBankSetting();
    }

    private SbAspspProfileBankSetting buildSbAspspProfileBankSetting() {
        return new SbAspspProfileBankSetting();
    }

    private CommonAspspProfileBankSetting buildCommonAspspProfileBankSetting() {
        return new CommonAspspProfileBankSetting();
    }
}
