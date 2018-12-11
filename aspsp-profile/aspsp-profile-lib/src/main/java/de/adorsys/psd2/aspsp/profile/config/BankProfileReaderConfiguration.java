/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class BankProfileReaderConfiguration implements ResourceLoaderAware {
    private static final String DEFAULT_BANK_PROFILE = "classpath:bank_profile.yml";
    private static final String FILE_PREFIX = "file:";

    @Value("${bank_profile.path}")
    private String customBankProfile;
    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Bean
    public ProfileConfiguration profileConfiguration() {
        return new Yaml(getDumperOptions()).loadAs(loadProfile(), ProfileConfiguration.class);
    }

    private DumperOptions getDumperOptions() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return options;
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
        return StringUtils.isBlank(customBankProfile)
                   ? DEFAULT_BANK_PROFILE
                   : FILE_PREFIX + customBankProfile;
    }
}
