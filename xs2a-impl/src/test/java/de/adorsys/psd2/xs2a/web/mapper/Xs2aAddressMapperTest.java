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
