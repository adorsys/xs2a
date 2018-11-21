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

package de.adorsys.psd2.consent.web.aspsp;

import de.adorsys.psd2.consent.aspsp.api.CmsAspspEventService;
import de.adorsys.psd2.xs2a.core.event.Event;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "aspsp-api/v1/events")
@Api(value = "aspsp-api/v1/events", tags = "ASPSP Events", description = "Provides access to the consent management system for ASPSP Events")
public class CmsAspspEventController {
    private final CmsAspspEventService cmsAspspEventService;

    @GetMapping(path = "/")
    @ApiOperation(value = "Returns a list of Event objects between two dates")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
    public ResponseEntity<List<Event>> getEventsForDates(
        @ApiParam(value = "Start date", example = "2010-01-01T00:00:00Z", required = true)
        @RequestHeader(value = "start-date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
        @ApiParam(value = "End date", example = "2030-01-01T00:00:00Z", required = true)
        @RequestHeader(value = "end-date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end) {
        List<Event> events = cmsAspspEventService.getEventsForPeriod(start, end);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }
}
