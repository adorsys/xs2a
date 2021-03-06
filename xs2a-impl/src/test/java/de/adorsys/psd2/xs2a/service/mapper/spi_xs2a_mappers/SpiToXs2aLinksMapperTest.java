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
