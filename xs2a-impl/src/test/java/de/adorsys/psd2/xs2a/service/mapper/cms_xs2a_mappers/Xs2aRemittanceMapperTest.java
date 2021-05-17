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

package de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers;

import de.adorsys.psd2.consent.api.pis.CmsRemittance;
import de.adorsys.psd2.xs2a.core.pis.Remittance;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Xs2aRemittanceMapperTest {
    private Xs2aRemittanceMapper mapper;

    private final JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        mapper = new Xs2aRemittanceMapperImpl();
    }

    @Test
    void mapToCmsRemittance_nullInput() {
        //When
        CmsRemittance actual = mapper.mapToCmsRemittance(null);

        //Then
        assertThat(actual).isNull();
    }

    @Test
    void mapToCmsRemittance() {
        //Given
        Remittance remittance =
            jsonReader.getObjectFromFile("json/service/mapper/remittance.json", Remittance.class);
        CmsRemittance expected =
            jsonReader.getObjectFromFile("json/service/mapper/remittance.json", CmsRemittance.class);

        //When
        CmsRemittance actual = mapper.mapToCmsRemittance(remittance);

        //Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void mapToRemittance_nullInput() {
        //When
        Remittance actual = mapper.mapToRemittance(null);

        //Then
        assertThat(actual).isNull();
    }

    @Test
    void mapToRemittance() {
        //Given
        CmsRemittance cmsRemittance =
            jsonReader.getObjectFromFile("json/service/mapper/remittance.json", CmsRemittance.class);
        Remittance expected =
            jsonReader.getObjectFromFile("json/service/mapper/remittance.json", Remittance.class);

        //When
        Remittance actual = mapper.mapToRemittance(cmsRemittance);

        //Then
        assertThat(actual).isEqualTo(expected);
    }
}
