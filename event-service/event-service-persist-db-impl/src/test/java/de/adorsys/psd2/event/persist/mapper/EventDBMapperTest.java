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

package de.adorsys.psd2.event.persist.mapper;

import de.adorsys.psd2.event.persist.entity.EventEntity;
import de.adorsys.psd2.event.persist.model.EventPO;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {EventDBMapperImpl.class})
class EventDBMapperTest {
    private static final byte[] PAYLOAD = "payload".getBytes();

    @Autowired
    private EventDBMapper mapper;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void toEventEntity() {
        EventPO eventPO = jsonReader.getObjectFromFile("json/event.json", EventPO.class);
        eventPO.setPayload(PAYLOAD);

        EventEntity actualEventEntity = mapper.toEventEntity(eventPO);

        EventEntity expectedEventEntity = jsonReader.getObjectFromFile("json/event-entity.json", EventEntity.class);
        expectedEventEntity.setPayload(PAYLOAD);
        assertEquals(expectedEventEntity, actualEventEntity);
    }

    @Test
    void toEventEntity_instanceIdNotSet() {
        EventPO eventPO = jsonReader.getObjectFromFile("json/event.json", EventPO.class);
        eventPO.setPayload(PAYLOAD);
        eventPO.setInstanceId(null);

        EventEntity actualEventEntity = mapper.toEventEntity(eventPO);

        EventEntity expectedEventEntity = jsonReader.getObjectFromFile("json/event-entity.json", EventEntity.class);
        expectedEventEntity.setPayload(PAYLOAD);
        expectedEventEntity.setInstanceId("UNDEFINED");
        assertEquals(expectedEventEntity, actualEventEntity);
    }

    @Test
    void toEventEntity_nullValue() {
        EventEntity actualEventEntity = mapper.toEventEntity(null);
        assertNull(actualEventEntity);
    }
}
