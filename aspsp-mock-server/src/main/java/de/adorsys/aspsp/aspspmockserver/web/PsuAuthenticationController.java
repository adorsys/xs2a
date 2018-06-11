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

import de.adorsys.aspsp.aspspmockserver.service.PsuAuthenticationService;
import de.adorsys.aspsp.xs2a.spi.domain.psu.PsuLogin;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/psu-authentication")
public class PsuAuthenticationController {
    private final PsuAuthenticationService psuAuthenticationService;


    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @GetMapping(path = "/login")
    public ResponseEntity validatePsuEmailAndPassword(@RequestBody PsuLogin psuLogin) throws Exception {
        String uriString = "TO DO define how to create this url";
        return psuAuthenticationService.isPsuEmailAndPasswordValid(psuLogin)
                   ? ResponseEntity.created(new URI(uriString)).build()
                   : ResponseEntity.badRequest().build();
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @GetMapping(path = "/{id}/{tan}")
    public ResponseEntity validatePsuEmailTan(@PathVariable("psuId") String psuId,
                                              @PathVariable("tan") int tan) throws Exception {
        String uriString = "TO DO define how to create this url";
        return psuAuthenticationService.isPsuEmailTanValid(psuId, tan)
                   ? ResponseEntity.created(new URI(uriString)).build()
                   : ResponseEntity.badRequest().build();
    }

}
