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

import de.adorsys.aspsp.aspspmockserver.service.PsuService;
import de.adorsys.psd2.aspsp.mock.api.psu.AspspAuthenticationObject;
import de.adorsys.psd2.aspsp.mock.api.psu.Psu;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/psu")
@Api(tags = "PSUs", description = "Provides access to the Psu`s")
public class PsuController {
    private final PsuService psuService;

    @ApiOperation(value = "Returns a list of all PSU`s available at ASPSP", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = List.class),
        @ApiResponse(code = 204, message = "No Content")})
    @GetMapping(path = "/")
    public ResponseEntity<List<Psu>> readAllPsuList() {
        List<Psu> psus = psuService.getAllPsuList();
        return CollectionUtils.isNotEmpty(psus)
                   ? ResponseEntity.ok(psus)
                   : ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "Returns a PSU by its ASPSP identifier", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = List.class),
        @ApiResponse(code = 204, message = "No Content")})
    @GetMapping(path = "/{psu-id}")
    public ResponseEntity<Psu> readPsuById(@PathVariable("psu-id") String psuId) {
        return psuService.getPsuByPsuId(psuId)
                   .map(ResponseEntity::ok)
                   .orElseGet(ResponseEntity.noContent()::build);
    }

    @ApiOperation(value = "Returns a list of allowed payment products for PSU by its ASPSP identifier", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = List.class),
        @ApiResponse(code = 204, message = "No Content")})
    @GetMapping(path = "/allowed-payment-products/{iban}")
    public ResponseEntity<List<String>> readPaymentProductsById(@PathVariable("iban") String iban) {
        return Optional.ofNullable(psuService.getAllowedPaymentProducts(iban))
                   .map(ResponseEntity::ok)
                   .orElseGet(ResponseEntity.noContent()::build);
    }

    @ApiOperation(value = "Adds a payment product to the list of allowed products for PSU specified by its ASPSP identifier", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = List.class)})
    @PutMapping(path = "/allowed-payment-products/{psu-id}/{product}")
    public void addPaymentProduct(@PathVariable("psu-id") String psuId, @PathVariable(value = "product") String product) {
        psuService.addAllowedProduct(psuId, product);
    }

    @ApiOperation(value = "Creates a PSU at ASPSP", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Created", response = String.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    @PostMapping(path = "/")
    public ResponseEntity<String> createPsu(@RequestBody Psu psu) {
        String saved = psuService.createPsuAndReturnId(psu);
        return StringUtils.isNotBlank(saved)
                   ? ResponseEntity.ok(saved)
                   : ResponseEntity.badRequest().build();
    }

    @ApiOperation(value = "Removes PSU from ASPSP by it`s ASPSP identifier", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "No Content"),
        @ApiResponse(code = 404, message = "Not Found")})
    @DeleteMapping(path = "/{aspsp-psu-id}")
    public ResponseEntity deletePsu(@PathVariable("aspsp-psu-id") String aspspPsuId) {
        return psuService.deletePsuByAspspPsuId(aspspPsuId)
                   ? ResponseEntity.noContent().build()
                   : ResponseEntity.notFound().build();
    }

    @ApiOperation(value = "Returns a list of SCA methods for PSU by its login", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = List.class),
        @ApiResponse(code = 204, message = "No Content")})
    @GetMapping(path = "/sca-methods/{psu-id}")
    public ResponseEntity<List<AspspAuthenticationObject>> readScaMethods(@PathVariable("psu-id") String psuId) {
        return Optional.ofNullable(psuService.getScaMethods(psuId))
                   .map(ResponseEntity::ok)
                   .orElseGet(ResponseEntity.noContent()::build);
    }

    @ApiOperation(value = "Updates list of SCA methods for PSU specified by its login", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
    @PutMapping(path = "/sca-methods/{psu-id}")
    public void updateScaMethods(@RequestBody List<AspspAuthenticationObject> scaMethods, @PathVariable("psu-id") String psuId) {
        psuService.updateScaMethods(psuId, scaMethods);
    }
}
