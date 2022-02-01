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

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Xs2aToSpiAccountAccessMapper.class, Xs2aToSpiAccountReferenceMapper.class})
class Xs2aToSpiAccountAccessMapperTest {

    @Autowired
    private Xs2aToSpiAccountAccessMapper mapper;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void mapToAccountAccess() {
        //Given
        AisConsent consent = jsonReader
            .getObjectFromFile("json/service/mapper/spi_xs2a_mappers/ais-consent-additional-info-filled.json", AisConsent.class);
        SpiAccountAccess expected = jsonReader
            .getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-access-expected.json", SpiAccountAccess.class);

        //When
        SpiAccountAccess actual = mapper.mapToAccountAccess(consent);

        //Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void mapToAccountAccess_additionalInfoNull() {
        //Given
        AisConsent consent = jsonReader
            .getObjectFromFile("json/service/mapper/spi_xs2a_mappers/ais-consent-additional-info-null.json", AisConsent.class);
        SpiAccountAccess expected = jsonReader
            .getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-access-null-additional-info-expected.json", SpiAccountAccess.class);

        //When
        SpiAccountAccess actual = mapper.mapToAccountAccess(consent);

        //Then
        assertThat(actual).isEqualTo(expected);
    }
}
