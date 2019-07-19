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

package de.adorsys.psd2.event.persist;

import de.adorsys.psd2.event.persist.model.EventPO;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.OffsetDateTime;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = TestDBConfiguration.class)
public class EventRepositoryImplIT {
    private static final byte[] PAYLOAD = "payload".getBytes();
    private static final OffsetDateTime CREATED_DATETIME = OffsetDateTime.now();

    @Autowired
    private EventRepositoryImpl repository;
    private JsonReader jsonReader = new JsonReader();
    private EventPO eventPO;
    private Long savedId;

    @Before
    public void setUp() {
        eventPO = jsonReader.getObjectFromFile("json/event.json", EventPO.class);
        eventPO.setTimestamp(CREATED_DATETIME);
        eventPO.setPayload(PAYLOAD);

        savedId = repository.save(eventPO);
        assertNotNull(savedId);
        eventPO.setId(savedId);
    }

    @Test
    public void save() {
        assertNotNull(savedId);
    }
}
