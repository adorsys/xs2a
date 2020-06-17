/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import de.adorsys.psd2.aspsp.profile.exception.AspspProfileConfigurationNotFoundException;
import de.adorsys.psd2.aspsp.profile.exception.InstanceIdIsMandatoryHeaderException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;

@Data
@AllArgsConstructor
public class ProfileConfigurations implements InitializingBean {
    private boolean multitenancyEnabled;
    private ProfileConfiguration singleConfiguration;
    private Map<String, ProfileConfiguration> instanceConfigurations;

    @Override
    public void afterPropertiesSet() {
        setDefaultProperties();
    }

    public void setDefaultProperties() {
        singleConfiguration.setDefaultProperties();
        instanceConfigurations.values().forEach(ProfileConfiguration::setDefaultProperties);
    }

    public BankProfileSetting getSetting(String instanceId) {
        if (multitenancyEnabled) {
            if (StringUtils.isBlank(instanceId)) {
                throw new InstanceIdIsMandatoryHeaderException();
            }
            if (!instanceConfigurations.containsKey(instanceId.toLowerCase())) {
                throw new AspspProfileConfigurationNotFoundException(instanceId);
            }
            return instanceConfigurations.get(instanceId.toLowerCase()).getSetting();
        }
        return singleConfiguration.getSetting();
    }

    public void updateSettings(ProfileConfigurations newProfileConfiguration) {
        singleConfiguration.setSetting(newProfileConfiguration.getSingleConfiguration().getSetting());
        instanceConfigurations.clear();
        instanceConfigurations.putAll(newProfileConfiguration.getInstanceConfigurations());
    }
}
