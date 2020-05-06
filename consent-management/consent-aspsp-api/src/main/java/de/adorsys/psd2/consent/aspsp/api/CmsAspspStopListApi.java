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

import de.adorsys.psd2.consent.aspsp.api.config.CmsAspspApiTagName;
import de.adorsys.psd2.xs2a.core.tpp.TppStopListRecord;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static de.adorsys.psd2.consent.aspsp.api.config.CmsPsuApiDefaultValue.DEFAULT_SERVICE_INSTANCE_ID;

@RequestMapping(path = "aspsp-api/v1/tpp/stop-list")
@Api(value = "aspsp-api/v1/tpp/stop-list", tags = CmsAspspApiTagName.ASPSP_TPP_STOP_LIST)
public interface CmsAspspStopListApi {

    @GetMapping
    @ApiOperation(value = "Returns TPP stop list record by TPP authorisation number")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    ResponseEntity<TppStopListRecord> getTppStopListRecord(
        @ApiParam(value = "ID of TPP", example = "12345987")
        @RequestHeader(value = "tpp-authorisation-number") String tppAuthorisationNumber,
        @ApiParam(value = "Service instance id", example = "instance id")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId);

    @PutMapping(path = "/block")
    @ApiOperation(value = "Blocks TPP by TPP authorisation number and lock period")
    @ApiResponse(code = 200, message = "OK")
    ResponseEntity<Boolean> blockTpp(
        @ApiParam(value = "ID of TPP", example = "12345987")
        @RequestHeader(value = "tpp-authorisation-number") String tppAuthorisationNumber,
        @ApiParam(value = "Service instance id", example = "instance id")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @ApiParam(value = "Period of TPP locking (in milliseconds)", example = "1000")
        @RequestHeader(value = "lock-period", required = false) Long lockPeriod);

    @DeleteMapping(path = "/unblock")
    @ApiOperation(value = "Unblocks TPP by TPP authorisation number")
    @ApiResponse(code = 200, message = "OK")
    ResponseEntity<Boolean> unblockTpp(
        @ApiParam(value = "ID of TPP", example = "12345987")
        @RequestHeader(value = "tpp-authorisation-number") String tppAuthorisationNumber,
        @ApiParam(value = "Service instance id", example = "instance id")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId);
}
