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

import de.adorsys.psd2.consent.aspsp.api.CmsAspspEventService;
import de.adorsys.psd2.consent.domain.event.EventEntity;
import de.adorsys.psd2.consent.repository.EventRepository;
import de.adorsys.psd2.consent.service.mapper.EventMapper;
import de.adorsys.psd2.xs2a.core.event.Event;
import de.adorsys.psd2.xs2a.core.event.EventOrigin;
import de.adorsys.psd2.xs2a.core.event.EventType;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CmsAspspEventServiceInternal implements CmsAspspEventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Override
    public List<Event> getEventsForPeriod(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end) {
        List<EventEntity> eventEntity = eventRepository.findByTimestampBetweenOrderByTimestampAsc(start, end);
        return eventMapper.mapToEventList(eventEntity);
    }

    @Override
    public List<Event> getEventsForPeriodAndConsentId(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull String consentId) {
        List<EventEntity> eventEntity = eventRepository.findByTimestampBetweenAndConsentIdOrderByTimestampAsc(start, end, consentId);
        return eventMapper.mapToEventList(eventEntity);
    }

    @Override
    public List<Event> getEventsForPeriodAndPaymentId(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull String paymentId) {
        List<EventEntity> eventEntity = eventRepository.findByTimestampBetweenAndPaymentIdOrderByTimestampAsc(start, end, paymentId);
        return eventMapper.mapToEventList(eventEntity);
    }

    @Override
    public List<Event> getEventsForPeriodAndEventType(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull EventType eventType) {
        List<EventEntity> eventEntity = eventRepository.findByTimestampBetweenAndEventTypeOrderByTimestampAsc(start, end, eventType);
        return eventMapper.mapToEventList(eventEntity);
    }

    @Override
    public List<Event> getEventsForPeriodAndEventOrigin(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull EventOrigin eventOrigin) {
        List<EventEntity> eventEntity = eventRepository.findByTimestampBetweenAndEventOriginOrderByTimestampAsc(start, end, eventOrigin);
        return eventMapper.mapToEventList(eventEntity);
    }
}
