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

package de.adorsys.psd2.xs2a.service.event;

import de.adorsys.psd2.consent.api.service.EventService;
import de.adorsys.psd2.xs2a.core.event.Event;
import de.adorsys.psd2.xs2a.core.event.EventOrigin;
import de.adorsys.psd2.xs2a.core.event.EventType;
import de.adorsys.psd2.xs2a.domain.RequestData;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.TppService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class Xs2aEventServiceTest {
    private static final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private static final String PAYMENT_ID = "0795805d-651b-4e00-88fb-a34248337bbd";
    private static final String URI = "/v1/consents";
    private static final UUID REQUEST_ID = UUID.fromString("0d7f200e-09b4-46f5-85bd-f4ea89fccace");
    private static final String TPP_IP = "1.2.3.4";
    private static final EventType EVENT_TYPE = EventType.PAYMENT_INITIATION_REQUEST_RECEIVED;

    @Mock
    private TppService tppService;
    @Mock
    private EventService eventService;
    @Mock
    private RequestProviderService requestProviderService;

    @InjectMocks
    private Xs2aEventService xs2aEventService;

    @Before
    public void setUp() {
        when(eventService.recordEvent(any(Event.class))).thenReturn(true);
        when(requestProviderService.getRequestData()).thenReturn(buildRequestData());
    }

    @Test
    public void recordAisTppRequest_Success() {
        // Given
        ArgumentCaptor<Event> argumentCaptor = ArgumentCaptor.forClass(Event.class);

        // When
        xs2aEventService.recordAisTppRequest(CONSENT_ID, EVENT_TYPE, null);

        // Then
        verify(eventService).recordEvent(argumentCaptor.capture());
        Event capturedEvent = argumentCaptor.getValue();
        assertThat(capturedEvent.getTimestamp()).isNotNull();
        assertThat(capturedEvent.getEventOrigin()).isEqualTo(EventOrigin.TPP);
        assertThat(capturedEvent.getEventType()).isEqualTo(EVENT_TYPE);
        assertThat(capturedEvent.getPayload()).isNotNull();
    }

    @Test
    public void recordPisTppRequest_Success() {
        // Given
        ArgumentCaptor<Event> argumentCaptor = ArgumentCaptor.forClass(Event.class);

        // When
        xs2aEventService.recordPisTppRequest(PAYMENT_ID, EVENT_TYPE, null);

        // Then
        verify(eventService).recordEvent(argumentCaptor.capture());
        Event capturedEvent = argumentCaptor.getValue();
        assertThat(capturedEvent.getTimestamp()).isNotNull();
        assertThat(capturedEvent.getEventOrigin()).isEqualTo(EventOrigin.TPP);
        assertThat(capturedEvent.getEventType()).isEqualTo(EVENT_TYPE);
        assertThat(capturedEvent.getPayload()).isNotNull();
    }

    @Test
    public void recordTppRequest_Success() {
        // Given
        ArgumentCaptor<Event> argumentCaptor = ArgumentCaptor.forClass(Event.class);

        // When
        xs2aEventService.recordTppRequest(EVENT_TYPE, null);

        // Then
        verify(eventService).recordEvent(argumentCaptor.capture());
        Event capturedEvent = argumentCaptor.getValue();
        assertThat(capturedEvent.getTimestamp()).isNotNull();
        assertThat(capturedEvent.getEventOrigin()).isEqualTo(EventOrigin.TPP);
        assertThat(capturedEvent.getEventType()).isEqualTo(EVENT_TYPE);
        assertThat(capturedEvent.getPayload()).isNotNull();
    }

    private RequestData buildRequestData() {
        return new RequestData(URI, REQUEST_ID, TPP_IP, Collections.emptyMap());
    }
}
