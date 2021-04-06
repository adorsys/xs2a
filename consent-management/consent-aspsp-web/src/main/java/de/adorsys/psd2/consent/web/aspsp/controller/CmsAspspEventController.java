/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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
