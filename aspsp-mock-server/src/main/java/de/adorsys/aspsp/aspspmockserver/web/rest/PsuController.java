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

package de.adorsys.aspsp.aspspmockserver.web.rest;

import de.adorsys.aspsp.aspspmockserver.service.PsuService;
import de.adorsys.aspsp.xs2a.spi.domain.psu.Psu;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/psu")
@Api(tags = "PSUs", description = "Provides access to the Psu`s")
public class PsuController {
    private final PsuService psuService;

    @ApiOperation(value = "Returns a list of all PSU`s available at ASPSP", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = List.class),
        @ApiResponse(code = 204, message = "Not Content")})
    @GetMapping(path = "/")
    public ResponseEntity<List<Psu>> readAllPsuList() {
        return ResponseEntity.ok(psuService.getAllPsuList());
    }

    @ApiOperation(value = "Returns a PSU by its ASPSP identifier", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @GetMapping(path = "/{id}")
    public ResponseEntity<Psu> readPsuById(@PathVariable("id") String id) {
        return psuService.getPsuById(id)
                   .map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @ApiOperation(value = "Creates a PSU at ASPSP", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = URI.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    @PostMapping(path = "/")
    public ResponseEntity createPsu(HttpServletRequest request, @RequestBody Psu psu) throws Exception {
        String uriString = getUriString(request);
        String saved = psuService.createPsuAndReturnId(psu);
        return saved == null
                   ? ResponseEntity.badRequest().build()
                   : ResponseEntity.created(new URI(uriString + saved)).build();
    }

    @ApiOperation(value = "Removes PSU from ASPSP by it`s ASPSP identifier", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "No Content"),
        @ApiResponse(code = 404, message = "Not Found")})
    @DeleteMapping(path = "/{id}")
    public ResponseEntity deletePsu(@PathVariable("id") String id) {
        if (psuService.deletePsuById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private String getUriString(HttpServletRequest request) {
        return UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request)).build().toUriString();
    }

}
