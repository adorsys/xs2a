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

package de.adorsys.psd2.consent.web.aspsp.controller;

import de.adorsys.psd2.consent.aspsp.api.CmsAspspEventApi;
import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.event.service.AspspEventService;
import de.adorsys.psd2.event.service.model.AspspEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CmsAspspEventController implements CmsAspspEventApi {
    private final AspspEventService aspspEventService;

    @Override
    public ResponseEntity<List<AspspEvent>> getEventsForDates(OffsetDateTime start, OffsetDateTime end, String instanceId,
                                                              Integer pageIndex, Integer itemsPerPage) {
        List<AspspEvent> events = aspspEventService.getEventsForPeriod(start, end, instanceId, pageIndex, itemsPerPage);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<AspspEvent>> getEventsForDatesAndConsentId(String consentId, OffsetDateTime start, OffsetDateTime end, String instanceId, Integer pageIndex, Integer itemsPerPage) {
        List<AspspEvent> events = aspspEventService.getEventsForPeriodAndConsentId(start, end, consentId, instanceId, pageIndex, itemsPerPage);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<AspspEvent>> getEventsForDatesAndPaymentId(String paymentId, OffsetDateTime start, OffsetDateTime end, String instanceId, Integer pageIndex, Integer itemsPerPage) {
        List<AspspEvent> events = aspspEventService.getEventsForPeriodAndPaymentId(start, end, paymentId, instanceId, pageIndex, itemsPerPage);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<AspspEvent>> getEventsForDatesAndEventType(String eventType, OffsetDateTime start, OffsetDateTime end, String instanceId, Integer pageIndex, Integer itemsPerPage) {
        EventType eventTypeValue;
        try {
            eventTypeValue = EventType.valueOf(eventType);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        List<AspspEvent> events = aspspEventService.getEventsForPeriodAndEventType(start, end, eventTypeValue, instanceId, pageIndex, itemsPerPage);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<AspspEvent>> getEventsForDatesAndEventOrigin(String eventOrigin, OffsetDateTime start, OffsetDateTime end, String instanceId, Integer pageIndex, Integer itemsPerPage) {
        EventOrigin eventOriginValue;
        try {
            eventOriginValue = EventOrigin.valueOf(eventOrigin);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        List<AspspEvent> events = aspspEventService.getEventsForPeriodAndEventOrigin(start, end, eventOriginValue, instanceId, pageIndex, itemsPerPage);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }
}
