package de.adorsys.psd2.aspsp.profile.service;

import de.adorsys.psd2.aspsp.profile.config.ProfileConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
public class BankProfileReadingService implements ResourceLoaderAware {
    private static final String DEFAULT_BANK_PROFILE = "classpath:bank_profile.yml";
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String FILE_PREFIX = "file:";

    @Value("${bank_profile.path:}")
    private String customBankProfile;

    private ResourceLoader resourceLoader;
    private Yaml yaml;

    public BankProfileReadingService() {
        this.yaml = new Yaml(createRepresenter(), createDumperOptions());
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public ProfileConfiguration getProfileConfiguration() {
        return yaml.loadAs(loadProfile(), ProfileConfiguration.class);
    }

    private InputStream loadProfile() {
        Resource resource = resourceLoader.getResource(resolveBankProfile());
        try {
            return resource.getInputStream();
        } catch (IOException e) {
            log.error("PSD2 api file is not found", e);
            throw new IllegalArgumentException("PSD2 api file is not found");
        }
    }

    private String resolveBankProfile() {
        if (StringUtils.isBlank(customBankProfile)) {
            return DEFAULT_BANK_PROFILE;
        } else {
            if (customBankProfile.startsWith(CLASSPATH_PREFIX)
                    || customBankProfile.startsWith(FILE_PREFIX)) {
                return customBankProfile;
            }
            return FILE_PREFIX + customBankProfile;
        }
    }

    private Representer createRepresenter() {
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        return representer;
    }

    private DumperOptions createDumperOptions() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return options;
    }
}
