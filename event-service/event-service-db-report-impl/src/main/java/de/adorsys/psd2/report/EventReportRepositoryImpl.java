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
import de.adorsys.psd2.report.entity.EventReportEntity;
import de.adorsys.psd2.report.jpa.EventReportJPARepository;
import de.adorsys.psd2.report.mapper.EventReportDBMapper;
import de.adorsys.psd2.report.specification.EventSpecification;
import de.adorsys.psd2.report.util.EventPageRequestBuilder;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventReportRepositoryImpl implements EventReportRepository {
    private final EventReportDBMapper eventReportDBMapper;
    private final EventReportJPARepository eventJpaRepository;
    private final EventSpecification eventSpecification;
    private final EventPageRequestBuilder pageRequestBuilder;

    @Override
    public List<ReportEvent> getEventsForPeriod(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @Nullable String instanceId,
                                                @Nullable Integer pageIndex, @Nullable Integer itemsPerPage) {
        Pageable pageable = pageRequestBuilder.getPageable(pageIndex, itemsPerPage);
        List<EventReportEntity> events = eventJpaRepository.findAll(eventSpecification.byPeriodAndInstanceId(start, end, instanceId), pageable)
                                            .stream()
                                            .collect(Collectors.toList());
        return eventReportDBMapper.mapToAspspReportEvents(events);
    }

    @Override
    public List<ReportEvent> getEventsForPeriodAndConsentId(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull String consentId, @Nullable String instanceId,
                                                            @Nullable Integer pageIndex, @Nullable Integer itemsPerPage) {
        Pageable pageable = pageRequestBuilder.getPageable(pageIndex, itemsPerPage);
        List<EventReportEntity> events = eventJpaRepository.findAll(eventSpecification.byPeriodAndInstanceIdAndConsentId(start, end, instanceId, consentId), pageable)
                                            .stream()
                                            .collect(Collectors.toList());
        return eventReportDBMapper.mapToAspspReportEvents(events);
    }

    @Override
    public List<ReportEvent> getEventsForPeriodAndPaymentId(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull String paymentId, @Nullable String instanceId,
                                                            @Nullable Integer pageIndex, @Nullable Integer itemsPerPage) {
        Pageable pageable = pageRequestBuilder.getPageable(pageIndex, itemsPerPage);
        List<EventReportEntity> events = eventJpaRepository.findAll(eventSpecification.byPeriodAndInstanceIdAndPaymentId(start, end, instanceId, paymentId), pageable)
                                            .stream()
                                            .collect(Collectors.toList());
        return eventReportDBMapper.mapToAspspReportEvents(events);
    }

    @Override
    public List<ReportEvent> getEventsForPeriodAndEventType(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull EventType eventType, @Nullable String instanceId,
                                                            @Nullable Integer pageIndex, @Nullable Integer itemsPerPage) {
        Pageable pageable = pageRequestBuilder.getPageable(pageIndex, itemsPerPage);
        List<EventReportEntity> events = eventJpaRepository.findAll(eventSpecification.byPeriodAndInstanceIdAndEventType(start, end, instanceId, eventType), pageable)
                                            .stream()
                                            .collect(Collectors.toList());
        return eventReportDBMapper.mapToAspspReportEvents(events);
    }

    @Override
    public List<ReportEvent> getEventsForPeriodAndEventOrigin(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull EventOrigin eventOrigin, @Nullable String instanceId,
                                                              @Nullable Integer pageIndex, @Nullable Integer itemsPerPage) {
        Pageable pageable = pageRequestBuilder.getPageable(pageIndex, itemsPerPage);
        List<EventReportEntity> events = eventJpaRepository.findAll(eventSpecification.byPeriodAndInstanceIdAndEventOrigin(start, end, instanceId, eventOrigin), pageable)
                                            .stream()
                                            .collect(Collectors.toList());
        return eventReportDBMapper.mapToAspspReportEvents(events);
    }
}
