/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.report;

import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.event.persist.model.ReportEvent;
import de.adorsys.psd2.report.entity.EventReportEntity;
import de.adorsys.psd2.report.jpa.EventReportJPARepository;
import de.adorsys.psd2.report.mapper.EventReportDBMapper;
import de.adorsys.psd2.report.specification.EventEntityAttributeSpecificationProvider;
import de.adorsys.psd2.report.specification.EventSpecification;
import de.adorsys.psd2.report.util.EventPageRequestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventReportRepositoryImplTest {

    private static final String INSTANCE_ID = "bank-instance-id";
    private static final String CONSENT_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String PAYMENT_ID = "329d04d9-cb42-4fb5-8265-de58fa00366d";

    private static final OffsetDateTime START = OffsetDateTime.MIN;
    private static final OffsetDateTime END = OffsetDateTime.MIN.plusDays(10);
    private static final int PAGE_INDEX = 0;
    private static final int ITEMS_PER_PAGE = 1000;

    @InjectMocks
    private EventReportRepositoryImpl eventReportRepository;

    @Mock
    private EventReportDBMapper eventReportDBMapper;
    @Mock
    private EventReportJPARepository eventJpaRepository;
    @Mock
    private EventSpecification eventSpecification;
    @Mock
    private EventPageRequestBuilder pageRequestBuilder;

    @BeforeEach
    void init() {
        when(eventJpaRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(new PageImpl(Collections.singletonList(getEventReportEntity())));
        when(eventReportDBMapper.mapToAspspReportEvents(any(List.class)))
            .thenReturn(Collections.singletonList(getReportEvent()));
    }

    @Test
    void getEventsForPeriod_success() {
        // Given
        when(pageRequestBuilder.getPageable(PAGE_INDEX, ITEMS_PER_PAGE))
            .thenReturn(getPageable());
        when(eventSpecification.byPeriodAndInstanceId(START, END, INSTANCE_ID))
            .thenReturn(EventEntityAttributeSpecificationProvider.provideSpecificationForEntityAttribute("INSTANCE-ID", INSTANCE_ID));
        // When
        List<ReportEvent> actual = eventReportRepository.getEventsForPeriod(START, END, INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);

        // Then
        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertEquals(1L, actual.get(0).getId());
    }

    @Test
    void getEventsForPeriodAndConsentId_success() {
        // Given
        when(pageRequestBuilder.getPageable(PAGE_INDEX, ITEMS_PER_PAGE))
            .thenReturn(getPageable());
        when(eventSpecification.byPeriodAndInstanceIdAndConsentId(START, END, INSTANCE_ID, CONSENT_ID))
            .thenReturn(EventEntityAttributeSpecificationProvider.provideSpecificationForEntityAttribute("INSTANCE-ID", INSTANCE_ID));
        // When
        List<ReportEvent> actual = eventReportRepository.getEventsForPeriodAndConsentId(START, END, CONSENT_ID, INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);

        // Then
        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertEquals(1L, actual.get(0).getId());
    }

    @Test
    void getEventsForPeriodAndPaymentId_success() {
        // Given
        when(pageRequestBuilder.getPageable(PAGE_INDEX, ITEMS_PER_PAGE))
            .thenReturn(getPageable());
        when(eventSpecification.byPeriodAndInstanceIdAndPaymentId(START, END, INSTANCE_ID, PAYMENT_ID))
            .thenReturn(EventEntityAttributeSpecificationProvider.provideSpecificationForEntityAttribute("INSTANCE-ID", INSTANCE_ID));
        // When
        List<ReportEvent> actual = eventReportRepository.getEventsForPeriodAndPaymentId(START, END, PAYMENT_ID, INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);

        // Then
        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertEquals(1L, actual.get(0).getId());
    }

    @Test
    void getEventsForPeriodAndEventType_success() {
        // Given
        when(pageRequestBuilder.getPageable(PAGE_INDEX, ITEMS_PER_PAGE))
            .thenReturn(getPageable());
        when(eventSpecification.byPeriodAndInstanceIdAndEventType(START, END, INSTANCE_ID, EventType.CREATE_AIS_CONSENT_REQUEST_RECEIVED))
            .thenReturn(EventEntityAttributeSpecificationProvider.provideSpecificationForEntityAttribute("INSTANCE-ID", INSTANCE_ID));
        // When
        List<ReportEvent> actual = eventReportRepository.getEventsForPeriodAndEventType(START, END, EventType.CREATE_AIS_CONSENT_REQUEST_RECEIVED, INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);

        // Then
        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertEquals(1L, actual.get(0).getId());
    }

    @Test
    void getEventsForPeriodAndEventOrigin_success() {
        // Given
        when(pageRequestBuilder.getPageable(PAGE_INDEX, ITEMS_PER_PAGE))
            .thenReturn(getPageable());
        when(eventSpecification.byPeriodAndInstanceIdAndEventOrigin(START, END, INSTANCE_ID, EventOrigin.TPP))
            .thenReturn(EventEntityAttributeSpecificationProvider.provideSpecificationForEntityAttribute("INSTANCE-ID", INSTANCE_ID));
        // When
        List<ReportEvent> actual = eventReportRepository.getEventsForPeriodAndEventOrigin(START, END, EventOrigin.TPP, INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);

        // Then
        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertEquals(1L, actual.get(0).getId());
    }

    private Pageable getPageable() {
        return PageRequest.of(PAGE_INDEX, ITEMS_PER_PAGE);
    }

    private EventReportEntity getEventReportEntity() {
        EventReportEntity entity = new EventReportEntity();
        entity.setEventType(EventType.CREATE_AIS_CONSENT_REQUEST_RECEIVED);
        entity.setId(1L);

        return entity;
    }

    private ReportEvent getReportEvent() {
        ReportEvent reportEvent = new ReportEvent();
        reportEvent.setEventType(EventType.CREATE_AIS_CONSENT_REQUEST_RECEIVED);
        reportEvent.setId(1L);

        return reportEvent;
    }
}
