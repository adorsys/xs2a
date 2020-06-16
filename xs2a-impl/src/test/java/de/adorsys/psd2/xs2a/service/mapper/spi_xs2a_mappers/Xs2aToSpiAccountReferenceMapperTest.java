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

import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class Xs2aToSpiAccountReferenceMapperTest {
    private JsonReader jsonReader;
    private Xs2aToSpiAccountReferenceMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new Xs2aToSpiAccountReferenceMapper();
        jsonReader = new JsonReader();
    }

    @Test
    void mapToSpiAccountReference() {
        AccountReference accountReference = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/account-reference.json", AccountReference.class);
        SpiAccountReference actual = mapper.mapToSpiAccountReference(accountReference);

        SpiAccountReference expected = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-reference.json", SpiAccountReference.class);
        assertEquals(expected, actual);
    }

    @Test
    void mapToSpiAccountReferences() {
        AccountReference accountReference = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/account-reference.json", AccountReference.class);
        List<SpiAccountReference> actual = mapper.mapToSpiAccountReferences(Collections.singletonList(accountReference));

        SpiAccountReference expected = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-reference.json", SpiAccountReference.class);
        assertEquals(expected, actual.get(0));
    }

    @Test
    void mapToSpiAccountReferences_nullValue() {
        List<SpiAccountReference> actual = mapper.mapToSpiAccountReferences(null);
        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    @Test
    void mapToSpiAccountReferencesOrDefault_notNull() {
        AccountReference accountReference = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/account-reference.json", AccountReference.class);

        SpiAccountReference spiAccountReference = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-reference.json", SpiAccountReference.class);
        List<SpiAccountReference> defaultList = Collections.singletonList(spiAccountReference);

        List<SpiAccountReference> actual = mapper.mapToSpiAccountReferencesOrDefault(Collections.singletonList(accountReference), defaultList);
        assertNotNull(actual);
        assertEquals(spiAccountReference, actual.get(0));
    }

    @Test
    void mapToSpiAccountReferencesOrDefault_nullValue() {
        SpiAccountReference spiAccountReference = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-reference.json", SpiAccountReference.class);
        List<SpiAccountReference> defaultList = Collections.singletonList(spiAccountReference);

        List<SpiAccountReference> actual = mapper.mapToSpiAccountReferencesOrDefault(null, defaultList);
        assertNotNull(actual);
        assertEquals(defaultList, actual);
    }
}
