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

package de.adorsys.psd2.aspsp.profile.web;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileUpdateService;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("debug_mode")
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "aspsp-profile/for-debug")
@Api(value = "Update aspsp profile ", tags = "Update aspsp profile.  Only for DEBUG!",
    description = "Provides access to update aspsp profile")
public class AspspProfileUpdateController {
    private final AspspProfileUpdateService aspspProfileService;

    @PutMapping(path = "/sca-approach")
    @ApiOperation(value = "Updates sca approach. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateScaApproach(@RequestBody String scaApproach) {
        aspspProfileService.updateScaApproach(ScaApproach.valueOf(scaApproach.trim().toUpperCase()));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/aspsp-settings")
    @ApiOperation(value = "Updates aspsp profile settings. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateAspspSettings(@RequestBody AspspSettings aspspSettings) {
        aspspProfileService.updateAspspSettings(aspspSettings);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
