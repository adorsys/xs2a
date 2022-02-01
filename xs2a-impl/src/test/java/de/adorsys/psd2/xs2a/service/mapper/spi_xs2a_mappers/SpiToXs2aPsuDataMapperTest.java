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

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpiToXs2aPsuDataMapperTest {
    private SpiToXs2aPsuDataMapper mapper;

    private final JsonReader reader = new JsonReader();

    @BeforeEach
    void setUp() {
        mapper = new SpiToXs2aPsuDataMapper();
    }

    @Test
    void mapToPsuIdData() {
        //Given
        PsuIdData expected =
            reader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/psu-id-data.json", PsuIdData.class);

        //When
        PsuIdData actual = mapper.mapToPsuIdData(getTestSpiPsuData());

        //Then
        assertThat(actual).isEqualTo(expected);
    }

    private SpiPsuData getTestSpiPsuData() {
        return SpiPsuData.builder()
            .psuId("psu Id")
            .psuIdType("psuId Type")
            .psuCorporateId("psu Corporate Id")
            .psuCorporateIdType("psuCorporate Id Type")
            .psuIpAddress("psu IP address")
            .build();
    }
}
