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

import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.persist.model.EventPO;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = TestDBConfiguration.class)
public class EventRepositoryImplIT {
    private static final String INSTANCE_ID = "3de76f19-1df7-44d8-b760-ca972d2f945c";
    private static final String CONSENT_ID = "fa6e687b-1ac9-4b1a-9c74-357c35c82ba1";
    private static final String PAYMENT_ID = "j-t4XyLJTzQkonfSTnyxIMc";
    private static final byte[] PAYLOAD = "payload".getBytes();
    private static final OffsetDateTime CREATED_DATETIME = OffsetDateTime.now();

    @Autowired
    private EventRepositoryImpl repository;
    private JsonReader jsonReader = new JsonReader();
    private EventPO eventPO;
    private OffsetDateTime start;
    private OffsetDateTime end;
    private Long savedId;

    @Before
    public void setUp() {
        eventPO = jsonReader.getObjectFromFile("json/event.json", EventPO.class);
        eventPO.setTimestamp(CREATED_DATETIME);
        eventPO.setPayload(PAYLOAD);

        start = CREATED_DATETIME.minusHours(1);
        end = CREATED_DATETIME.plusHours(1);

        savedId = repository.save(eventPO);
        assertNotNull(savedId);
        eventPO.setId(savedId);
    }

    @Test
    public void save() {
        assertNotNull(savedId);
    }

    @Test
    public void getEventsForPeriod() {
        List<EventPO> eventsForPeriod = repository.getEventsForPeriod(start, end, INSTANCE_ID);
        assertNotNull(eventsForPeriod);
        assertEquals(1, eventsForPeriod.size());
        assertEquals(eventPO, eventsForPeriod.get(0));
    }

    @Test
    public void getEventsForPeriodAndConsentId() {
        List<EventPO> eventsForPeriod = repository.getEventsForPeriodAndConsentId(start, end, CONSENT_ID, INSTANCE_ID);
        assertNotNull(eventsForPeriod);
        assertEquals(1, eventsForPeriod.size());
        assertEquals(eventPO, eventsForPeriod.get(0));
    }

    @Test
    public void getEventsForPeriodAndPaymentId() {
        List<EventPO> eventsForPeriod = repository.getEventsForPeriodAndPaymentId(start, end, PAYMENT_ID, INSTANCE_ID);
        assertNotNull(eventsForPeriod);
        assertEquals(1, eventsForPeriod.size());
        assertEquals(eventPO, eventsForPeriod.get(0));
    }

    @Test
    public void getEventsForPeriodAndEventOrigin() {
        List<EventPO> eventsForPeriod = repository.getEventsForPeriodAndEventOrigin(start, end, EventOrigin.TPP, INSTANCE_ID);
        assertNotNull(eventsForPeriod);
        assertEquals(1, eventsForPeriod.size());
        assertEquals(eventPO, eventsForPeriod.get(0));
    }

    @Test
    public void getEventsForPeriodAndEventType() {
        List<EventPO> eventsForPeriod = repository.getEventsForPeriodAndEventType(start, end, EventType.PAYMENT_INITIATION_REQUEST_RECEIVED, INSTANCE_ID);
        assertNotNull(eventsForPeriod);
        assertEquals(1, eventsForPeriod.size());
        assertEquals(eventPO, eventsForPeriod.get(0));
    }
}
