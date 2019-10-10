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

import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SpiToXs2aAccountReferenceMapperImpl.class})
public class SpiToXs2aAccountReferenceMapperTest {

    @Autowired
    private SpiToXs2aAccountReferenceMapper mapper;

    private JsonReader jsonReader = new JsonReader();
    private SpiAccountReference spiAccountReference;
    private AccountReference expectedAccountReference;

    @Before
    public void setUp() {
        spiAccountReference = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-reference.json", SpiAccountReference.class);
        expectedAccountReference = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-reference.json", AccountReference.class);
    }

    @Test
    public void mapToXs2aAccountReference() {
        AccountReference accountReference = mapper.mapToXs2aAccountReference(spiAccountReference);
        assertEquals(expectedAccountReference, accountReference);
    }

    @Test
    public void mapToXs2aAccountReference_nullValue() {
        assertNull(mapper.mapToXs2aAccountReference(null));
    }

    @Test
    public void mapToXs2aAccountReferenceList() {
        List<AccountReference> accountReference = mapper.mapToXs2aAccountReferences(Collections.singletonList(spiAccountReference));
        assertEquals(expectedAccountReference, accountReference.get(0));
    }
}
