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

import de.adorsys.psd2.aspsp.profile.domain.migration.NewProfileConfiguration;
import de.adorsys.psd2.aspsp.profile.domain.migration.OldProfileConfiguration;
import de.adorsys.psd2.aspsp.profile.mapper.NewProfileConfigurationMapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
 class AspspProfileConvertServiceImplTest {
    @Mock
    private NewProfileConfigurationMapper newProfileConfigurationMapper;

    @InjectMocks
    private AspspProfileConvertServiceImpl aspspProfileConvertService;

    private final JsonReader jsonReader = new JsonReader();

    @Test
     void convertProfile() {
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
