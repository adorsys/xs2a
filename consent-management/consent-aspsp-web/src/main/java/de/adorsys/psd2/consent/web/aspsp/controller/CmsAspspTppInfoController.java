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

import de.adorsys.psd2.consent.aspsp.api.tpp.CmsAspspTppService;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "aspsp-api/v1/tpp")
@Api(value = "aspsp-api/v1/tpp", tags = "ASPSP TPP Info", description = "Provides access to the consent management system TPP Info")
public class CmsAspspTppInfoController {
    private final CmsAspspTppService cmsAspspTppService;

    @GetMapping
    @ApiOperation(value = "Returns TPP info by TPP ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<TppInfo> getTppInfo(
        @ApiParam(value = "ID of TPP", required = true, example = "12345987")
        @RequestHeader(value = "tpp-authorisation-number") String tppAuthorisationNumber,
        @ApiParam(value = "Service instance id", example = "instance id")
        @RequestHeader(value = "instance-id", required = false, defaultValue = "UNDEFINED") String instanceId) {
        return cmsAspspTppService.getTppInfo(tppAuthorisationNumber, instanceId)
                   .map(record -> new ResponseEntity<>(record, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
