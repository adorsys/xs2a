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
import de.adorsys.psd2.aspsp.profile.service.AspspProfileUpdateService;
import de.adorsys.psd2.aspsp.profile.web.config.AspspProfileApiTagName;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Profile("debug_mode")
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/aspsp-profile/for-debug")
@Tag(description = "Update ASPSP profile", name = AspspProfileApiTagName.UPDATE_ASPSP_PROFILE)
public class AspspProfileUpdateController {
    public static final String DEFAULT_SERVICE_INSTANCE_ID = "";

    private final AspspProfileUpdateService aspspProfileService;

    @PutMapping(path = "/sca-approaches")
    @Operation(description = "Updates list of SCA approaches. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "400", description = "Bad request")})
    public ResponseEntity<Void> updateScaApproach(@RequestBody List<String> newScaApproaches,
                                                  @RequestHeader(value = "Instance-ID", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        List<ScaApproach> scaApproaches = newScaApproaches.stream()
                                              .map(s -> ScaApproach.valueOf(s.trim().toUpperCase()))
                                              .collect(Collectors.toList());

        aspspProfileService.updateScaApproaches(scaApproaches, instanceId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/aspsp-settings")
    @Operation(description = "Updates ASPSP profile settings. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "400", description = "Bad request")})
    public ResponseEntity<Void> updateAspspSettings(@RequestBody AspspSettings aspspSettings,
                                                    @RequestHeader(value = "Instance-ID", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        aspspProfileService.updateAspspSettings(aspspSettings, instanceId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/multitenancy/enabled")
    @Operation(description = "Enable ASPSP profile multi tenancy support. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "400", description = "Bad request")})
    public ResponseEntity<Void> enableMultitenancy(@RequestBody Boolean multitenancyEnabled) {
        aspspProfileService.enableMultitenancy(multitenancyEnabled);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
