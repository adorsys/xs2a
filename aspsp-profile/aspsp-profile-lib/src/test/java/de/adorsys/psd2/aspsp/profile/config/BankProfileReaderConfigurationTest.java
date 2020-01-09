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

package de.adorsys.psd2.aspsp.profile.config;

import de.adorsys.psd2.aspsp.profile.domain.ais.*;
import de.adorsys.psd2.aspsp.profile.domain.common.CommonAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.service.BankProfileReadingService;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.core.profile.StartAuthorisationMode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BankProfileReaderConfigurationTest {
    @Mock
    private BankProfileReadingService bankProfileReadingService;

    private BankProfileReaderConfiguration bankProfileReaderConfiguration;

    @Before
    public void setUp() {
        bankProfileReaderConfiguration = new BankProfileReaderConfiguration(bankProfileReadingService);
        ProfileConfiguration profileConfiguration = buildProfileConfiguration();
        profileConfiguration.afterPropertiesSet();
        when(bankProfileReadingService.getProfileConfiguration())
            .thenReturn(profileConfiguration);
    }

    @Test
    public void profileConfigurationDefaultScaRedirectFlow() {
        //Given
        //When
        ProfileConfiguration profileConfiguration = bankProfileReaderConfiguration.profileConfiguration();
        //Then
        assertEquals(ScaRedirectFlow.REDIRECT, profileConfiguration.getSetting().getCommon().getScaRedirectFlow());
    }

    @Test
    public void profileConfigurationDefaultBookingStatus() {
        //Given
        //When
        ProfileConfiguration profileConfiguration = bankProfileReaderConfiguration.profileConfiguration();
        //Then
        assertEquals(1, profileConfiguration.getSetting().getAis().getTransactionParameters().getAvailableBookingStatuses().size());
        assertEquals(true, profileConfiguration.getSetting().getAis().getTransactionParameters().getAvailableBookingStatuses().contains(BookingStatus.BOOKED));
    }

    @Test
    public void profileConfigurationDefaultScaApproach() {
        //Given
        //When
        ProfileConfiguration profileConfiguration = bankProfileReaderConfiguration.profileConfiguration();
        //Then
        assertEquals(1, profileConfiguration.getSetting().getCommon().getScaApproachesSupported().size());
        assertEquals(true, profileConfiguration.getSetting().getCommon().getScaApproachesSupported().contains(ScaApproach.REDIRECT));
    }

    @Test
    public void profileConfigurationDefaultStartAuthorisationMode() {
        //Given
        //When
        ProfileConfiguration profileConfiguration = bankProfileReaderConfiguration.profileConfiguration();
        //Then
        assertEquals(StartAuthorisationMode.AUTO.getValue(), profileConfiguration.getSetting().getCommon().getStartAuthorisationMode());
    }

    private ProfileConfiguration buildProfileConfiguration() {
        ProfileConfiguration profileConfiguration = new ProfileConfiguration();
        profileConfiguration.setSetting(new BankProfileSetting(buildAisAspspProfileBankSetting(), buildPisAspspProfileBankSetting(), buildPiisAspspProfileBankSetting(), buildCommonAspspProfileBankSetting()));
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

    private CommonAspspProfileBankSetting buildCommonAspspProfileBankSetting() {
        return new CommonAspspProfileBankSetting();
    }
}
