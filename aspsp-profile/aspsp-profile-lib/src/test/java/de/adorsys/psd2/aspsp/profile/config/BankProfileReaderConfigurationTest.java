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

package de.adorsys.psd2.aspsp.profile.config;

import de.adorsys.psd2.aspsp.profile.domain.ais.*;
import de.adorsys.psd2.aspsp.profile.domain.common.CommonAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.sb.SbAspspProfileBankSetting;
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
