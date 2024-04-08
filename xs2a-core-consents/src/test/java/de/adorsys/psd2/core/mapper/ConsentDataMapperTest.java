/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.core.mapper;

import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.core.data.piis.PiisConsentData;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    void mapToAisConsentData_nullData() {
        AisConsentData expected = new AisConsentData(null, null, null, false);

        assertEquals(expected, consentDataMapper.mapToAisConsentData(null));
    }

    @Test
    void mapToAisConsentData_exception() {

        assertNull(consentDataMapper.mapToAisConsentData("".getBytes()));
    }

    @Test
    void mapToPiisConsentData() {
        PiisConsentData consentData = jsonReader.getObjectFromFile("json/data/piis/piis-consent-data.json", PiisConsentData.class);
        byte[] bytesFromConsentData = consentDataMapper.getBytesFromConsentData(consentData);

        assertEquals(consentData, consentDataMapper.mapToPiisConsentData(bytesFromConsentData));
    }

    @Test
    void mapToPiisConsentData_nullData() {
        PiisConsentData expected = new PiisConsentData(null, null, null, null);

        assertEquals(expected, consentDataMapper.mapToPiisConsentData(null));
    }

    @Test
    void mapToPiisConsentData_exception() {

        assertNull(consentDataMapper.mapToPiisConsentData("".getBytes()));
    }
}
