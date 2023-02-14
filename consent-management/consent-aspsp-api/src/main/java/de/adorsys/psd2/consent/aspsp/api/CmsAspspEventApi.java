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

package de.adorsys.psd2.consent.aspsp.api;

import de.adorsys.psd2.consent.api.CmsConstant;
import de.adorsys.psd2.consent.aspsp.api.config.CmsAspspApiTagName;
import de.adorsys.psd2.event.service.model.AspspEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

import static de.adorsys.psd2.consent.aspsp.api.config.CmsPsuApiDefaultValue.DEFAULT_SERVICE_INSTANCE_ID;

@RequestMapping(path = "aspsp-api/v1/events")
@Tag(name = CmsAspspApiTagName.ASPSP_EVENTS, description = "Provides access to the consent management system for ASPSP Events")
public interface CmsAspspEventApi {

    @GetMapping(path = "/")
    @Operation(description = "Returns a list of Event objects between two dates")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK")})
    ResponseEntity<List<AspspEvent>> getEventsForDates(
        @Parameter(description = "Start date", example = "2010-01-01T00:00:00Z", required = true)
        @RequestHeader(value = "start-date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
        @Parameter(description = "End date", example = "2030-01-01T00:00:00Z", required = true)
        @RequestHeader(value = "end-date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end,
        @Parameter(description = "Bank instance ID")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @Parameter(description = "Index of current page", example = "0")
        @RequestParam(value = CmsConstant.QUERY.PAGE_INDEX, defaultValue = "0") Integer pageIndex,
        @Parameter(description = "Quantity of consents on one page", example = "20")
        @RequestParam(value = CmsConstant.QUERY.ITEMS_PER_PAGE, defaultValue = "20") Integer itemsPerPage);

    @GetMapping(path = "/consent/{consent-id}")
    @Operation(description = "Returns a list of Event objects between two dates")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK")})
    ResponseEntity<List<AspspEvent>> getEventsForDatesAndConsentId(
        @Parameter(name = CmsConstant.PATH.CONSENT_ID, description = "The consent identifier", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7", required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @Parameter(description = "Start date", example = "2010-01-01T00:00:00Z", required = true)
        @RequestHeader(value = "start-date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
        @Parameter(description = "End date", example = "2030-01-01T00:00:00Z", required = true)
        @RequestHeader(value = "end-date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end,
        @Parameter(description = "Bank instance ID")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @Parameter(description = "Index of current page", example = "0")
        @RequestParam(value = CmsConstant.QUERY.PAGE_INDEX, defaultValue = "0") Integer pageIndex,
        @Parameter(description = "Quantity of consents on one page", example = "20")
        @RequestParam(value = CmsConstant.QUERY.ITEMS_PER_PAGE, defaultValue = "20") Integer itemsPerPage);

    @GetMapping(path = "/payment/{payment-id}")
    @Operation(description = "Returns a list of Event objects between two dates")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK")})
    ResponseEntity<List<AspspEvent>> getEventsForDatesAndPaymentId(
        @Parameter(name = "payment-id", description = "The payment identification assigned to the created payment.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7", required = true)
        @PathVariable("payment-id") String paymentId,
        @Parameter(description = "Start date", example = "2010-01-01T00:00:00Z", required = true)
        @RequestHeader(value = "start-date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
        @Parameter(description = "End date", example = "2030-01-01T00:00:00Z", required = true)
        @RequestHeader(value = "end-date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end,
        @Parameter(description = "Bank instance ID")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @Parameter(description = "Index of current page", example = "0")
        @RequestParam(value = CmsConstant.QUERY.PAGE_INDEX, defaultValue = "0") Integer pageIndex,
        @Parameter(description = "Quantity of consents on one page", example = "20")
        @RequestParam(value = CmsConstant.QUERY.ITEMS_PER_PAGE, defaultValue = "20") Integer itemsPerPage);

    @GetMapping(path = "/type/{event-type}")
    @Operation(description = "Returns a list of Event objects between two dates")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK")})
    ResponseEntity<List<AspspEvent>> getEventsForDatesAndEventType(
        @Parameter(name = "event-type", description = "The type of event", example = "CREATE_SIGNING_BASKET_REQUEST_RECEIVED", required = true)
        @PathVariable("event-type") String eventType,
        @Parameter(description = "Start date", example = "2010-01-01T00:00:00Z", required = true)
        @RequestHeader(value = "start-date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
        @Parameter(description = "End date", example = "2030-01-01T00:00:00Z", required = true)
        @RequestHeader(value = "end-date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end,
        @Parameter(description = "Bank instance ID")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @Parameter(description = "Index of current page", example = "0")
        @RequestParam(value = CmsConstant.QUERY.PAGE_INDEX, defaultValue = "0") Integer pageIndex,
        @Parameter(description = "Quantity of consents on one page", example = "20")
        @RequestParam(value = CmsConstant.QUERY.ITEMS_PER_PAGE, defaultValue = "20") Integer itemsPerPage);

    @GetMapping(path = "/origin/{event-origin}")
    @Operation(description = "Returns a list of Event objects between two dates")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK")})
    ResponseEntity<List<AspspEvent>> getEventsForDatesAndEventOrigin(
        @Parameter(name = "event-origin", description = "The origin of event", example = "ASPSP", required = true)
        @PathVariable("event-origin") String eventOrigin,
        @Parameter(description = "Start date", example = "2010-01-01T00:00:00Z", required = true)
        @RequestHeader(value = "start-date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
        @Parameter(description = "End date", example = "2030-01-01T00:00:00Z", required = true)
        @RequestHeader(value = "end-date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end,
        @Parameter(description = "Bank instance ID")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @Parameter(description = "Index of current page", example = "0")
        @RequestParam(value = CmsConstant.QUERY.PAGE_INDEX, defaultValue = "0") Integer pageIndex,
        @Parameter(description = "Quantity of consents on one page", example = "20")
        @RequestParam(value = CmsConstant.QUERY.ITEMS_PER_PAGE, defaultValue = "20") Integer itemsPerPage);
}
