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
@ContextConfiguration(classes = {SpiToXs2aAddressMapperImpl.class})
class SpiToXs2aAddressMapperTest {

    @Autowired
    private SpiToXs2aAddressMapper mapper;

    private JsonReader jsonReader = new JsonReader();

    @Test
    void mapToAddress() {
        Xs2aAddress xs2aAddress = mapper.mapToAddress(
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-address.json", SpiAddress.class));

        Xs2aAddress expectedAddress = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-address.json", Xs2aAddress.class);
        assertEquals(expectedAddress, xs2aAddress);
    }

    @Test
    void mapToAddress_nullValue() {
        Xs2aAddress xs2aAddress = mapper.mapToAddress(null);
        assertNull(xs2aAddress);
    }
}
