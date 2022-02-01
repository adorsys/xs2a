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
