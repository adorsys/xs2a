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

package de.adorsys.psd2.aspsp.profile.web.controller;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileUpdateService;
import de.adorsys.psd2.aspsp.profile.web.config.AspspProfileApiTagName;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
@Api(value = "Update aspsp profile ", tags = AspspProfileApiTagName.UPDATE_ASPSP_PROFILE)
public class AspspProfileUpdateController {
    public static final String DEFAULT_SERVICE_INSTANCE_ID = "";

    private final AspspProfileUpdateService aspspProfileService;

    @PutMapping(path = "/sca-approaches")
    @ApiOperation(value = "Updates list of sca approaches. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateScaApproach(@RequestBody List<String> newScaApproaches,
                                                  @RequestHeader(value = "Instance-ID", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        List<ScaApproach> scaApproaches = newScaApproaches.stream()
                                              .map(s -> ScaApproach.valueOf(s.trim().toUpperCase()))
                                              .collect(Collectors.toList());

        aspspProfileService.updateScaApproaches(scaApproaches, instanceId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/aspsp-settings")
    @ApiOperation(value = "Updates aspsp profile settings. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateAspspSettings(@RequestBody AspspSettings aspspSettings,
                                                    @RequestHeader(value = "Instance-ID", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        aspspProfileService.updateAspspSettings(aspspSettings, instanceId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/multitenancy/enabled")
    @ApiOperation(value = "Enable aspsp profile multi tenancy support. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> enableMultitenancy(@RequestBody Boolean multitenancyEnabled) {
        aspspProfileService.enableMultitenancy(multitenancyEnabled);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
