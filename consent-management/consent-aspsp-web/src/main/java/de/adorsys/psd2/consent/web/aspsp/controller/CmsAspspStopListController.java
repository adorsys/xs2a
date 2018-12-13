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

import de.adorsys.psd2.consent.aspsp.api.tpp.CmsAspspTppService;
import de.adorsys.psd2.xs2a.core.tpp.TppStopListRecord;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "aspsp-api/v1/tpp/stop-list")
@Api(value = "aspsp-api/v1/tpp/stop-list", tags = "ASPSP TPP Stop List", description = "Provides access to the consent management system TPP Stop List")
public class CmsAspspStopListController {
    private final CmsAspspTppService cmsAspspTppService;

    @GetMapping
    @ApiOperation(value = "Returns TPP stop list record by TPP authorisation number and national authority ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<TppStopListRecord> getTppStopListRecord(
        @ApiParam(value = "ID of TPP to load", example = "12345987")
        @RequestHeader(value = "tpp-authorisation-number") String tppAuthorisationNumber,
        @ApiParam(value = "National competent authority id", example = "authority id")
        @RequestHeader(value = "authority-id") String nationalAuthorityId) {
        return cmsAspspTppService.getTppStopListRecord(tppAuthorisationNumber, nationalAuthorityId)
                   .map(record -> new ResponseEntity<>(record, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping(path = "/block")
    @ApiOperation(value = "Blocks TPP by TPP authorisation number, national authority ID and lock period")
    @ApiResponse(code = 200, message = "OK")
    public ResponseEntity<Boolean> blockTpp(
        @ApiParam(value = "ID of TPP to load", example = "12345987")
        @RequestHeader(value = "tpp-authorisation-number") String tppAuthorisationNumber,
        @ApiParam(value = "National competent authority id", example = "authority id")
        @RequestHeader(value = "authority-id") String nationalAuthorityId,
        @ApiParam(value = "Period of TPP locking (in milliseconds)", example = "1000")
        @RequestHeader(value = "lock-period", required = false) Long lockPeriod) {
        Duration lockPeriodDuration = lockPeriod != null ? Duration.ofMillis(lockPeriod) : null;
        boolean isBlocked = cmsAspspTppService.blockTpp(tppAuthorisationNumber, nationalAuthorityId, lockPeriodDuration);
        return new ResponseEntity<>(isBlocked, HttpStatus.OK);
    }

    @DeleteMapping(path = "/unblock")
    @ApiOperation(value = "Unblocks TPP by TPP authorisation number and national authority ID")
    @ApiResponse(code = 200, message = "OK")
    public ResponseEntity<Boolean> unblockTpp(
        @ApiParam(value = "ID of TPP to load", example = "12345987")
        @RequestHeader(value = "tpp-authorisation-number") String tppAuthorisationNumber,
        @ApiParam(value = "National competent authority id", example = "authority id")
        @RequestHeader(value = "authority-id") String nationalAuthorityId) {
        boolean isUnblocked = cmsAspspTppService.unblockTpp(tppAuthorisationNumber, nationalAuthorityId);
        return new ResponseEntity<>(isUnblocked, HttpStatus.OK);
    }
}
