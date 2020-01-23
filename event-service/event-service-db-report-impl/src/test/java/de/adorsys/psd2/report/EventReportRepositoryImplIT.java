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

package de.adorsys.psd2.report;

import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.event.persist.model.EventPO;
import de.adorsys.psd2.event.persist.model.ReportEvent;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ContextConfiguration(classes = TestDBConfiguration.class)
class EventReportRepositoryImplIT {
    private static final String INSTANCE_ID = "3de76f19-1df7-44d8-b760-ca972d2f945c";
    private static final String CONSENT_ID = "fa6e687b-1ac9-4b1a-9c74-357c35c82ba1";
    private static final String PAYMENT_ID = "j-t4XyLJTzQkonfSTnyxIMc";
    private static final byte[] PAYLOAD = "payload".getBytes();
    private static final OffsetDateTime CREATED_DATETIME = OffsetDateTime.now();

    @Autowired
    private EventReportRepositoryImpl repository;
    private JsonReader jsonReader = new JsonReader();
    private EventPO eventPO;
    private OffsetDateTime start;
    private OffsetDateTime end;

    @BeforeEach
    void setUp() {
        eventPO = jsonReader.getObjectFromFile("json/event.json", EventPO.class);
        eventPO.setTimestamp(CREATED_DATETIME);
        eventPO.setPayload(PAYLOAD);

        start = CREATED_DATETIME.minusHours(1);
        end = CREATED_DATETIME.plusHours(1);
    }

    @Test
    @Disabled
    void getEventsForPeriod() {
        List<ReportEvent> eventsForPeriod = repository.getEventsForPeriod(start, end, INSTANCE_ID);
        assertNotNull(eventsForPeriod);
        assertEquals(1, eventsForPeriod.size());
        assertEquals(eventPO, eventsForPeriod.get(0));
    }

    @Test
    @Disabled
    void getEventsForPeriodAndConsentId() {
        List<ReportEvent> eventsForPeriod = repository.getEventsForPeriodAndConsentId(start, end, CONSENT_ID, INSTANCE_ID);
        assertNotNull(eventsForPeriod);
        assertEquals(1, eventsForPeriod.size());
        assertEquals(eventPO, eventsForPeriod.get(0));
    }

    @Test
    @Disabled
    void getEventsForPeriodAndPaymentId() {
        List<ReportEvent> eventsForPeriod = repository.getEventsForPeriodAndPaymentId(start, end, PAYMENT_ID, INSTANCE_ID);
        assertNotNull(eventsForPeriod);
        assertEquals(1, eventsForPeriod.size());
        assertEquals(eventPO, eventsForPeriod.get(0));
    }

    @Test
    @Disabled
    void getEventsForPeriodAndEventOrigin() {
        List<ReportEvent> eventsForPeriod = repository.getEventsForPeriodAndEventOrigin(start, end, EventOrigin.TPP, INSTANCE_ID);
        assertNotNull(eventsForPeriod);
        assertEquals(1, eventsForPeriod.size());
        assertEquals(eventPO, eventsForPeriod.get(0));
    }

    @Test
    @Disabled
    void getEventsForPeriodAndEventType() {
        List<ReportEvent> eventsForPeriod = repository.getEventsForPeriodAndEventType(start, end, EventType.PAYMENT_INITIATION_REQUEST_RECEIVED, INSTANCE_ID);
        assertNotNull(eventsForPeriod);
        assertEquals(1, eventsForPeriod.size());
        assertEquals(eventPO, eventsForPeriod.get(0));
    }
}
