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

package de.adorsys.psd2.event.service;

import de.adorsys.psd2.event.persist.EventRepository;
import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.persist.model.EventPO;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.event.service.mapper.AspspEventBOMapper;
import de.adorsys.psd2.event.service.model.EventBO;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AspspEventServiceImpl implements AspspEventService {
    private final EventRepository eventRepository;
    private final AspspEventBOMapper eventBOMapper;

    @Override
    public List<EventBO> getEventsForPeriod(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @Nullable String instanceId) {
        List<EventPO> result = eventRepository.getEventsForPeriod(start, end, instanceId);
        return eventBOMapper.toEventBOList(result);
    }

    @Override
    public List<EventBO> getEventsForPeriodAndConsentId(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull String consentId, @Nullable String instanceId) {
        List<EventPO> result = eventRepository.getEventsForPeriodAndConsentId(start, end, consentId, instanceId);
        return eventBOMapper.toEventBOList(result);
    }

    @Override
    public List<EventBO> getEventsForPeriodAndPaymentId(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull String paymentId, @Nullable String instanceId) {
        List<EventPO> result = eventRepository.getEventsForPeriodAndPaymentId(start, end, paymentId, instanceId);
        return eventBOMapper.toEventBOList(result);
    }

    @Override
    public List<EventBO> getEventsForPeriodAndEventType(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull EventType eventType, @Nullable String instanceId) {
        List<EventPO> result = eventRepository.getEventsForPeriodAndEventType(start, end, eventType, instanceId);
        return eventBOMapper.toEventBOList(result);
    }

    @Override
    public List<EventBO> getEventsForPeriodAndEventOrigin(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull EventOrigin eventOrigin, @Nullable String instanceId) {
        List<EventPO> result = eventRepository.getEventsForPeriodAndEventOrigin(start, end, eventOrigin, instanceId);
        return eventBOMapper.toEventBOList(result);
    }
}
