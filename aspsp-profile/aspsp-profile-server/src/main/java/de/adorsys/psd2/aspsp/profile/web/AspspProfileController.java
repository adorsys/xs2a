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

package de.adorsys.psd2.aspsp.profile.web;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/aspsp-profile")
@Api(value = "Aspsp profile", tags = "Aspsp profile", description = "Provides access to aspsp profile")
public class AspspProfileController {

    private final AspspProfileService aspspProfileService;

    @GetMapping
    @ApiOperation(value = "Reads aspsp specific settings")
    @ApiResponse(code = 200, message = "Ok", response = AspspSettings.class)
    public ResponseEntity<AspspSettings> getAspspSettings() {
        return new ResponseEntity<>(aspspProfileService.getAspspSettings(), HttpStatus.OK);
    }

    @GetMapping(path = "/sca-approaches")
    @ApiOperation(value = "Reads list of sca approaches")
    @ApiResponse(code = 200, message = "Ok", response = ScaApproach.class)
    public ResponseEntity<List<ScaApproach>> getScaApproaches() {
        return new ResponseEntity<>(aspspProfileService.getScaApproaches(), HttpStatus.OK);
    }
}
