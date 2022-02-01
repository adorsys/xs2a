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

package de.adorsys.psd2.aspsp.profile.service;

import de.adorsys.psd2.aspsp.profile.config.ProfileConfiguration;
import de.adorsys.psd2.aspsp.profile.config.ProfileConfigurations;
import de.adorsys.psd2.aspsp.profile.exception.AspspProfileConfigurationNotFoundException;
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
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class BankProfileReadingService implements ResourceLoaderAware {
    private static final String DEFAULT_BANK_PROFILE = "classpath:bank_profile.yml";
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String FILE_PREFIX = "file:";
    private static final String FILE_NOT_FOUND_ERROR_MESSAGE = "PSD2 api file is not found";

    @Value("${xs2a.bank_profile.path:}")
    private String customBankProfile;

    @Value("${xs2a.bank_profile.multitenancy.enabled:false}")
    private boolean multitenancyEnabled;

    @Value("#{${xs2a.bank_profile.multitenancy.customBankProfiles:{:}}}")
    private Map<String, String> customBankProfiles = new HashMap<>();

    private ResourceLoader resourceLoader;
    private Yaml yaml;

    public BankProfileReadingService() {
        this.yaml = new Yaml(createRepresenter(), createDumperOptions());
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public ProfileConfigurations getProfileConfigurations() {
        Map<String, ProfileConfiguration> instanceConfigurations = new HashMap<>();
        if (multitenancyEnabled) {
            customBankProfiles.keySet().forEach(key -> instanceConfigurations.put(
                key.toLowerCase(),
                yaml.loadAs(loadProfile(key), ProfileConfiguration.class)
            ));
        }
        ProfileConfiguration singleProfileConfiguration = yaml.loadAs(loadProfile(), ProfileConfiguration.class);
        return new ProfileConfigurations(multitenancyEnabled, singleProfileConfiguration, instanceConfigurations);
    }

    private InputStream loadProfile() {
        Resource resource = resourceLoader.getResource(resolveBankProfile());
        try {
            return resource.getInputStream();
        } catch (IOException e) {
            log.error(FILE_NOT_FOUND_ERROR_MESSAGE, e);
            throw new IllegalArgumentException(FILE_NOT_FOUND_ERROR_MESSAGE);
        }
    }

    private InputStream loadProfile(String instanceId) {
        Resource resource = resourceLoader.getResource(resolveBankProfile(instanceId));
        try {
            return resource.getInputStream();
        } catch (IOException e) {
            log.error(FILE_NOT_FOUND_ERROR_MESSAGE, e);
            throw new IllegalArgumentException("PSD2 api file for `instance-id` " + instanceId + "is not found");
        }
    }

    private String resolveBankProfile(String instanceId) {
        if (multitenancyEnabled) {
            if (!customBankProfiles.containsKey(instanceId.toLowerCase())) {
                throw new AspspProfileConfigurationNotFoundException(instanceId);
            }
            return customBankProfiles.get(instanceId);
        } else {
            return resolveBankProfile();
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
