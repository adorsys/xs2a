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

import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiLinks;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpiToXs2aLinksMapperImpl.class})
class SpiToXs2aLinksMapperTest {

    @Autowired
    private SpiToXs2aLinksMapperImpl mapper;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void toXs2aLinks() {
        // Given
        SpiLinks spiLinks = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-links.json", SpiLinks.class);

        // When
        Links actual = mapper.toXs2aLinks(spiLinks);
        Links expected = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-links.json", Links.class);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void toXs2aLinks_null() {
        // When
        Links actual = mapper.toXs2aLinks(null);

        // Then
        assertThat(actual).isNull();
    }

    @Test
    void toXs2aHrefType_null() {
        //When
        HrefType actual = mapper.toXs2aHrefType(null);

        // Then
        assertThat(actual).isNull();
    }
}
