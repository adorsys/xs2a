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
