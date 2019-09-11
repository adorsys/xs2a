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

import de.adorsys.psd2.aspsp.profile.domain.migration.OldProfileConfiguration;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileConvertService;
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
@Api(value = "Convert old aspsp profile", tags = "Convert old aspsp profile to the new")
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
