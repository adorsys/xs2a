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
