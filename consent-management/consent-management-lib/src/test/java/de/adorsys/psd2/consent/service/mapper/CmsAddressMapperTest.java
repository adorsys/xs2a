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

