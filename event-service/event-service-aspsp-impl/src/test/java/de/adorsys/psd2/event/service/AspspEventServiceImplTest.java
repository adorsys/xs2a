/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
