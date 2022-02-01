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

package de.adorsys.psd2.event.service;

import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.event.persist.EventReportRepository;
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
    private final EventReportRepository eventReportRepository;
    private final AspspEventMapper eventBOMapper;

    @Override
    public List<AspspEvent> getEventsForPeriod(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @Nullable String instanceId,
                                               @Nullable Integer pageIndex, @Nullable Integer itemsPerPage) {
        List<ReportEvent> events = eventReportRepository.getEventsForPeriod(start, end, instanceId, pageIndex, itemsPerPage);
        return eventBOMapper.toAspspEventList(events);
    }

    @Override
    public List<AspspEvent> getEventsForPeriodAndConsentId(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull String consentId, @Nullable String instanceId,
                                                           @Nullable Integer pageIndex, @Nullable Integer itemsPerPage) {
        List<ReportEvent> result = eventReportRepository.getEventsForPeriodAndConsentId(start, end, consentId, instanceId, pageIndex, itemsPerPage);
        return eventBOMapper.toAspspEventList(result);
    }

    @Override
    public List<AspspEvent> getEventsForPeriodAndPaymentId(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull String paymentId, @Nullable String instanceId,
                                                           @Nullable Integer pageIndex, @Nullable Integer itemsPerPage) {
        List<ReportEvent> result = eventReportRepository.getEventsForPeriodAndPaymentId(start, end, paymentId, instanceId, pageIndex, itemsPerPage);
        return eventBOMapper.toAspspEventList(result);
    }

    @Override
    public List<AspspEvent> getEventsForPeriodAndEventType(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull EventType eventType, @Nullable String instanceId,
                                                           @Nullable Integer pageIndex, @Nullable Integer itemsPerPage) {
        List<ReportEvent> result = eventReportRepository.getEventsForPeriodAndEventType(start, end, eventType, instanceId, pageIndex, itemsPerPage);
        return eventBOMapper.toAspspEventList(result);
    }

    @Override
    public List<AspspEvent> getEventsForPeriodAndEventOrigin(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull EventOrigin eventOrigin, @Nullable String instanceId,
                                                             @Nullable Integer pageIndex, @Nullable Integer itemsPerPage) {
        List<ReportEvent> result = eventReportRepository.getEventsForPeriodAndEventOrigin(start, end, eventOrigin, instanceId, pageIndex, itemsPerPage);
        return eventBOMapper.toAspspEventList(result);
    }
}
