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

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.model.Address;
import de.adorsys.psd2.xs2a.core.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.core.domain.address.Xs2aCountryCode;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Xs2aAddressMapperImpl.class})
class Xs2aAddressMapperTest {

    @Autowired
    private Xs2aAddressMapper xs2aAddressMapper;
    private final JsonReader jsonReader = new JsonReader();

    @Test
    void mapToXs2aAddress_success() {
        Xs2aAddress xs2aAddress = xs2aAddressMapper.mapToXs2aAddress(
            jsonReader.getObjectFromFile("json/service/mapper/address.json", Address.class));

        assertEquals(jsonReader.getObjectFromFile("json/service/mapper/expected-address.json", Xs2aAddress.class),
            xs2aAddress);
    }

    @Test
    void mapToXs2aAddress_nullValue() {
        Xs2aAddress xs2aAddress = xs2aAddressMapper.mapToXs2aAddress(null);
        assertNull(xs2aAddress);
    }

    @Test
    void mapToAddress_success() {
        Address address = xs2aAddressMapper.mapToAddress(
            jsonReader.getObjectFromFile("json/service/mapper/expected-address.json", Xs2aAddress.class));

        assertEquals(jsonReader.getObjectFromFile("json/service/mapper/address.json", Address.class),
                     address);
    }

    @Test
    void mapToAddress_nullValue() {
        Address address = xs2aAddressMapper.mapToAddress(null);
        assertNull(address);
    }

    @Test
    void mapToAdress_Xs2aCountryCodeIsNull() {
        //Given
        Xs2aAddress xs2aAddress = jsonReader.getObjectFromFile("json/service/mapper/expected-address.json", Xs2aAddress.class);
        xs2aAddress.setCountry(null);
        //When
        Address address = xs2aAddressMapper.mapToAddress(xs2aAddress);
        //Then
        assertNull(address.getCountry());
    }

    @Test
    void mapToAdress_Xs2aCountryCodeCodeIsNull() {
        //Given
        Xs2aAddress xs2aAddress = jsonReader.getObjectFromFile("json/service/mapper/expected-address.json", Xs2aAddress.class);
        xs2aAddress.setCountry(new Xs2aCountryCode(null));
        //When
        Address address = xs2aAddressMapper.mapToAddress(xs2aAddress);
        //Then
        assertNull(address.getCountry());
    }
}
