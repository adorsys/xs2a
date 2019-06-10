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

import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthenticationObject;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SpiToXs2aAuthenticationObjectMapperImpl.class})
public class SpiToXs2aAuthenticationObjectMapperTest {

    @Autowired
    private SpiToXs2aAuthenticationObjectMapper mapper;

    private JsonReader jsonReader = new JsonReader();
    private SpiAuthenticationObject spiAuthenticationObject;
    private Xs2aAuthenticationObject expectedXs2aAuthenticationObject;

    @Before
    public void setUp() {
        spiAuthenticationObject = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-authentication-object.json", SpiAuthenticationObject.class);
        expectedXs2aAuthenticationObject = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-authentication-object.json", Xs2aAuthenticationObject.class);
        expectedXs2aAuthenticationObject.setDecoupled(true);
    }

    @Test
    public void mapToXs2aAuthenticationObject() {
        Xs2aAuthenticationObject xs2aAuthenticationObject = mapper.mapToXs2aAuthenticationObject(spiAuthenticationObject);
        assertEquals(expectedXs2aAuthenticationObject, xs2aAuthenticationObject);
    }

    @Test
    public void mapToXs2aAuthenticationObject_nullValue() {
        Xs2aAuthenticationObject xs2aAuthenticationObject = mapper.mapToXs2aAuthenticationObject(null);
        assertNotNull(xs2aAuthenticationObject);
    }

    @Test
    public void mapToXs2aListAuthenticationObject() {
        List<Xs2aAuthenticationObject> xs2aAuthenticationObjectList = mapper.mapToXs2aListAuthenticationObject(Collections.singletonList(spiAuthenticationObject));

        assertEquals(1, xs2aAuthenticationObjectList.size());
        assertEquals(expectedXs2aAuthenticationObject, xs2aAuthenticationObjectList.get(0));
    }

    @Test
    public void mapToXs2aListAuthenticationObject_nullValue() {
        List<Xs2aAuthenticationObject> xs2aAuthenticationObjectList = mapper.mapToXs2aListAuthenticationObject(null);

        assertTrue(xs2aAuthenticationObjectList.isEmpty());
    }
}
