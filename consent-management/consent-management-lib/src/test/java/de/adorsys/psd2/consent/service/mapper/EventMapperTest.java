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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.domain.event.EventEntity;
import de.adorsys.psd2.consent.service.JsonConverterService;
import de.adorsys.psd2.xs2a.core.event.Event;
import de.adorsys.psd2.xs2a.core.event.EventOrigin;
import de.adorsys.psd2.xs2a.core.event.EventType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventMapperTest {
    private static final OffsetDateTime TIMESTAMP =
        OffsetDateTime.of(2019, 2, 20, 12, 0, 0, 0, ZoneOffset.UTC);
    private static final String CONSENT_ID = "consent id";
    private static final String PAYMENT_ID = "payment id";
    private static final String PAYLOAD_OBJECT = "payload";
    private static final byte[] PAYLOAD_BYTES = "payload".getBytes();
    private static final EventOrigin EVENT_ORIGIN = EventOrigin.ASPSP;
    private static final EventType EVENT_TYPE = EventType.FUNDS_CONFIRMATION_REQUEST_RECEIVED;
    private static final String INSTANCE_ID = "instance id";
    private static final String DEFAULT_INSTANCE_ID = "UNDEFINED";

    @InjectMocks
    private EventMapper eventMapper;
    @Mock
    private JsonConverterService jsonConverterService;

    @Before
    public void setUp() {
        when(jsonConverterService.toJsonBytes(PAYLOAD_OBJECT))
            .thenReturn(Optional.of(PAYLOAD_BYTES));
        when(jsonConverterService.toObject(PAYLOAD_BYTES, Object.class))
            .thenReturn(Optional.of(PAYLOAD_OBJECT));
    }

    @Test
    public void mapToEventList() {
        List<Event> events = eventMapper.mapToEventList(Collections.singletonList(buildEventEntity()));

        assertNotNull(events);
        assertEquals(1, events.size());

        Event event = events.get(0);
        assertEquals(TIMESTAMP, event.getTimestamp());
        assertEquals(CONSENT_ID, event.getConsentId());
        assertEquals(PAYMENT_ID, event.getPaymentId());
        assertEquals(PAYLOAD_OBJECT, event.getPayload());
        assertEquals(EVENT_ORIGIN, event.getEventOrigin());
        assertEquals(EVENT_TYPE, event.getEventType());
        assertEquals(INSTANCE_ID, event.getInstanceId());
    }

    @Test
    public void mapToEventEntity() {
        EventEntity eventEntity = eventMapper.mapToEventEntity(buildEvent());

        assertNotNull(eventEntity);
        assertEquals(TIMESTAMP, eventEntity.getTimestamp());
        assertEquals(CONSENT_ID, eventEntity.getConsentId());
        assertEquals(PAYMENT_ID, eventEntity.getPaymentId());
        assertEquals(PAYLOAD_BYTES, eventEntity.getPayload());
        assertEquals(EVENT_ORIGIN, eventEntity.getEventOrigin());
        assertEquals(EVENT_TYPE, eventEntity.getEventType());
    }

    @Test
    public void mapToEventEntity_shouldIgnoreInstanceId() {
        Event event = buildEvent();
        event.setInstanceId("custom instance id");

        EventEntity eventEntity = eventMapper.mapToEventEntity(event);

        assertEquals(DEFAULT_INSTANCE_ID, eventEntity.getInstanceId());
    }

    private EventEntity buildEventEntity() {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setTimestamp(TIMESTAMP);
        eventEntity.setConsentId(CONSENT_ID);
        eventEntity.setPaymentId(PAYMENT_ID);
        eventEntity.setPayload(PAYLOAD_BYTES);
        eventEntity.setEventOrigin(EVENT_ORIGIN);
        eventEntity.setEventType(EVENT_TYPE);
        eventEntity.setInstanceId(INSTANCE_ID);
        return eventEntity;
    }

    private Event buildEvent() {
        return Event.builder()
                   .timestamp(TIMESTAMP)
                   .consentId(CONSENT_ID)
                   .paymentId(PAYMENT_ID)
                   .payload(PAYLOAD_OBJECT)
                   .eventOrigin(EVENT_ORIGIN)
                   .eventType(EVENT_TYPE)
                   .instanceId(INSTANCE_ID)
                   .build();
    }
}
