package de.adorsys.psd2.aspsp.profile.service;

import de.adorsys.psd2.aspsp.profile.config.ProfileConfiguration;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

 class BankProfileReadingServiceTest {
    private BankProfileReadingService bankProfileReadingService;

    @BeforeEach
     void setUp(){
        bankProfileReadingService = new BankProfileReadingService();
        bankProfileReadingService.setResourceLoader(new DefaultResourceLoader());
    }

    @Test
     void profileConfigurationWithAdditionalFields() {
        ProfileConfiguration defaultConfiguration = bankProfileReadingService.getProfileConfiguration();

        ReflectionTestUtils.setField(bankProfileReadingService,
                                      "customBankProfile",
                                      "classpath:bank_profile_additional_fields.yml");


        ProfileConfiguration configurationWithCustomProfile = bankProfileReadingService.getProfileConfiguration();


        assertEquals(defaultConfiguration.getSetting(), configurationWithCustomProfile.getSetting());
    }

    @Test
     void profileConfigurationWithoutUsualFields() {
        ProfileConfiguration defaultConfiguration = bankProfileReadingService.getProfileConfiguration();

        ReflectionTestUtils.setField(bankProfileReadingService,
                                  "customBankProfile",
                                  "classpath:bank_profile_missing_fields.yml");

        ProfileConfiguration configurationWithCustomProfile = bankProfileReadingService.getProfileConfiguration();


        assertNotEquals(defaultConfiguration.getSetting(), configurationWithCustomProfile.getSetting());
    }

    @Test
     void profileConfigurationScaRedirectFlowOAUTH() {
        //Given
        //When
        ReflectionTestUtils.setField(bankProfileReadingService,
                                  "customBankProfile",
                                  "classpath:bank_profile_sca_redirect_flow_oauth.yml");

        ProfileConfiguration configurationWithCustomProfile = bankProfileReadingService.getProfileConfiguration();
        //Then
        assertEquals(ScaRedirectFlow.OAUTH, configurationWithCustomProfile.getSetting().getCommon().getScaRedirectFlow());
    }

    @Test
     void profileConfigurationScaRedirectFlowRedirect() {
        //Given
        //When
        ReflectionTestUtils.setField(bankProfileReadingService,
                                  "customBankProfile",
                                  "classpath:bank_profile_sca_redirect_flow_redirect.yml");

        ProfileConfiguration configurationWithCustomProfile = bankProfileReadingService.getProfileConfiguration();
        //Then
        assertEquals(ScaRedirectFlow.REDIRECT, configurationWithCustomProfile.getSetting().getCommon().getScaRedirectFlow());
    }
}
