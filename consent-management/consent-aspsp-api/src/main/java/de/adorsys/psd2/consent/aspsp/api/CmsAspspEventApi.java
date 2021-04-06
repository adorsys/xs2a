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

package de.adorsys.psd2.consent.aspsp.api;

import de.adorsys.psd2.consent.api.CmsConstant;
import de.adorsys.psd2.consent.aspsp.api.config.CmsAspspApiTagName;
import de.adorsys.psd2.event.service.model.AspspEvent;
import io.swagger.annotations.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

import static de.adorsys.psd2.consent.aspsp.api.config.CmsPsuApiDefaultValue.DEFAULT_SERVICE_INSTANCE_ID;

@RequestMapping(path = "aspsp-api/v1/events")
@Api(value = "aspsp-api/v1/events", tags = CmsAspspApiTagName.ASPSP_EVENTS)
public interface CmsAspspEventApi {

    @GetMapping(path = "/")
    @ApiOperation(value = "Returns a list of Event objects between two dates")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
    ResponseEntity<List<AspspEvent>> getEventsForDates(
        @ApiParam(value = "Start date", example = "2010-01-01T00:00:00Z", required = true)
        @RequestHeader(value = "start-date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
        @ApiParam(value = "End date", example = "2030-01-01T00:00:00Z", required = true)
        @RequestHeader(value = "end-date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end,
        @ApiParam(value = "Bank instance ID")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @ApiParam(value = "Index of current page", example = "0")
        @RequestParam(value = CmsConstant.QUERY.PAGE_INDEX, defaultValue = "0") Integer pageIndex,
        @ApiParam(value = "Quantity of consents on one page", example = "20")
        @RequestParam(value = CmsConstant.QUERY.ITEMS_PER_PAGE, defaultValue = "20") Integer itemsPerPage);

    @GetMapping(path = "/consent/{consent-id}")
    @ApiOperation(value = "Returns a list of Event objects between two dates")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
    ResponseEntity<List<AspspEvent>> getEventsForDatesAndConsentId(
        @ApiParam(name = CmsConstant.PATH.CONSENT_ID, value = "The consent identifier", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7", required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @ApiParam(value = "Start date", example = "2010-01-01T00:00:00Z", required = true)
        @RequestHeader(value = "start-date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
        @ApiParam(value = "End date", example = "2030-01-01T00:00:00Z", required = true)
        @RequestHeader(value = "end-date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end,
        @ApiParam(value = "Bank instance ID")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @ApiParam(value = "Index of current page", example = "0")
        @RequestParam(value = CmsConstant.QUERY.PAGE_INDEX, defaultValue = "0") Integer pageIndex,
        @ApiParam(value = "Quantity of consents on one page", example = "20")
        @RequestParam(value = CmsConstant.QUERY.ITEMS_PER_PAGE, defaultValue = "20") Integer itemsPerPage);

    @GetMapping(path = "/payment/{payment-id}")
    @ApiOperation(value = "Returns a list of Event objects between two dates")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
    ResponseEntity<List<AspspEvent>> getEventsForDatesAndPaymentId(
        @ApiParam(name = "payment-id", value = "The payment identification assigned to the created payment.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7", required = true)
        @PathVariable("payment-id") String paymentId,
        @ApiParam(value = "Start date", example = "2010-01-01T00:00:00Z", required = true)
        @RequestHeader(value = "start-date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
        @ApiParam(value = "End date", example = "2030-01-01T00:00:00Z", required = true)
        @RequestHeader(value = "end-date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end,
        @ApiParam(value = "Bank instance ID")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @ApiParam(value = "Index of current page", example = "0")
        @RequestParam(value = CmsConstant.QUERY.PAGE_INDEX, defaultValue = "0") Integer pageIndex,
        @ApiParam(value = "Quantity of consents on one page", example = "20")
        @RequestParam(value = CmsConstant.QUERY.ITEMS_PER_PAGE, defaultValue = "20") Integer itemsPerPage);

    @GetMapping(path = "/type/{event-type}")
    @ApiOperation(value = "Returns a list of Event objects between two dates")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
    ResponseEntity<List<AspspEvent>> getEventsForDatesAndEventType(
        @ApiParam(name = "event-type", value = "The type of event", example = "CREATE_SIGNING_BASKET_REQUEST_RECEIVED", required = true)
        @PathVariable("event-type") String eventType,
        @ApiParam(value = "Start date", example = "2010-01-01T00:00:00Z", required = true)
        @RequestHeader(value = "start-date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
        @ApiParam(value = "End date", example = "2030-01-01T00:00:00Z", required = true)
        @RequestHeader(value = "end-date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end,
        @ApiParam(value = "Bank instance ID")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @ApiParam(value = "Index of current page", example = "0")
        @RequestParam(value = CmsConstant.QUERY.PAGE_INDEX, defaultValue = "0") Integer pageIndex,
        @ApiParam(value = "Quantity of consents on one page", example = "20")
        @RequestParam(value = CmsConstant.QUERY.ITEMS_PER_PAGE, defaultValue = "20") Integer itemsPerPage);

    @GetMapping(path = "/origin/{event-origin}")
    @ApiOperation(value = "Returns a list of Event objects between two dates")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
    ResponseEntity<List<AspspEvent>> getEventsForDatesAndEventOrigin(
        @ApiParam(name = "event-origin", value = "The origin of event", example = "ASPSP", required = true)
        @PathVariable("event-origin") String eventOrigin,
        @ApiParam(value = "Start date", example = "2010-01-01T00:00:00Z", required = true)
        @RequestHeader(value = "start-date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
        @ApiParam(value = "End date", example = "2030-01-01T00:00:00Z", required = true)
        @RequestHeader(value = "end-date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end,
        @ApiParam(value = "Bank instance ID")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @ApiParam(value = "Index of current page", example = "0")
        @RequestParam(value = CmsConstant.QUERY.PAGE_INDEX, defaultValue = "0") Integer pageIndex,
        @ApiParam(value = "Quantity of consents on one page", example = "20")
        @RequestParam(value = CmsConstant.QUERY.ITEMS_PER_PAGE, defaultValue = "20") Integer itemsPerPage);
}
