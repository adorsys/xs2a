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

import de.adorsys.psd2.aspsp.profile.domain.migration.NewProfileConfiguration;
import de.adorsys.psd2.aspsp.profile.domain.migration.OldProfileConfiguration;
import de.adorsys.psd2.aspsp.profile.mapper.NewProfileConfigurationMapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AspspProfileConvertServiceImplTest {
    @Mock
    private NewProfileConfigurationMapper newProfileConfigurationMapper;

    @InjectMocks
    private AspspProfileConvertServiceImpl aspspProfileConvertService;

    private JsonReader jsonReader = new JsonReader();

    @Test
    public void convertProfile() {
        // Given
        OldProfileConfiguration oldProfileConfiguration = jsonReader.getObjectFromFile("json/mapper/old-profile-configuration.json", OldProfileConfiguration.class);
        NewProfileConfiguration newProfileConfiguration = jsonReader.getObjectFromFile("json/mapper/new-profile-configuration.json", NewProfileConfiguration.class);
        when(newProfileConfigurationMapper.mapToNewProfileConfiguration(oldProfileConfiguration)).thenReturn(newProfileConfiguration);

        String expectedYaml = jsonReader.getStringFromFile("json/service/new-profile.yml");

        // When
        String actual = aspspProfileConvertService.convertProfile(oldProfileConfiguration);

        // Then
        assertEquals(expectedYaml, actual);
    }
}
