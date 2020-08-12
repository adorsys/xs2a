/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.spi.domain.piis.SpiPiisConsent;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Xs2aToSpiPiisConsentMapperImpl.class, Xs2aToSpiPsuDataMapper.class})
class Xs2aToSpiPiisConsentMapperTest {
    private JsonReader jsonReader = new JsonReader();

    @Autowired
    private Xs2aToSpiPiisConsentMapper xs2aToSpiPiisConsentMapper;

    @Test
    void mapToSpiPiisConsent() {
        PiisConsent xs2aConsent =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/piis/piis-consent.json", PiisConsent.class);
        SpiPiisConsent expectedSpiConsent =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/piis/spi-piis-consent.json", SpiPiisConsent.class);

        SpiPiisConsent result = xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(xs2aConsent);

        assertEquals(expectedSpiConsent, result);
    }

    @Test
    void mapToSpiPiisConsent_null() {
        SpiPiisConsent result = xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(null);

        assertNull(result);
    }
}
