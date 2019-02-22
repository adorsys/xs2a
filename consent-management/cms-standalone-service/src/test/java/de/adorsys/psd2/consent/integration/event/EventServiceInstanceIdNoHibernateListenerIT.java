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

package de.adorsys.psd2.consent.integration.event;

import de.adorsys.psd2.consent.api.service.EventService;
import de.adorsys.psd2.consent.config.HibernateListenerConfig;
import de.adorsys.psd2.consent.domain.event.EventEntity;
import de.adorsys.psd2.consent.integration.config.IntegrationTestConfiguration;
import de.adorsys.psd2.consent.repository.EventRepository;
import de.adorsys.psd2.xs2a.core.event.Event;
import de.adorsys.psd2.xs2a.core.event.EventOrigin;
import de.adorsys.psd2.xs2a.core.event.EventType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.Assert.assertEquals;

@ActiveProfiles("integration-test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = IntegrationTestConfiguration.class)
@DataJpaTest
public class EventServiceInstanceIdNoHibernateListenerIT {
    private static final String DEFAULT_INSTANCE_ID = "UNDEFINED";

    @Autowired
    private EventService eventService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private EventRepository eventRepository;

    // Turn off Hibernate listener for setting instanceId
    @MockBean
    private HibernateListenerConfig hibernateListenerConfig;

    @Test
    public void recordEvent_withNoInstanceIdInEvent_shouldSetDefaultInstanceId() {
        // Given
        OffsetDateTime timestamp = OffsetDateTime.of(2019, 2, 20, 12, 0, 0, 0, ZoneOffset.UTC);
        Event event = Event.builder()
                          .timestamp(timestamp)
                          .eventOrigin(EventOrigin.XS2A_INTERNAL)
                          .eventType(EventType.FUNDS_CONFIRMATION_REQUEST_RECEIVED)
                          .build();

        // When
        eventService.recordEvent(event);

        flushAndClearPersistenceContext();

        Iterable<EventEntity> entities = eventRepository.findAll();
        EventEntity savedEntity = entities.iterator().next();


        // Then
        assertEquals(DEFAULT_INSTANCE_ID, savedEntity.getInstanceId());
    }

    @Test
    public void recordEvent_withCustomInstanceIdInEvent_shouldIgnoreCustomInstanceId() {
        // Given
        OffsetDateTime timestamp = OffsetDateTime.of(2019, 2, 20, 12, 0, 0, 0, ZoneOffset.UTC);
        String testInstanceId = "Test instance id";
        Event event = Event.builder()
                          .timestamp(timestamp)
                          .eventOrigin(EventOrigin.XS2A_INTERNAL)
                          .eventType(EventType.FUNDS_CONFIRMATION_REQUEST_RECEIVED)
                          .instanceId(testInstanceId)
                          .build();

        // When
        eventService.recordEvent(event);

        flushAndClearPersistenceContext();

        Iterable<EventEntity> entities = eventRepository.findAll();
        EventEntity savedEntity = entities.iterator().next();

        // Then
        assertEquals(DEFAULT_INSTANCE_ID, savedEntity.getInstanceId());
    }

    /**
     * Flush and clear the persistence context to force the call to the database
     */
    private void flushAndClearPersistenceContext() {
        entityManager.flush();
        entityManager.clear();
    }
}
