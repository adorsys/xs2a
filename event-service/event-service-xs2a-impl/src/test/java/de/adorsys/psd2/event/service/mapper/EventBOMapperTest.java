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

package de.adorsys.psd2.event.service.mapper;

import de.adorsys.psd2.event.persist.model.EventPO;
import de.adorsys.psd2.event.service.model.EventBO;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Xs2aEventBOMapperImpl.class, Xs2aObjectMapper.class})
class EventBOMapperTest {
    private static final String PAYLOAD = "payload";

    @Autowired
    private Xs2aEventBOMapper mapper;

    private JsonReader jsonReader = new JsonReader();
    private Xs2aObjectMapper xs2aObjectMapper = new Xs2aObjectMapper();
    private byte[] payloadAsBytes;

    @BeforeEach
    void setUp() throws Exception {
        payloadAsBytes = xs2aObjectMapper.writeValueAsBytes(PAYLOAD);
    }

    @Test
    void toEventPO() {
        EventBO eventBO = jsonReader.getObjectFromFile("json/event-bo.json", EventBO.class);

        EventPO actualEventPO = mapper.toEventPO(eventBO);

        EventPO expectedEventPO = jsonReader.getObjectFromFile("json/event-po.json", EventPO.class);
        expectedEventPO.setPayload(payloadAsBytes);

        assertEquals(expectedEventPO, actualEventPO);
    }

    @Test
    void toEventPO_nullValue() {
        EventPO actualEventPO = mapper.toEventPO(null);
        assertNull(actualEventPO);
    }
}
