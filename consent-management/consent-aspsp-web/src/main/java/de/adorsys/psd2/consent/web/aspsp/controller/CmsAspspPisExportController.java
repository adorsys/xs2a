/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

import de.adorsys.psd2.consent.api.pis.CmsPayment;
import de.adorsys.psd2.consent.aspsp.api.pis.CmsAspspPisExportService;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "aspsp-api/v1/pis/payments")
@Api(value = "aspsp-api/v1/pis/payments", tags = "ASPSP Export, PIS", description = "Provides access to the consent management system for exporting PIS payments by ASPSP")
public class CmsAspspPisExportController {
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";

    private final CmsAspspPisExportService cmsAspspPisExportService;

    @GetMapping(path = "/tpp/{tpp-id}")
    @ApiOperation(value = "Returns a list of payments by given mandatory TPP ID, optional creation date, PSU ID Data and instance ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
    public ResponseEntity<Collection<CmsPayment>> getPaymentsByTpp(
        @ApiParam(value = "TPP ID", required = true, example = "12345987")
        @PathVariable("tpp-id") String tppId,
        @ApiParam(value = "Creation start date", example = "2010-01-01")
        @RequestHeader(value = "start-date", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @ApiParam(value = "Creation end date", example = "2030-01-01")
        @RequestHeader(value = "end-date", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
        @ApiParam(value = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's" +
                              " documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceeding AIS service in the same session. ")
        @RequestHeader(value = "psu-id", required = false) String psuId,
        @ApiParam(value = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility. ")
        @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @ApiParam(value = "ID of the particular service instance")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        PsuIdData psuIdData = new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType);
        Collection<CmsPayment> payments = cmsAspspPisExportService.exportPaymentsByTpp(tppId, start, end, psuIdData, instanceId);
        return new ResponseEntity<>(payments, HttpStatus.OK);
    }

    @GetMapping(path = "/psu")
    @ApiOperation(value = "Returns a list of payments by given mandatory PSU ID Data, optional creation date and instance ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
    public ResponseEntity<Collection<CmsPayment>> getPaymentsByPsu(
        @ApiParam(value = "Creation start date", example = "2010-01-01")
        @RequestHeader(value = "start-date", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @ApiParam(value = "Creation end date", example = "2030-01-01")
        @RequestHeader(value = "end-date", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
        @ApiParam(value = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceeding AIS service in the same session. ")
        @RequestHeader(value = "psu-id", required = false) String psuId,
        @ApiParam(value = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility. ")
        @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @ApiParam(value = "ID of the particular service instance")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        PsuIdData psuIdData = new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType);
        Collection<CmsPayment> payments = cmsAspspPisExportService.exportPaymentsByPsu(psuIdData, start, end, instanceId);
        return new ResponseEntity<>(payments, HttpStatus.OK);
    }

    @GetMapping(path = "/accounts/{account-id}")
    @ApiOperation(value = "Returns a list of payments by given mandatory aspsp account id, optional creation date and instance ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
    public ResponseEntity<Collection<CmsPayment>> getPaymentsByAccountId(
        @ApiParam(value = "Bank specific account identifier.", required = true, example = "11111-99999")
        @PathVariable("account-id") String aspspAccountId,
        @ApiParam(value = "Creation start date", example = "2010-01-01")
        @RequestHeader(value = "start-date", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @ApiParam(value = "Creation end date", example = "2030-01-01")
        @RequestHeader(value = "end-date", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
        @ApiParam(value = "ID of the particular service instance")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        Collection<CmsPayment> payments = cmsAspspPisExportService.exportPaymentsByAccountId(aspspAccountId, start, end, instanceId);
        return new ResponseEntity<>(payments, HttpStatus.OK);
    }
}
