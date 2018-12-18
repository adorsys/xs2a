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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.domain.event.EventEntity;
import de.adorsys.psd2.consent.service.JsonConverterService;
import de.adorsys.psd2.xs2a.core.event.Event;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EventMapper {
    private final JsonConverterService jsonConverterService;

    public List<Event> mapToEventList(@NotNull List<EventEntity> eventEntities) {
        return eventEntities.stream()
                   .map(this::mapToEvent)
                   .collect(Collectors.toList());
    }

    public EventEntity mapToEventEntity(@NotNull Event event) {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setTimestamp(event.getTimestamp());
        eventEntity.setConsentId(event.getConsentId());
        eventEntity.setPaymentId(event.getPaymentId());
        byte[] payload = jsonConverterService.toJsonBytes(event.getPayload())
                             .orElse(null);
        eventEntity.setPayload(payload);
        eventEntity.setEventOrigin(event.getEventOrigin());
        eventEntity.setEventType(event.getEventType());
        return eventEntity;
    }

    private Event mapToEvent(@NotNull EventEntity eventEntity) {
        Object payload = jsonConverterService.toObject(eventEntity.getPayload(), Object.class)
                             .orElse(null);
        return Event.builder()
                   .timestamp(eventEntity.getTimestamp())
                   .consentId(eventEntity.getConsentId())
                   .paymentId(eventEntity.getPaymentId())
                   .payload(payload)
                   .eventOrigin(eventEntity.getEventOrigin())
                   .eventType(eventEntity.getEventType())
                   .instanceId(eventEntity.getInstanceId())
                   .build();
    }
}
