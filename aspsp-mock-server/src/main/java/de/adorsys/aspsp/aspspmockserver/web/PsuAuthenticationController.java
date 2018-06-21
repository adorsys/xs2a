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
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/psu-authentication")
@Api(tags = "PSU Authentication", description = "Provides access to the Psu authentication for payment execution")
public class PsuAuthenticationController {
    private final PsuAuthenticationService psuAuthenticationService;

    @ApiOperation(value = "Generates and sends TAN to PSU`s e-mail by it`s ASPSP identifier", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created", response = String.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    @PostMapping(path = "/{psu-id}")
    public ResponseEntity<String> generateAndSendTan(HttpServletRequest request,
                                                     @PathVariable("psu-id") String psuId) throws Exception {
        //TODO change to correct url when tan validation page will be created according to task https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/99
        String uriString = getUriString(request);

        return psuAuthenticationService.generateAndSendTanForPsu(psuId)
                   ? ResponseEntity.created(new URI(uriString + "/tan-confirmation")).build()
                   : ResponseEntity.badRequest().build();
    }

    @ApiOperation(value = "Validates TAN sended to PSU`s e-mail and returns a link to continue as authenticated user", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = String.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    @GetMapping(path = "/{psu-id}/{tan}")
    public ResponseEntity<String> validatePsuTan(HttpServletRequest request,
                                                 @PathVariable("psu-id") String psuId,
                                                 @PathVariable("tan") int tanNumber) throws Exception {
        //TODO change to correct url when consent validation page will be created according to task https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/99
        String uriString = getUriString(request);

        return psuAuthenticationService.isPsuTanNumberValid(psuId, tanNumber)
                   ? ResponseEntity.ok(new URI(uriString).getPath())
                   : ResponseEntity.badRequest().build();
    }

    private String getUriString(HttpServletRequest request) {
        return UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request)).build().toUriString();
    }

}
