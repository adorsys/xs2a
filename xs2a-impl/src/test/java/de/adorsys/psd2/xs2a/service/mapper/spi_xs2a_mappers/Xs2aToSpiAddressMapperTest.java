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

import de.adorsys.psd2.xs2a.core.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiAddress;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Xs2aToSpiAddressMapper.class})
class Xs2aToSpiAddressMapperTest {
    @Autowired
    private Xs2aToSpiAddressMapper xs2aToSpiAddressMapper;
    private JsonReader jsonReader = new JsonReader();

    @Test
    void mapToSpiAddress() {
        //Given
        Xs2aAddress xs2aAddress = jsonReader.getObjectFromFile("json/Xs2aAddress.json", Xs2aAddress.class);
        SpiAddress expectedSpiAddress = jsonReader.getObjectFromFile("json/SpiAddress.json", SpiAddress.class);
        //When
        SpiAddress actualSpiAddress = xs2aToSpiAddressMapper.mapToSpiAddress(xs2aAddress);
        //Then
        assertEquals(expectedSpiAddress, actualSpiAddress);
    }

    @Test
    void mapToSpiAddress_xs2aAddressIsNull() {
        //Given
        Xs2aAddress xs2aAddress = null;
        //When
        SpiAddress actualSpiAddress = xs2aToSpiAddressMapper.mapToSpiAddress(xs2aAddress);
        //Then
        assertNull(actualSpiAddress);
    }

    @Test
    void mapToXs2aAddress() {
        //Given
        SpiAddress spiAddress = jsonReader.getObjectFromFile("json/SpiAddress.json", SpiAddress.class);
        Xs2aAddress expectedXs2aAddress = jsonReader.getObjectFromFile("json/Xs2aAddress.json", Xs2aAddress.class);
        //When
        Xs2aAddress actualXs2aAddress = xs2aToSpiAddressMapper.mapToXs2aAddress(spiAddress);

        //Then
        assertEquals(expectedXs2aAddress, actualXs2aAddress);
    }

    @Test
    void mapToXs2aAddress_spiAddressIsNull() {
        //Given
        SpiAddress spiAddress = null;
        //When
        Xs2aAddress actualXs2aAddress = xs2aToSpiAddressMapper.mapToXs2aAddress(spiAddress);
        //Then
        assertNull(actualXs2aAddress);
    }
}
