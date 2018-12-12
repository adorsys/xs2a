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

package de.adorsys.psd2.consent.web.xs2a.controller;

import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.xs2a.core.tpp.TppUniqueParamsHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/tpp/stop-list")
@Api(value = "api/v1/tpp/stop-list", tags = "TPP Stop List", description = "Provides access to the TPP stop list")
public class TppStopListController {
    private final TppStopListService tppStopListService;

    @GetMapping
    @ApiOperation(value = "Checks if TPP is blocked")
    @ApiResponse(code = 200, message = "OK")
    public ResponseEntity<Boolean> checkIfTppBlocked(
        @ApiParam(value = "ID of TPP to load", example = "12345987")
        @RequestHeader(value = "tpp-authorisation-number") String tppAuthorisationNumber,
        @ApiParam(value = "National competent authority id", example = "authority id")
        @RequestHeader(value = "authority-id") String nationalAuthorityId) {
        TppUniqueParamsHolder tppUniqueParams = new TppUniqueParamsHolder(tppAuthorisationNumber, nationalAuthorityId);

        boolean isTppBlocked = tppStopListService.checkIfTppBlocked(tppUniqueParams);
        return new ResponseEntity<>(isTppBlocked, HttpStatus.OK);
    }
}
