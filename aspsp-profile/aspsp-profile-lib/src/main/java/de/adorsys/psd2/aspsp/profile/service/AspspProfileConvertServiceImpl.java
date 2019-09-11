/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.aspsp.profile.service;

import de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField;
import de.adorsys.psd2.aspsp.profile.domain.migration.NewProfileConfiguration;
import de.adorsys.psd2.aspsp.profile.domain.migration.OldProfileConfiguration;
import de.adorsys.psd2.aspsp.profile.mapper.NewProfileConfigurationMapper;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
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
