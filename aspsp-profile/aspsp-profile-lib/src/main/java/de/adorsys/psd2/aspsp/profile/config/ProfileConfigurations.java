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
