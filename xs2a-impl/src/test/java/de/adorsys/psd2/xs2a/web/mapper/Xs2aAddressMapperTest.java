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

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.model.Address;
import de.adorsys.psd2.xs2a.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {Xs2aAddressMapperImpl.class})
public class Xs2aAddressMapperTest {

    @Autowired
    private Xs2aAddressMapper xs2aAddressMapper;
    private JsonReader jsonReader = new JsonReader();

    @Test
    public void mapToXs2aAddress_success() {
        Xs2aAddress xs2aAddress = xs2aAddressMapper.mapToXs2aAddress(
            jsonReader.getObjectFromFile("json/service/mapper/address.json", Address.class));

        assertEquals(jsonReader.getObjectFromFile("json/service/mapper/expected-address.json", Xs2aAddress.class),
            xs2aAddress);
    }

    @Test
    public void mapToXs2aAddress_nullValue() {
        Xs2aAddress xs2aAddress = xs2aAddressMapper.mapToXs2aAddress(null);

        assertEquals(new Xs2aAddress(), xs2aAddress);
    }
}
