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

package de.adorsys.psd2.xs2a.service.event.mapper;

import de.adorsys.psd2.event.service.model.PsuIdDataBO;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {EventMapperImpl.class})
public class EventMapperTest {

    @Autowired
    private EventMapper mapper;
    private JsonReader jsonReader = new JsonReader();

    @Test
    public void toEventPsuIdData() {
        PsuIdDataBO actualPsuIdDataBO = mapper.toEventPsuIdData(jsonReader.getObjectFromFile("json/service/event/psu-id-data.json", PsuIdData.class));

        PsuIdDataBO expectedPsuIdDataBO = jsonReader.getObjectFromFile("json/service/event/psu-id-data.json", PsuIdDataBO.class);
        assertEquals(expectedPsuIdDataBO, actualPsuIdDataBO);
    }

    @Test
    public void toEventPsuIdData_nullValue() {
        PsuIdDataBO actualPsuIdDataBO = mapper.toEventPsuIdData(null);
        assertNull(actualPsuIdDataBO);
    }
}
