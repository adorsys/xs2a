/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.api;

import de.adorsys.psd2.consent.api.config.InternalCmsXs2aApiTagName;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(path = "api/v1/tpp")
@Tag(name = InternalCmsXs2aApiTagName.TPP, description = "Provides access to the TPP")
public interface TppApi {

    @PutMapping
    @Operation(description = "Updates TPP Info")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<Boolean> updateTppInfo(@RequestBody TppInfo tppInfo);

    @GetMapping(path = "/stop-list")
    @Operation(description = "Checks if TPP is blocked")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<Boolean> checkIfTppBlocked(
        @Parameter(description = "ID of TPP", example = "12345987")
        @RequestHeader(value = "tpp-authorisation-number") String tppAuthorisationNumber,
        @Parameter(description = "ID of the particular service instance")
        @RequestHeader(value = "instance-id", required = false, defaultValue = "") String instanceId);
}
