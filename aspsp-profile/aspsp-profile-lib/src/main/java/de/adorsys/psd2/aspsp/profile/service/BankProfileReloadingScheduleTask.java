package de.adorsys.psd2.aspsp.profile.service;

import de.adorsys.psd2.aspsp.profile.config.ProfileConfigurations;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BankProfileReloadingScheduleTask {
    private final BankProfileReadingService bankProfileReadingService;
    private final ProfileConfigurations profileConfigurations;

    @Scheduled(cron = "${aspsp-profile-reload.cron.expression:-}")
    public void updateProfileConfiguration() {
        ProfileConfigurations newProfileConfiguration = bankProfileReadingService.getProfileConfigurations();
        profileConfigurations.updateSettings(newProfileConfiguration);
        profileConfigurations.setDefaultProperties();
    }
}
