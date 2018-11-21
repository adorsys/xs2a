/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.domain.event.EventEntity;
import de.adorsys.psd2.consent.repository.EventRepository;
import de.adorsys.psd2.consent.service.mapper.EventMapper;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.event.Event;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EventServiceInternalTest {
    private static final long EVENT_ID = 100;
    private static final String DECRYPTED_ID = "0310318d-c87d-405b-bd2b-166af5124e1f";

    @InjectMocks
    private EventServiceInternal eventServiceInternal;

    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventMapper eventMapper;
    @Mock
    private SecurityDataService securityDataService;

    @Before
    public void setUp() {
        when(eventMapper.mapToEventEntity(any())).thenReturn(buildEventEntity());
        when(eventRepository.save(any(EventEntity.class)))
            .thenReturn(buildEventEntity(EVENT_ID));
        when(securityDataService.decryptId(anyString())).thenReturn(Optional.of(DECRYPTED_ID));
    }

    @Test
    public void recordEvent() {
        // Given
        Event event = new Event();

        // When
        boolean actual = eventServiceInternal.recordEvent(event);

        // Then
        assertThat(actual).isTrue();
        verify(eventRepository, atLeastOnce()).save(any(EventEntity.class));
    }

    private EventEntity buildEventEntity() {
        return buildEventEntity(null);
    }

    private EventEntity buildEventEntity(Long id) {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setId(id);
        return eventEntity;
    }
}
