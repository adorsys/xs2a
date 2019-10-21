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

package de.adorsys.psd2.consent.web.xs2a.controller;

import de.adorsys.psd2.consent.api.service.TppService;
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/tpp")
@Api(value = "api/v1/tpp", tags = "TPP", description = "Provides access to the TPP")
public class TppController {
    private final TppService tppService;
    private final TppStopListService tppStopListService;

    @PutMapping
    @ApiOperation(value = "Updates TPP Info")
    @ApiResponse(code = 200, message = "OK")
    public ResponseEntity<Boolean> updateTppInfo(@RequestBody TppInfo tppInfo) {
        return new ResponseEntity<>(tppService.updateTppInfo(tppInfo), HttpStatus.OK);
    }

    @GetMapping(path = "/stop-list")
    @ApiOperation(value = "Checks if TPP is blocked")
    @ApiResponse(code = 200, message = "OK")
    public ResponseEntity<Boolean> checkIfTppBlocked(
        @ApiParam(value = "ID of TPP", example = "12345987")
        @RequestHeader(value = "tpp-authorisation-number") String tppAuthorisationNumber) {

        boolean isTppBlocked = tppStopListService.checkIfTppBlocked(tppAuthorisationNumber);
        return new ResponseEntity<>(isTppBlocked, HttpStatus.OK);
    }
}
