package de.adorsys.psd2.aspsp.profile.service;

import de.adorsys.psd2.aspsp.profile.config.BankProfileSetting;
import de.adorsys.psd2.aspsp.profile.config.ProfileConfiguration;
import de.adorsys.psd2.aspsp.profile.domain.ais.*;
import de.adorsys.psd2.aspsp.profile.domain.common.CommonAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisAspspProfileBankSetting;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BankProfileReloadingScheduleTaskTest {
    @Mock
    private BankProfileReadingService bankProfileReadingService;
    @Mock
    private ProfileConfiguration profileConfiguration;

    @InjectMocks
    private BankProfileReloadingScheduleTask bankProfileReloadingScheduleTask;

    @Before
    public void setUp() {
        when(bankProfileReadingService.getProfileConfiguration())
            .thenReturn(buildNewProfileConfiguration());
    }

    @Test
    public void updateProfileConfiguration() {
        bankProfileReloadingScheduleTask.updateProfileConfiguration();

        ProfileConfiguration newProfileConfiguration = buildNewProfileConfiguration();

        ArgumentCaptor<BankProfileSetting> bankProfileSettingArgumentCaptor = ArgumentCaptor.forClass(BankProfileSetting.class);
        verify(profileConfiguration, times(1)).setSetting(bankProfileSettingArgumentCaptor.capture());
        verify(profileConfiguration, times(1)).setDefaultProperties();

        assertEquals(bankProfileReadingService.getProfileConfiguration(), newProfileConfiguration);
        assertEquals(bankProfileSettingArgumentCaptor.getValue(), newProfileConfiguration.getSetting());
    }

    private ProfileConfiguration buildNewProfileConfiguration() {
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
