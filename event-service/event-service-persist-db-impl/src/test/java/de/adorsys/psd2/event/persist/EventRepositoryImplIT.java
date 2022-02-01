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

package de.adorsys.psd2.event.persist;

import de.adorsys.psd2.event.persist.model.EventPO;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ContextConfiguration(classes = TestDBConfiguration.class)
class EventRepositoryImplIT {
    private static final byte[] PAYLOAD = "payload".getBytes();
    private static final OffsetDateTime CREATED_DATETIME = OffsetDateTime.now();

    @Autowired
    private EventRepositoryImpl repository;
    private JsonReader jsonReader = new JsonReader();
    private Long savedId;

    @BeforeEach
    void setUp() {
        EventPO eventPO = jsonReader.getObjectFromFile("json/event.json", EventPO.class);
        eventPO.setTimestamp(CREATED_DATETIME);
        eventPO.setPayload(PAYLOAD);

        savedId = repository.save(eventPO);
        assertNotNull(savedId);
        eventPO.setId(savedId);
    }

    @Test
    void save() {
        assertNotNull(savedId);
    }
}
