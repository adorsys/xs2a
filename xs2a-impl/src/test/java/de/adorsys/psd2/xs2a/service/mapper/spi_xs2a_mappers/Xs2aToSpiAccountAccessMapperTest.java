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
