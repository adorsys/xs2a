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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
 class BankProfileReaderConfigurationTest {
    private static final String INSTANCE_ID = "bank1";

    @Mock
    private BankProfileReadingService bankProfileReadingService;

    private BankProfileReaderConfiguration bankProfileReaderConfiguration;

    @BeforeEach
     void setUp() {
        bankProfileReaderConfiguration = new BankProfileReaderConfiguration(bankProfileReadingService);
        ProfileConfiguration profileConfiguration = buildProfileConfiguration();
        profileConfiguration.afterPropertiesSet();
        when(bankProfileReadingService.getProfileConfigurations()).thenReturn(new ProfileConfigurations(false, profileConfiguration, Collections.emptyMap()));
    }

    @Test
     void profileConfigurationDefaultScaRedirectFlow() {
        //Given
        //When
        ProfileConfigurations profileConfiguration = bankProfileReaderConfiguration.profileConfiguration();
        //Then
        assertEquals(ScaRedirectFlow.REDIRECT, profileConfiguration.getSetting(INSTANCE_ID).getCommon().getScaRedirectFlow());
    }

    @Test
     void profileConfigurationDefaultBookingStatus() {
        //Given
        //When
        ProfileConfigurations profileConfiguration = bankProfileReaderConfiguration.profileConfiguration();
        //Then
        assertEquals(1, profileConfiguration.getSetting(INSTANCE_ID).getAis().getTransactionParameters().getAvailableBookingStatuses().size());
        assertTrue(profileConfiguration.getSetting(INSTANCE_ID).getAis().getTransactionParameters().getAvailableBookingStatuses().contains(BookingStatus.BOOKED));
    }

    @Test
     void profileConfigurationDefaultScaApproach() {
        //Given
        //When
        ProfileConfigurations profileConfiguration = bankProfileReaderConfiguration.profileConfiguration();
        //Then
        assertEquals(1, profileConfiguration.getSetting(INSTANCE_ID).getCommon().getScaApproachesSupported().size());
        assertTrue(profileConfiguration.getSetting(INSTANCE_ID).getCommon().getScaApproachesSupported().contains(ScaApproach.REDIRECT));
    }

    @Test
     void profileConfigurationDefaultStartAuthorisationMode() {
        //Given
        //When
        ProfileConfigurations profileConfiguration = bankProfileReaderConfiguration.profileConfiguration();
        //Then
        assertEquals(StartAuthorisationMode.AUTO.getValue(), profileConfiguration.getSetting(INSTANCE_ID).getCommon().getStartAuthorisationMode());
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
