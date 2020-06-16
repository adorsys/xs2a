package de.adorsys.psd2.aspsp.profile.service;

import de.adorsys.psd2.aspsp.profile.config.ProfileConfigurations;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BankProfileReadingServiceTest {
    private static final String INSTANCE_ID = "bank1";

    private BankProfileReadingService bankProfileReadingService;

    @BeforeEach
    void setUp() {
        bankProfileReadingService = new BankProfileReadingService();
        bankProfileReadingService.setResourceLoader(new DefaultResourceLoader());
    }

    @Test
    void profileConfigurationWithAdditionalFields() {
        ProfileConfigurations defaultConfigurations = bankProfileReadingService.getProfileConfigurations();

        ReflectionTestUtils.setField(bankProfileReadingService,
                                     "customBankProfile",
                                     "classpath:bank_profile_additional_fields.yml");


        ProfileConfigurations configurationWithCustomProfile = bankProfileReadingService.getProfileConfigurations();


        assertEquals(defaultConfigurations.getSetting(INSTANCE_ID), configurationWithCustomProfile.getSetting(INSTANCE_ID));
    }

    @Test
    void profileConfigurationWithoutUsualFields() {
        ProfileConfigurations defaultConfiguration = bankProfileReadingService.getProfileConfigurations();

        ReflectionTestUtils.setField(bankProfileReadingService,
                                     "customBankProfile",
                                     "classpath:bank_profile_missing_fields.yml");

        ProfileConfigurations configurationWithCustomProfile = bankProfileReadingService.getProfileConfigurations();


        assertNotEquals(defaultConfiguration.getSetting(INSTANCE_ID), configurationWithCustomProfile.getSetting(INSTANCE_ID));
    }

    @Test
    void profileConfigurationScaRedirectFlowOAUTH() {
        //Given
        //When
        ReflectionTestUtils.setField(bankProfileReadingService,
                                     "customBankProfile",
                                     "classpath:bank_profile_sca_redirect_flow_oauth.yml");

        ProfileConfigurations configurationWithCustomProfile = bankProfileReadingService.getProfileConfigurations();
        //Then
        assertEquals(ScaRedirectFlow.OAUTH, configurationWithCustomProfile.getSetting(INSTANCE_ID).getCommon().getScaRedirectFlow());
    }

    @Test
    void profileConfigurationScaRedirectFlowRedirect() {
        //Given
        //When
        ReflectionTestUtils.setField(bankProfileReadingService,
                                     "customBankProfile",
                                     "classpath:bank_profile_sca_redirect_flow_redirect.yml");

        ProfileConfigurations configurationWithCustomProfile = bankProfileReadingService.getProfileConfigurations();
        //Then
        assertEquals(ScaRedirectFlow.REDIRECT, configurationWithCustomProfile.getSetting(INSTANCE_ID).getCommon().getScaRedirectFlow());
    }

    @Test
    void profileConfigurationMultitenant() {
        //Given
        ReflectionTestUtils.setField(bankProfileReadingService,
                                     "multitenancyEnabled",
                                     true);
        Map<String, String> customBankProfiles = new HashMap<>();
        customBankProfiles.put("bank1", "classpath:bank_profile_additional_fields.yml");
        customBankProfiles.put("bank2", "classpath:bank_profile_additional_fields.yml");
        ReflectionTestUtils.setField(bankProfileReadingService,
                                     "customBankProfiles",
                                     customBankProfiles);

        //When
        ProfileConfigurations profileConfigurations = bankProfileReadingService.getProfileConfigurations();

        //Then
        assertNotNull(profileConfigurations);
        assertTrue(profileConfigurations.isMultitenancyEnabled());
        assertEquals(2, profileConfigurations.getInstanceConfigurations().size());
        assertTrue(profileConfigurations.getInstanceConfigurations().containsKey("bank1"));
        assertTrue(profileConfigurations.getInstanceConfigurations().containsKey("bank2"));
    }
}
