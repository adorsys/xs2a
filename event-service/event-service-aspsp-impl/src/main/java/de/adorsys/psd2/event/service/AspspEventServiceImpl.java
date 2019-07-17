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

import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.event.persist.EventRepository;
import de.adorsys.psd2.event.persist.model.ReportEvent;
import de.adorsys.psd2.event.service.mapper.AspspEventMapper;
import de.adorsys.psd2.event.service.model.AspspEvent;
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
    private final AspspEventMapper eventBOMapper;

    @Override
    public List<AspspEvent> getEventsForPeriod(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @Nullable String instanceId) {
        List<ReportEvent> events = eventRepository.getEventsForPeriod(start, end, instanceId);


        return eventBOMapper.toAspspEventList(events);
    }

    @Override
    public List<AspspEvent> getEventsForPeriodAndConsentId(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull String consentId, @Nullable String instanceId) {
        List<ReportEvent> result = eventRepository.getEventsForPeriodAndConsentId(start, end, consentId, instanceId);
        return eventBOMapper.toAspspEventList(result);
    }

    @Override
    public List<AspspEvent> getEventsForPeriodAndPaymentId(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull String paymentId, @Nullable String instanceId) {
        List<ReportEvent> result = eventRepository.getEventsForPeriodAndPaymentId(start, end, paymentId, instanceId);
        return eventBOMapper.toAspspEventList(result);
    }

    @Override
    public List<AspspEvent> getEventsForPeriodAndEventType(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull EventType eventType, @Nullable String instanceId) {
        List<ReportEvent> result = eventRepository.getEventsForPeriodAndEventType(start, end, eventType, instanceId);
        return eventBOMapper.toAspspEventList(result);
    }

    @Override
    public List<AspspEvent> getEventsForPeriodAndEventOrigin(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull EventOrigin eventOrigin, @Nullable String instanceId) {
        List<ReportEvent> result = eventRepository.getEventsForPeriodAndEventOrigin(start, end, eventOrigin, instanceId);
        return eventBOMapper.toAspspEventList(result);
    }
}
