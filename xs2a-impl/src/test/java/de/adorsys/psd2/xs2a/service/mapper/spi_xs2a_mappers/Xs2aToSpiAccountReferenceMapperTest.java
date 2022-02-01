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
