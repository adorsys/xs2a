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
import de.adorsys.psd2.event.service.mapper.AspspEventMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AspspEventServiceImplTest {
    private static final OffsetDateTime START = OffsetDateTime.now().minusHours(1);
    private static final OffsetDateTime END = OffsetDateTime.now().plusHours(1);
    private static final String INSTANCE_ID = "3de76f19-1df7-44d8-b760-ca972d2f945c";
    private static final String CONSENT_ID = "fa6e687b-1ac9-4b1a-9c74-357c35c82ba1";
    private static final String PAYMENT_ID = "j-t4XyLJTzQkonfSTnyxIMc";

    @InjectMocks
    private AspspEventServiceImpl aspspEventService;

    @Mock
    private EventReportRepository eventReportRepository;
    @Spy
    private final AspspEventMapper mapper = Mappers.getMapper(AspspEventMapper.class);

    @Test
    void getEventsForPeriod() {
        when(eventReportRepository.getEventsForPeriod(START, END, INSTANCE_ID, null, null)).thenReturn(Collections.emptyList());

        aspspEventService.getEventsForPeriod(START, END, INSTANCE_ID, null, null);

        verify(eventReportRepository, times(1)).getEventsForPeriod(START, END, INSTANCE_ID, null, null);
    }

    @Test
    void getEventsForPeriodAndConsentId() {
        when(eventReportRepository.getEventsForPeriodAndConsentId(START, END, CONSENT_ID, INSTANCE_ID, null, null)).thenReturn(Collections.emptyList());

        aspspEventService.getEventsForPeriodAndConsentId(START, END, CONSENT_ID, INSTANCE_ID, null, null);

        verify(eventReportRepository, times(1)).getEventsForPeriodAndConsentId(START, END, CONSENT_ID, INSTANCE_ID, null, null);
    }

    @Test
    void getEventsForPeriodAndPaymentId() {
        when(eventReportRepository.getEventsForPeriodAndPaymentId(START, END, PAYMENT_ID, INSTANCE_ID, null, null)).thenReturn(Collections.emptyList());

        aspspEventService.getEventsForPeriodAndPaymentId(START, END, PAYMENT_ID, INSTANCE_ID, null, null);

        verify(eventReportRepository, times(1)).getEventsForPeriodAndPaymentId(START, END, PAYMENT_ID, INSTANCE_ID, null, null);
    }

    @Test
    void getEventsForPeriodAndEventOrigin() {
        when(eventReportRepository.getEventsForPeriodAndEventOrigin(START, END, EventOrigin.ASPSP, INSTANCE_ID, null, null)).thenReturn(Collections.emptyList());

        aspspEventService.getEventsForPeriodAndEventOrigin(START, END, EventOrigin.ASPSP, INSTANCE_ID, null, null);

        verify(eventReportRepository, times(1)).getEventsForPeriodAndEventOrigin(START, END, EventOrigin.ASPSP, INSTANCE_ID, null, null);
    }

    @Test
    void getEventsForPeriodAndEventType() {
        when(eventReportRepository.getEventsForPeriodAndEventType(START, END, EventType.CREATE_AIS_CONSENT_REQUEST_RECEIVED, INSTANCE_ID, null, null)).thenReturn(Collections.emptyList());

        aspspEventService.getEventsForPeriodAndEventType(START, END, EventType.CREATE_AIS_CONSENT_REQUEST_RECEIVED, INSTANCE_ID, null, null);

        verify(eventReportRepository, times(1)).getEventsForPeriodAndEventType(START, END, EventType.CREATE_AIS_CONSENT_REQUEST_RECEIVED, INSTANCE_ID, null, null);
    }
}
