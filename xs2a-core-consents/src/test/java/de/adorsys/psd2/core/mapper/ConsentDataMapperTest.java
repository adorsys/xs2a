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

package de.adorsys.psd2.core.mapper;

import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.core.data.piis.v1.PiisConsentData;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsentDataMapperTest {

    private ConsentDataMapper consentDataMapper;
    private JsonReader jsonReader;

    @BeforeEach
    void setUp() {
        consentDataMapper = new ConsentDataMapper();
        jsonReader = new JsonReader();
    }

    @Test
    void mapToAisConsentData() {
        AisConsentData consentData = jsonReader.getObjectFromFile("json/data/ais/ais-consent-data.json", AisConsentData.class);
        byte[] bytesFromConsentData = consentDataMapper.getBytesFromConsentData(consentData);

        assertEquals(consentData, consentDataMapper.mapToAisConsentData(bytesFromConsentData));
    }

    @Test
    void mapToPiisConsentData() {
        PiisConsentData consentData = jsonReader.getObjectFromFile("json/data/piis/piis-consent-data.json", PiisConsentData.class);
        byte[] bytesFromConsentData = consentDataMapper.getBytesFromConsentData(consentData);

        assertEquals(consentData, consentDataMapper.mapToPiisConsentData(bytesFromConsentData));
    }
}
