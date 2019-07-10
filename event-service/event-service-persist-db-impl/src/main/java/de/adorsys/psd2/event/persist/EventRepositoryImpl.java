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

import de.adorsys.psd2.event.persist.entity.EventEntity;
import de.adorsys.psd2.event.persist.jpa.EventJPARepository;
import de.adorsys.psd2.event.persist.mapper.EventDBMapper;
import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.persist.model.EventPO;
import de.adorsys.psd2.event.core.model.EventType;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventRepositoryImpl implements EventRepository {
    private final EventJPARepository eventRepository;
    private final EventDBMapper eventDBMapper;

    @Override
    @Transactional
    public Long save(EventPO eventPO) {
        EventEntity entity = eventDBMapper.toEventEntity(eventPO);
        eventRepository.save(entity);
        return entity.getId();
    }

    @Override
    public List<EventPO> getEventsForPeriod(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @Nullable String instanceId) {
        List<EventEntity> result = eventRepository.findByTimestampBetweenAndInstanceIdOrderByTimestampAsc(start, end, instanceId);
        return eventDBMapper.toEventPOList(result);
    }

    @Override
    public List<EventPO> getEventsForPeriodAndConsentId(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull String consentId, @Nullable String instanceId) {
        List<EventEntity> result = eventRepository.findByTimestampBetweenAndConsentIdAndInstanceIdOrderByTimestampAsc(start, end, consentId, instanceId);
        return eventDBMapper.toEventPOList(result);
    }

    @Override
    public List<EventPO> getEventsForPeriodAndPaymentId(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull String paymentId, @Nullable String instanceId) {
        List<EventEntity> result = eventRepository.findByTimestampBetweenAndPaymentIdAndInstanceIdOrderByTimestampAsc(start, end, paymentId, instanceId);
        return eventDBMapper.toEventPOList(result);
    }

    @Override
    public List<EventPO> getEventsForPeriodAndEventType(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull EventType eventType, @Nullable String instanceId) {
        List<EventEntity> result = eventRepository.findByTimestampBetweenAndEventTypeAndInstanceIdOrderByTimestampAsc(start, end, eventType, instanceId);
        return eventDBMapper.toEventPOList(result);
    }

    @Override
    public List<EventPO> getEventsForPeriodAndEventOrigin(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull EventOrigin eventOrigin, @Nullable String instanceId) {
        List<EventEntity> result = eventRepository.findByTimestampBetweenAndEventOriginAndInstanceIdOrderByTimestampAsc(start, end, eventOrigin, instanceId);
        return eventDBMapper.toEventPOList(result);
    }
}
