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
