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

package de.adorsys.psd2.event.service.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.event.persist.model.EventPO;
import de.adorsys.psd2.event.service.model.EventBO;
import de.adorsys.xs2a.reader.JsonReader;
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
@ContextConfiguration(classes = {AspspEventBOMapperImpl.class, JsonConverterService.class, ObjectMapper.class})
public class EventBOMapperTest {

    private static final String PAYLOAD = "payload";

    @Autowired
    private AspspEventBOMapper mapper;

    private JsonReader jsonReader = new JsonReader();
    private ObjectMapper objectMapper = new ObjectMapper();
    private byte[] payloadAsBytes;

    @Before
    public void setUp() throws Exception {
        payloadAsBytes = objectMapper.writeValueAsBytes(PAYLOAD);
    }

    @Test
    public void toEventBO() {
        EventPO eventPO = jsonReader.getObjectFromFile("json/event-po.json", EventPO.class);
        eventPO.setPayload(payloadAsBytes);

        EventBO actualEventBO = mapper.toEventBO(eventPO);

        EventBO expectedEventBO = jsonReader.getObjectFromFile("json/event-bo.json", EventBO.class);
        assertEquals(expectedEventBO, actualEventBO);
    }

    @Test
    public void toEventBO_nullValue() {
        EventBO actualEventBO = mapper.toEventBO(null);

        assertNull(actualEventBO);
    }

    @Test
    public void toEventBOList() {
        EventPO eventPO = jsonReader.getObjectFromFile("json/event-po.json", EventPO.class);
        eventPO.setPayload(payloadAsBytes);

        List<EventBO> actualEventBOList = mapper.toEventBOList(Collections.singletonList(eventPO));

        EventBO expectedEventBO = jsonReader.getObjectFromFile("json/event-bo.json", EventBO.class);
        assertEquals(1, actualEventBOList.size());
        assertEquals(expectedEventBO, actualEventBOList.get(0));
    }

    @Test
    public void toEventBOList_nullValue() {
        List<EventBO> actualEventPOList = mapper.toEventBOList(null);
        assertNotNull(actualEventPOList);
        assertTrue(actualEventPOList.isEmpty());
    }
}
