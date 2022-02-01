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

package de.adorsys.psd2.xs2a.service.event;

import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.event.service.Xs2aEventServiceEncrypted;
import de.adorsys.psd2.event.service.model.EventBO;
import de.adorsys.psd2.event.service.model.PsuIdDataBO;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.RequestData;
import de.adorsys.psd2.xs2a.domain.event.RequestEventPayload;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.event.mapper.EventMapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Xs2aEventServiceTest {
    private static final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private static final String PAYMENT_ID = "0795805d-651b-4e00-88fb-a34248337bbd";
    private static final String URI = "/v1/consents";
    private static final UUID X_REQUEST_ID = UUID.fromString("0d7f200e-09b4-46f5-85bd-f4ea89fccace");
    private static final UUID INTERNAL_REQUEST_ID = UUID.fromString("9fe83704-6019-46fa-b8aa-53fb8fa667ea");
    private static final String TPP_IP = "1.2.3.4";
    private static final EventType EVENT_TYPE = EventType.PAYMENT_INITIATION_REQUEST_RECEIVED;
    private static final String AUTHORISATION_NUMBER = "999";
    private static final String BODY = "body";

    @InjectMocks
    private Xs2aEventService xs2aEventService;

    @Mock
    private TppService tppService;
    @Mock
    private Xs2aEventServiceEncrypted eventService;
    @Mock
    private RequestProviderService requestProviderService;
    @Spy
    private EventMapper eventMapper = Mappers.getMapper(EventMapper.class);

    @Captor
    private ArgumentCaptor<EventBO> eventCaptor;

    private JsonReader jsonReader = new JsonReader();
    private PsuIdDataBO psuIdData;


    @BeforeEach
    void setUp() {
        psuIdData = jsonReader.getObjectFromFile("json/service/event/psu-id-data.json", PsuIdDataBO.class);

        when(eventService.recordEvent(eventCaptor.capture())).thenReturn(true);
        when(requestProviderService.getRequestData()).thenReturn(buildRequestData());
        when(tppService.getTppInfo()).thenReturn(buildTppInfo());
    }

    @Test
    void recordAisTppRequest_withBody() {
        // Given

        // When
        xs2aEventService.recordConsentTppRequest(CONSENT_ID, EVENT_TYPE, "body");

        // Then
        verify(eventService, times(1)).recordEvent(any(EventBO.class));
        EventBO capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getTimestamp()).isNotNull();
        assertThat(capturedEvent.getEventOrigin()).isEqualTo(EventOrigin.TPP);
        assertThat(capturedEvent.getEventType()).isEqualTo(EVENT_TYPE);
        assertThat(capturedEvent.getPayload()).isNotNull();
        assertThat(capturedEvent.getPsuIdData()).isEqualTo(psuIdData);
        assertThat(capturedEvent.getTppAuthorisationNumber()).isEqualTo(AUTHORISATION_NUMBER);
        assertThat(capturedEvent.getXRequestId()).isEqualTo(X_REQUEST_ID);
        assertThat(capturedEvent.getInternalRequestId()).isEqualTo(INTERNAL_REQUEST_ID);
        assertThat(((RequestEventPayload) capturedEvent.getPayload()).getBody()).isEqualTo(BODY);
    }

    @Test
    void recordAisTppRequest_bodyIsNull() {
        // Given

        // When
        xs2aEventService.recordConsentTppRequest(CONSENT_ID, EVENT_TYPE);

        // Then
        verify(eventService, times(1)).recordEvent(any(EventBO.class));
        EventBO capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getTimestamp()).isNotNull();
        assertThat(capturedEvent.getEventOrigin()).isEqualTo(EventOrigin.TPP);
        assertThat(capturedEvent.getEventType()).isEqualTo(EVENT_TYPE);
        assertThat(capturedEvent.getPayload()).isNotNull();
        assertThat(capturedEvent.getPsuIdData()).isEqualTo(psuIdData);
        assertThat(capturedEvent.getTppAuthorisationNumber()).isEqualTo(AUTHORISATION_NUMBER);
        assertThat(capturedEvent.getXRequestId()).isEqualTo(X_REQUEST_ID);
        assertThat(capturedEvent.getInternalRequestId()).isEqualTo(INTERNAL_REQUEST_ID);
        assertThat(((RequestEventPayload) capturedEvent.getPayload()).getBody()).isNull();
    }

    @Test
    void recordPisTppRequest_withBody() {
        // Given

        // When
        xs2aEventService.recordPisTppRequest(PAYMENT_ID, EVENT_TYPE, BODY);

        // Then
        verify(eventService, times(1)).recordEvent(any(EventBO.class));
        EventBO capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getTimestamp()).isNotNull();
        assertThat(capturedEvent.getEventOrigin()).isEqualTo(EventOrigin.TPP);
        assertThat(capturedEvent.getEventType()).isEqualTo(EVENT_TYPE);
        assertThat(capturedEvent.getPayload()).isNotNull();
        assertThat(capturedEvent.getPsuIdData()).isEqualTo(psuIdData);
        assertThat(capturedEvent.getTppAuthorisationNumber()).isEqualTo(AUTHORISATION_NUMBER);
        assertThat(capturedEvent.getXRequestId()).isEqualTo(X_REQUEST_ID);
        assertThat(capturedEvent.getInternalRequestId()).isEqualTo(INTERNAL_REQUEST_ID);
        assertThat(((RequestEventPayload) capturedEvent.getPayload()).getBody()).isEqualTo(BODY);
    }

    @Test
    void recordPisTppRequest_bodyIsNull() {
        // Given

        // When
        xs2aEventService.recordPisTppRequest(PAYMENT_ID, EVENT_TYPE);

        // Then
        verify(eventService, times(1)).recordEvent(any(EventBO.class));
        EventBO capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getTimestamp()).isNotNull();
        assertThat(capturedEvent.getEventOrigin()).isEqualTo(EventOrigin.TPP);
        assertThat(capturedEvent.getEventType()).isEqualTo(EVENT_TYPE);
        assertThat(capturedEvent.getPayload()).isNotNull();
        assertThat(capturedEvent.getPsuIdData()).isEqualTo(psuIdData);
        assertThat(capturedEvent.getTppAuthorisationNumber()).isEqualTo(AUTHORISATION_NUMBER);
        assertThat(capturedEvent.getXRequestId()).isEqualTo(X_REQUEST_ID);
        assertThat(capturedEvent.getInternalRequestId()).isEqualTo(INTERNAL_REQUEST_ID);
        assertThat(((RequestEventPayload) capturedEvent.getPayload()).getBody()).isNull();
    }

    @Test
    void recordTppRequest_withBody() {
        // Given

        // When
        xs2aEventService.recordTppRequest(EVENT_TYPE, BODY);

        // Then
        verify(eventService, times(1)).recordEvent(any(EventBO.class));
        EventBO capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getTimestamp()).isNotNull();
        assertThat(capturedEvent.getEventOrigin()).isEqualTo(EventOrigin.TPP);
        assertThat(capturedEvent.getEventType()).isEqualTo(EVENT_TYPE);
        assertThat(capturedEvent.getPayload()).isNotNull();
        assertThat(capturedEvent.getPsuIdData()).isEqualTo(psuIdData);
        assertThat(capturedEvent.getTppAuthorisationNumber()).isEqualTo(AUTHORISATION_NUMBER);
        assertThat(capturedEvent.getXRequestId()).isEqualTo(X_REQUEST_ID);
        assertThat(capturedEvent.getInternalRequestId()).isEqualTo(INTERNAL_REQUEST_ID);
        assertThat(((RequestEventPayload) capturedEvent.getPayload()).getBody()).isEqualTo(BODY);
    }

    @Test
    void recordTppRequest_bodyIsNull() {
        // Given

        // When
        xs2aEventService.recordTppRequest(EVENT_TYPE);

        // Then
        verify(eventService, times(1)).recordEvent(any(EventBO.class));
        EventBO capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getTimestamp()).isNotNull();
        assertThat(capturedEvent.getEventOrigin()).isEqualTo(EventOrigin.TPP);
        assertThat(capturedEvent.getEventType()).isEqualTo(EVENT_TYPE);
        assertThat(capturedEvent.getPayload()).isNotNull();
        assertThat(capturedEvent.getPsuIdData()).isEqualTo(psuIdData);
        assertThat(capturedEvent.getTppAuthorisationNumber()).isEqualTo(AUTHORISATION_NUMBER);
        assertThat(capturedEvent.getXRequestId()).isEqualTo(X_REQUEST_ID);
        assertThat(capturedEvent.getInternalRequestId()).isEqualTo(INTERNAL_REQUEST_ID);
        assertThat(((RequestEventPayload) capturedEvent.getPayload()).getBody()).isNull();
    }

    private RequestData buildRequestData() {
        return new RequestData(URI, INTERNAL_REQUEST_ID, X_REQUEST_ID, TPP_IP, Collections.emptyMap(),
                               jsonReader.getObjectFromFile("json/service/event/psu-id-data.json", PsuIdData.class));
    }

    private TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(AUTHORISATION_NUMBER);
        return tppInfo;
    }
}
