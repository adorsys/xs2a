/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.core.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiAddress;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SpiToXs2aAddressMapperImpl.class})
public class SpiToXs2aAddressMapperTest {

    @Autowired
    private SpiToXs2aAddressMapper mapper;

    private JsonReader jsonReader = new JsonReader();

    @Test
    public void mapToAddress() {
        Xs2aAddress xs2aAddress = mapper.mapToAddress(
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-address.json", SpiAddress.class));

        Xs2aAddress expectedAddress = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-address.json", Xs2aAddress.class);
        assertEquals(expectedAddress, xs2aAddress);
    }

    @Test
    public void mapToAddress_nullValue() {
        Xs2aAddress xs2aAddress = mapper.mapToAddress(null);
        assertNull(xs2aAddress);
    }
}
