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

import de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField;
import de.adorsys.psd2.aspsp.profile.domain.migration.NewProfileConfiguration;
import de.adorsys.psd2.aspsp.profile.domain.migration.OldProfileConfiguration;
import de.adorsys.psd2.aspsp.profile.mapper.NewProfileConfigurationMapper;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

@Service
@AllArgsConstructor
public class AspspProfileConvertServiceImpl implements AspspProfileConvertService {
    private NewProfileConfigurationMapper newProfileConfigurationMapper;

    @Override
    public String convertProfile(OldProfileConfiguration profile) {
        NewProfileConfiguration newProfileConfiguration = newProfileConfigurationMapper.mapToNewProfileConfiguration(profile);
        Yaml yaml = new Yaml(getYamlRepresenter(), getYamlDumperOptions());
        return yaml.dumpAsMap(newProfileConfiguration);
    }

    @NotNull
    private Representer getYamlRepresenter() {
        Representer representer = new Representer();
        representer.addClassTag(BookingStatus.class, Tag.STR);
        representer.addClassTag(ScaApproach.class, Tag.STR);
        representer.addClassTag(SupportedAccountReferenceField.class, Tag.STR);
        representer.addClassTag(NotificationSupportedMode.class, Tag.STR);
        return representer;
    }

    @NotNull
    private DumperOptions getYamlDumperOptions() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setExplicitStart(true);
        return options;
    }
}
