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

import de.adorsys.psd2.aspsp.profile.domain.migration.OldProfileConfiguration;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileConvertService;
import de.adorsys.psd2.aspsp.profile.web.config.AspspProfileApiTagName;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("debug_mode")
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/aspsp-profile/convert-profile", consumes = "application/x-yaml", produces = "application/x-yaml")
@Api(value = "Convert old aspsp profile", tags = AspspProfileApiTagName.CONVERT_OLD_ASPSP_PROFILE)
public class AspspProfileConvertController {
    private final AspspProfileConvertService convertService;

    @PostMapping(path = "/aspsp-settings")
    @ApiOperation(value = "Converts old aspsp profile to the new format")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<String> convertAspspSetting(@RequestBody OldProfileConfiguration oldConfiguration) {
        return ResponseEntity.ok(convertService.convertProfile(oldConfiguration));
    }
}
