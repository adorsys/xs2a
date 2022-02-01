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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.CmsAddress;
import de.adorsys.psd2.consent.domain.payment.PisAddress;
import de.adorsys.psd2.core.payment.model.Address;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CmsAddressMapperImpl.class})
class CmsAddressMapperTest {

    @Autowired
    private CmsAddressMapper cmsAddressMapper;
    private JsonReader jsonReader = new JsonReader();

    @Test
    void mapToAddress() {
        //Given
        CmsAddress cmsAddress = jsonReader.getObjectFromFile("json/service/mapper/cms-address.json", CmsAddress.class);
        //When
        Address actual = cmsAddressMapper.mapToAddress(cmsAddress);
        //Then
        Address expected = jsonReader.getObjectFromFile("json/service/mapper/address.json", Address.class);
        assertEquals(expected, actual);
    }

    @Test
    void mapToCmsAddress() {
        //Given
        PisAddress pisAddress = jsonReader.getObjectFromFile("json/service/mapper/pis-address.json", PisAddress.class);
        //When
        CmsAddress actual = cmsAddressMapper.mapToCmsAddress(pisAddress);
        //Then
        CmsAddress expected = jsonReader.getObjectFromFile("json/service/mapper/cms-address.json", CmsAddress.class);
        assertEquals(expected, actual);
    }
}

