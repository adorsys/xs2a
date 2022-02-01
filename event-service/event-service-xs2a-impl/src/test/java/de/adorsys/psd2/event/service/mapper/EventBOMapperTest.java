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
