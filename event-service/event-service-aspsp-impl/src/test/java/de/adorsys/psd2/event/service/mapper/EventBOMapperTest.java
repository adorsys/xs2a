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
import de.adorsys.psd2.event.persist.model.ReportEvent;
import de.adorsys.psd2.event.service.model.AspspEvent;
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
@ContextConfiguration(classes = {AspspEventMapperImpl.class, JsonConverterService.class, ObjectMapper.class})
public class EventBOMapperTest {

    private static final String PAYLOAD = "payload";

    @Autowired
    private AspspEventMapper mapper;

    private JsonReader jsonReader = new JsonReader();
    private ObjectMapper objectMapper = new ObjectMapper();
    private byte[] payloadAsBytes;

    @Before
    public void setUp() throws Exception {
        payloadAsBytes = objectMapper.writeValueAsBytes(PAYLOAD);
    }

    @Test
    public void toAspspEvent() {
        ReportEvent reportEvent = jsonReader.getObjectFromFile("json/aspsp-event-po.json", ReportEvent.class);
        reportEvent.setPayload(payloadAsBytes);

        AspspEvent actualAspspEvent = mapper.toAspspEvent(reportEvent);

        AspspEvent expectedEventBO = jsonReader.getObjectFromFile("json/aspsp-event-bo.json", AspspEvent.class);
        assertEquals(expectedEventBO, actualAspspEvent);
    }

    @Test
    public void toAspspEvent_nullValue() {
        AspspEvent actualAspspEvent = mapper.toAspspEvent(null);

        assertNull(actualAspspEvent);
    }

    @Test
    public void toAspspEventList() {
        ReportEvent reportEvent = jsonReader.getObjectFromFile("json/aspsp-event-po.json", ReportEvent.class);
        reportEvent.setPayload(payloadAsBytes);

        List<AspspEvent> actualAspspEventList = mapper.toAspspEventList(Collections.singletonList(reportEvent));

        AspspEvent expectedAspspEvent = jsonReader.getObjectFromFile("json/aspsp-event-bo.json", AspspEvent.class);
        assertEquals(1, actualAspspEventList.size());
        assertEquals(expectedAspspEvent, actualAspspEventList.get(0));
    }

    @Test
    public void toAspspEventBOList_nullValue() {
        List<AspspEvent> actualAspspEventList = mapper.toAspspEventList(null);
        assertNotNull(actualAspspEventList);
        assertTrue(actualAspspEventList.isEmpty());
    }
}
