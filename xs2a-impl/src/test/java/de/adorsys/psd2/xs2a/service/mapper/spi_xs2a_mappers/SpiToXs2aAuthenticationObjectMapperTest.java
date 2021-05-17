/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAuthenticationObject;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpiToXs2aAuthenticationObjectMapperTest {
    private SpiToXs2aAuthenticationObjectMapper mapper;

    private final JsonReader reader = new JsonReader();

    @BeforeEach
    void setUp() {
        mapper = new SpiToXs2aAuthenticationObjectMapperImpl();
    }

    @Test
    void toAuthenticationObject_nullInput() {
        //When
        AuthenticationObject actual = mapper.toAuthenticationObject(null);

        //Then
        assertThat(actual).isNull();
    }

    @Test
    void toAuthenticationObject() {
        //Given
        SpiAuthenticationObject spiAuthenticationObject =
            reader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-authentication-object.json",
                SpiAuthenticationObject.class);
        AuthenticationObject expected =
            reader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-authentication-object.json",
                AuthenticationObject.class);

        //When
        AuthenticationObject actual = mapper.toAuthenticationObject(spiAuthenticationObject);

        //Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void toAuthenticationObjectList_nullInput() {
        //When
        List<AuthenticationObject> actual = mapper.toAuthenticationObjectList(null);

        //Then
        assertThat(actual).isNull();
    }

    @Test
    void toAuthenticationObjectList() {
        //Given
        List<SpiAuthenticationObject> spiAuthenticationObjects =
            reader.getListFromFile("json/service/mapper/spi_xs2a_mappers/spi-authentication-object-list.json",
                SpiAuthenticationObject.class);
        AuthenticationObject expected =
            reader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-authentication-object.json",
                AuthenticationObject.class);

        //When
        List<AuthenticationObject> actual = mapper.toAuthenticationObjectList(spiAuthenticationObjects);

        //Then
        assertThat(actual)
            .asList()
            .hasSize(1)
            .contains(expected);
    }
}
