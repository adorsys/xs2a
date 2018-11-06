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

package de.adorsys.aspsp.aspspmockserver.web;

import de.adorsys.aspsp.aspspmockserver.domain.TanHolder;
import de.adorsys.aspsp.aspspmockserver.service.TanGettingService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("test")
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/consent/tan")
@Api(tags = "Tan getting controller", description = "Returns tan number (for testing purposes only!)")
public class TanGettingController {
    private final TanGettingService tanGettingService;

    @GetMapping(path = "/{psu-id}")
    @ApiOperation(value = "Returns last unused tan by psu id", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = TanHolder.class),
        @ApiResponse(code = 204, message = "No content")
    })
    public ResponseEntity<TanHolder> getTan(@PathVariable("psu-id") String psuId) {
        return tanGettingService.getUnusedTanNumberByPsuId(psuId)
                   .map(tanHolder -> new ResponseEntity<>(tanHolder, HttpStatus.OK))
                   .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
