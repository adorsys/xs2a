package de.adorsys.psd2.aspsp.profile.service;

import de.adorsys.psd2.aspsp.profile.config.ProfileConfiguration;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;
import org.springframework.core.io.DefaultResourceLoader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class BankProfileReadingServiceTest {
    private BankProfileReadingService bankProfileReadingService;

    @Before
    public void setUp(){
        bankProfileReadingService = new BankProfileReadingService();
        bankProfileReadingService.setResourceLoader(new DefaultResourceLoader());
    }

    @Test
    public void profileConfigurationWithAdditionalFields() {
        ProfileConfiguration defaultConfiguration = bankProfileReadingService.getProfileConfiguration();

        Whitebox.setInternalState(bankProfileReadingService,
                                  "customBankProfile",
                                  "classpath:bank_profile_additional_fields.yml");


        ProfileConfiguration configurationWithCustomProfile = bankProfileReadingService.getProfileConfiguration();


        assertEquals(defaultConfiguration.getSetting(), configurationWithCustomProfile.getSetting());
    }

    @Test
    public void profileConfigurationWithoutUsualFields() {
        ProfileConfiguration defaultConfiguration = bankProfileReadingService.getProfileConfiguration();

        Whitebox.setInternalState(bankProfileReadingService,
                                  "customBankProfile",
                                  "classpath:bank_profile_missing_fields.yml");

        ProfileConfiguration configurationWithCustomProfile = bankProfileReadingService.getProfileConfiguration();


        assertNotEquals(defaultConfiguration.getSetting(), configurationWithCustomProfile.getSetting());
    }

    @Test
    public void profileConfigurationScaRedirectFlowOAUTH() {
        //Given
        //When
        Whitebox.setInternalState(bankProfileReadingService,
                                  "customBankProfile",
                                  "classpath:bank_profile_sca_redirect_flow_oauth.yml");

        ProfileConfiguration configurationWithCustomProfile = bankProfileReadingService.getProfileConfiguration();
        //Then
        assertEquals(ScaRedirectFlow.OAUTH, configurationWithCustomProfile.getSetting().getCommon().getScaRedirectFlow());
    }

    @Test
    public void profileConfigurationScaRedirectFlowRedirect() {
        //Given
        //When
        Whitebox.setInternalState(bankProfileReadingService,
                                  "customBankProfile",
                                  "classpath:bank_profile_sca_redirect_flow_redirect.yml");

        ProfileConfiguration configurationWithCustomProfile = bankProfileReadingService.getProfileConfiguration();
        //Then
        assertEquals(ScaRedirectFlow.REDIRECT, configurationWithCustomProfile.getSetting().getCommon().getScaRedirectFlow());
    }
}
