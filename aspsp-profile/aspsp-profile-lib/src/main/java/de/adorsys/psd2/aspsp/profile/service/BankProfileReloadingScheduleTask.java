package de.adorsys.psd2.aspsp.profile.service;

    import de.adorsys.psd2.aspsp.profile.config.ProfileConfiguration;
    import lombok.RequiredArgsConstructor;
    import org.springframework.scheduling.annotation.Scheduled;
    import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BankProfileReloadingScheduleTask {
    private final BankProfileReadingService bankProfileReadingService;
    private final ProfileConfiguration profileConfiguration;

    @Scheduled(cron = "${aspsp-profile-reload.cron.expression:-}")
    public void updateProfileConfiguration() {
        ProfileConfiguration newProfileConfiguration = bankProfileReadingService.getProfileConfiguration();
        profileConfiguration.setSetting(newProfileConfiguration.getSetting());
        profileConfiguration.setDefaultProperties();
    }
}
