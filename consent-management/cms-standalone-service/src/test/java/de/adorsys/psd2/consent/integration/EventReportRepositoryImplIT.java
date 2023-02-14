/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.integration;

import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.integration.config.IntegrationTestConfiguration;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.event.persist.EventReportRepository;
import de.adorsys.psd2.event.persist.EventRepository;
import de.adorsys.psd2.event.persist.model.EventPO;
import de.adorsys.psd2.event.persist.model.ReportEvent;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = IntegrationTestConfiguration.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application-integration-test.properties")
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
class EventReportRepositoryImplIT {
    private static final String INSTANCE_ID = "3de76f19-1df7-44d8-b760-ca972d2f945c";
    private static final String CONSENT_ID = "fa6e687b-1ac9-4b1a-9c74-357c35c82ba1";
    private static final String PAYMENT_ID = "j-t4XyLJTzQkonfSTnyxIMc";
    private static final OffsetDateTime START = OffsetDateTime.parse("2019-07-09T12:29:50.042136Z");
    private static final OffsetDateTime END = OffsetDateTime.parse("2019-07-09T14:29:50.042136Z");

    @Autowired
    private EventReportRepository repository;

    @Autowired
    private ConsentJpaRepository consentJpaRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private PisCommonPaymentDataRepository pisCommonPaymentDataRepository;

    private ReportEvent expectedEvent;
    private ReportEvent expectedPaymentEvent;

    private final JsonReader jsonReader = new JsonReader();

    @BeforeAll
    void setUp() {
        ConsentEntity consentEntity = jsonReader.getObjectFromFile("json/consent/integration/ais/consent-entity.json", ConsentEntity.class);
        consentJpaRepository.save(consentEntity);

        PisCommonPaymentData pisCommonPaymentData = jsonReader.getObjectFromFile("json/consent/integration/pis/common-payment-data.json", PisCommonPaymentData.class);
        pisCommonPaymentData.getPayments().forEach(p -> p.setPaymentData(pisCommonPaymentData));

        pisCommonPaymentDataRepository.save(pisCommonPaymentData);

        EventPO eventPO = jsonReader.getObjectFromFile("json/event.json", EventPO.class);
        expectedEvent = buildReportEvent(eventPO, 1L);
        eventRepository.save(eventPO);

        EventPO eventPaymentPO = jsonReader.getObjectFromFile("json/event-payment.json", EventPO.class);
        expectedPaymentEvent = buildReportEvent(eventPaymentPO, 2L);
        eventRepository.save(eventPaymentPO);
    }

    @Test
    void getEventsForPeriod() {
        List<ReportEvent> eventsForPeriod = repository.getEventsForPeriod(START, END, INSTANCE_ID, 0, 20);

        assertNotNull(eventsForPeriod);
        assertEquals(2, eventsForPeriod.size());
        assertEquals(expectedEvent, updateToUTC(eventsForPeriod.get(0)));
    }

    @Test
    void getEventsForPeriodAndConsentId() {
        List<ReportEvent> eventsForPeriod = repository.getEventsForPeriodAndConsentId(START, END, CONSENT_ID, INSTANCE_ID, 0, 20);

        assertNotNull(eventsForPeriod);
        assertEquals(1, eventsForPeriod.size());
        assertEquals(expectedEvent, updateToUTC(eventsForPeriod.get(0)));
    }

    @Test
    void getEventsForPeriodAndPaymentId() {
        List<ReportEvent> eventsForPeriod = repository.getEventsForPeriodAndPaymentId(START, END, PAYMENT_ID, INSTANCE_ID, 0, 20);

        assertNotNull(eventsForPeriod);
        assertEquals(1, eventsForPeriod.size());
        assertEquals(expectedPaymentEvent, updateToUTC(eventsForPeriod.get(0)));
    }

    @Test
    void getEventsForPeriodAndEventOrigin() {
        List<ReportEvent> eventsForPeriod = repository.getEventsForPeriodAndEventOrigin(START, END, EventOrigin.TPP, INSTANCE_ID, 0, 20);

        assertNotNull(eventsForPeriod);
        assertEquals(2, eventsForPeriod.size());
        assertEquals(expectedEvent, updateToUTC(eventsForPeriod.get(0)));
    }

    @Test
    void getEventsForPeriodAndEventType() {
        List<ReportEvent> eventsForPeriod = repository.getEventsForPeriodAndEventType(START, END, EventType.PAYMENT_INITIATION_REQUEST_RECEIVED, INSTANCE_ID, 0, 20);

        assertNotNull(eventsForPeriod);
        assertEquals(2, eventsForPeriod.size());
        assertEquals(expectedEvent, updateToUTC(eventsForPeriod.get(0)));
    }

    private ReportEvent updateToUTC(ReportEvent reportEvent) {
        reportEvent.setTimestamp(reportEvent.getTimestamp().withOffsetSameInstant(ZoneOffset.UTC));
        return reportEvent;
    }

    private ReportEvent buildReportEvent(EventPO eventPO, Long id) {
        ReportEvent reportEvent = new ReportEvent();
        reportEvent.setId(id);
        reportEvent.setTimestamp(eventPO.getTimestamp());
        reportEvent.setConsentId(eventPO.getConsentId());
        reportEvent.setPaymentId(eventPO.getPaymentId());
        reportEvent.setPayload(eventPO.getPayload());
        reportEvent.setEventOrigin(eventPO.getEventOrigin());
        reportEvent.setEventType(eventPO.getEventType());
        reportEvent.setInstanceId(eventPO.getInstanceId());
        reportEvent.setTppAuthorisationNumber(eventPO.getTppAuthorisationNumber());
        reportEvent.setXRequestId(eventPO.getXRequestId());
        reportEvent.setPsuIdData(Collections.singleton((eventPO.getPsuIdData())));
        reportEvent.setInternalRequestId(eventPO.getInternalRequestId());
        return reportEvent;
    }
}
