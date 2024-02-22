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

package de.adorsys.psd2.aspsp.profile.web.controller;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.aspsp.profile.web.config.AspspProfileApiTagName;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/aspsp-profile")
@Tag(description = "ASPSP profile", name = AspspProfileApiTagName.ASPSP_PROFILE)
public class AspspProfileController {
    public static final String DEFAULT_SERVICE_INSTANCE_ID = "";

    private final AspspProfileService aspspProfileService;

    @GetMapping
    @Operation(description = "Reads ASPSP specific settings")
    @ApiResponse(responseCode = "200", description = "Ok", content = @Content(schema = @Schema(implementation = AspspSettings.class)))
    public ResponseEntity<AspspSettings> getAspspSettings(
        @RequestHeader(value = "Instance-ID", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId
    ) {
        return new ResponseEntity<>(aspspProfileService.getAspspSettings(instanceId), HttpStatus.OK);
    }

    @GetMapping(path = "/sca-approaches")
    @Operation(description = "Reads list of SCA approaches")
    @ApiResponse(responseCode = "200", description = "Ok", content = @Content(schema = @Schema(implementation = ScaApproach.class)))
    public ResponseEntity<List<ScaApproach>> getScaApproaches(
        @RequestHeader(value = "Instance-ID", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId
    ) {
        return new ResponseEntity<>(aspspProfileService.getScaApproaches(instanceId), HttpStatus.OK);
    }

    @GetMapping(path = "/multitenancy/enabled")
    @Operation(description = "Reads multitenancy supporting flag")
    @ApiResponse(responseCode = "200", description = "Ok", content = @Content(schema = @Schema(implementation = ScaApproach.class)))
    public ResponseEntity<Boolean> isMultitenancyEnabled() {
        return new ResponseEntity<>(aspspProfileService.isMultitenancyEnabled(), HttpStatus.OK);
    }
}
