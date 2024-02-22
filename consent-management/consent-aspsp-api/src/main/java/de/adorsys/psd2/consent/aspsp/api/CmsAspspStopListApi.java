/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.consent.aspsp.api;

import de.adorsys.psd2.consent.aspsp.api.config.CmsAspspApiTagName;
import de.adorsys.psd2.xs2a.core.tpp.TppStopListRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static de.adorsys.psd2.consent.aspsp.api.config.CmsPsuApiDefaultValue.DEFAULT_SERVICE_INSTANCE_ID;

@RequestMapping(path = "aspsp-api/v1/tpp/stop-list")
@Tag(name = CmsAspspApiTagName.ASPSP_TPP_STOP_LIST, description = "Provides access to the consent management system TPP Stop List")
public interface CmsAspspStopListApi {

    @GetMapping
    @Operation(description = "Returns TPP stop list record by TPP authorisation number")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not Found")})
    ResponseEntity<TppStopListRecord> getTppStopListRecord(
        @Parameter(description = "ID of TPP", example = "PSDDE-FAKENCA-87B2AC")
        @RequestHeader(value = "tpp-authorisation-number") String tppAuthorisationNumber,
        @Parameter(description = "Service instance ID", example = "instance id")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId);

    @PutMapping(path = "/block")
    @Operation(description = "Blocks TPP by TPP authorisation number and lock period")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<Boolean> blockTpp(
        @Parameter(description = "ID of TPP", example = "PSDDE-FAKENCA-87B2AC")
        @RequestHeader(value = "tpp-authorisation-number") String tppAuthorisationNumber,
        @Parameter(description = "Service instance ID", example = "instance id")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @Parameter(description = "Period of TPP locking (in milliseconds)", example = "1000")
        @RequestHeader(value = "lock-period", required = false) Long lockPeriod);

    @DeleteMapping(path = "/unblock")
    @Operation(description = "Unblocks TPP by TPP authorisation number")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<Boolean> unblockTpp(
        @Parameter(description = "ID of TPP", example = "PSDDE-FAKENCA-87B2AC")
        @RequestHeader(value = "tpp-authorisation-number") String tppAuthorisationNumber,
        @Parameter(description = "Service instance ID", example = "instance id")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId);
}
