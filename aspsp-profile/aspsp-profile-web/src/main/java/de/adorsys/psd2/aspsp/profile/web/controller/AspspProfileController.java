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

package de.adorsys.psd2.aspsp.profile.web.controller;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.aspsp.profile.web.config.AspspProfileApiTagName;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
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
@Api(value = "Aspsp profile", tags = AspspProfileApiTagName.ASPSP_PROFILE)
public class AspspProfileController {
    public static final String DEFAULT_SERVICE_INSTANCE_ID = "";

    private final AspspProfileService aspspProfileService;

    @GetMapping
    @ApiOperation(value = "Reads aspsp specific settings")
    @ApiResponse(code = 200, message = "Ok", response = AspspSettings.class)
    public ResponseEntity<AspspSettings> getAspspSettings(
        @RequestHeader(value = "Instance-ID", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId
    ) {
        return new ResponseEntity<>(aspspProfileService.getAspspSettings(instanceId), HttpStatus.OK);
    }

    @GetMapping(path = "/sca-approaches")
    @ApiOperation(value = "Reads list of sca approaches")
    @ApiResponse(code = 200, message = "Ok", response = ScaApproach.class)
    public ResponseEntity<List<ScaApproach>> getScaApproaches(
        @RequestHeader(value = "Instance-ID", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId
    ) {
        return new ResponseEntity<>(aspspProfileService.getScaApproaches(instanceId), HttpStatus.OK);
    }

    @GetMapping(path = "/multitenancy/enabled")
    @ApiOperation(value = "Reads multitenncy supporting flag")
    @ApiResponse(code = 200, message = "Ok", response = ScaApproach.class)
    public ResponseEntity<Boolean> isMultitenancyEnabled() {
        return new ResponseEntity<>(aspspProfileService.isMultitenancyEnabled(), HttpStatus.OK);
    }
}
