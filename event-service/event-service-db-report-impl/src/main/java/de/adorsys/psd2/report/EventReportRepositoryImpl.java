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

package de.adorsys.psd2.report;

import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.event.persist.EventReportRepository;
import de.adorsys.psd2.event.persist.model.ReportEvent;
import de.adorsys.psd2.report.entity.EventEntityForReport;
import de.adorsys.psd2.report.jpa.EventReportJPARepository;
import de.adorsys.psd2.report.mapper.EventReportDBMapper;
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
public class EventReportRepositoryImpl implements EventReportRepository {
    private final EventReportJPARepository eventReportJPARepository;
    private final EventReportDBMapper eventReportDBMapper;

    @Override
    public List<ReportEvent> getEventsForPeriod(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @Nullable String instanceId) {
        List<EventEntityForReport> events = eventReportJPARepository.getEventsForPeriod(start, end, instanceId);
        return eventReportDBMapper.mapToAspspReportEvents(events);
    }

    @Override
    public List<ReportEvent> getEventsForPeriodAndConsentId(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull String consentId, @Nullable String instanceId) {
        List<EventEntityForReport> events = eventReportJPARepository.findByTimestampBetweenAndConsentIdAndInstanceIdOrderByTimestampAsc(start, end, consentId, instanceId);
        return eventReportDBMapper.mapToAspspReportEvents(events);
    }

    @Override
    public List<ReportEvent> getEventsForPeriodAndPaymentId(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull String paymentId, @Nullable String instanceId) {
        List<EventEntityForReport> events = eventReportJPARepository.findByTimestampBetweenAndPaymentIdAndInstanceIdOrderByTimestampAsc(start, end, paymentId, instanceId);
        return eventReportDBMapper.mapToAspspReportEvents(events);
    }

    @Override
    public List<ReportEvent> getEventsForPeriodAndEventType(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull EventType eventType, @Nullable String instanceId) {
        List<EventEntityForReport> events = eventReportJPARepository.findByTimestampBetweenAndEventTypeAndInstanceIdOrderByTimestampAsc(start, end, eventType, instanceId);
        return eventReportDBMapper.mapToAspspReportEvents(events);
    }

    @Override
    public List<ReportEvent> getEventsForPeriodAndEventOrigin(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull EventOrigin eventOrigin, @Nullable String instanceId) {
        List<EventEntityForReport> events = eventReportJPARepository.findByTimestampBetweenAndEventOriginAndInstanceIdOrderByTimestampAsc(start, end, eventOrigin, instanceId);
        return eventReportDBMapper.mapToAspspReportEvents(events);
    }
}
