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
