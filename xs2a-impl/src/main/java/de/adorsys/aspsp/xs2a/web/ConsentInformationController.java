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

package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.consent.AccountConsent;
import de.adorsys.aspsp.xs2a.domain.consent.ConsentStatus;
import de.adorsys.aspsp.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.aspsp.xs2a.domain.consent.CreateConsentResp;
import de.adorsys.aspsp.xs2a.service.ConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.ResponseMapper;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/consents")
@Api(value = "api/v1/consents", tags = "AISP, Consents", description = "Provides access to the Psu Consents")
public class ConsentInformationController {
    private final ConsentService consentService;
    private final ResponseMapper responseMapper;

    @ApiOperation(value = "Creates an account information consent resource at the ASPSP regarding access to accounts specified in this request.", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "OK", response = CreateConsentResp.class),
        @ApiResponse(code = 400, message = "Bad request")})
    @PostMapping
    @ApiImplicitParams({
        @ApiImplicitParam(name = "x-request-id", value = "2f77a125-aa7a-45c0-b414-cea25a116035", required = true, dataType = "UUID", paramType = "header"),
        @ApiImplicitParam(name = "psu-id", value = "31d50f56-a543-3664-b345-b9a822ae98a7", paramType = "header"),
        @ApiImplicitParam(name = "psu-id-type", value = "Type of the PSU-ID", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "psu-corporate-id", value = "Might be mandated in the ASPSP’s documentation", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "psu-corporate-id-type", value = "Might be mandated in the ASPSP’s documentation", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "tpp-redirect-uri", value = "http://example.com", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "digest", value = "digest of the payload request", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "signature", value = "98c0", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "tpp-signature-certificate", value = "some certificate", dataType = "String", paramType = "header")})
    public ResponseEntity<CreateConsentResp> createAccountConsent(
        @RequestHeader(name = "psu-id", required = false) String psuId,
        @Valid @RequestBody CreateConsentReq createConsent) {
        ResponseObject<CreateConsentResp> response = consentService.createAccountConsentsWithResponse(createConsent, psuId);
        return responseMapper.created(response);
    }

    @ApiOperation(value = "Can check the status of an account information consent resource", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ConsentStatus.class),
        @ApiResponse(code = 400, message = "Bad request")})
    @GetMapping(path = "/{consent-id}/status")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "x-request-id", value = "2f77a125-aa7a-45c0-b414-cea25a116035", required = true, dataType = "UUID", paramType = "header"),
        @ApiImplicitParam(name = "digest", value = "digest of the payload request", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "signature", value = "98c0", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "tpp-signature-certificate", value = "some certificate", dataType = "String", paramType = "header")})
    public ResponseEntity<ConsentStatus> getAccountConsentsStatusById(
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created resource", required = true)
        @PathVariable("consent-id") String consentId) {
        ResponseObject<ConsentStatus> response = consentService.getAccountConsentsStatusById(consentId);
        return responseMapper.ok(response);
    }

    @ApiOperation(value = "Returns the content of an account information consent object", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AccountConsent.class),
        @ApiResponse(code = 400, message = "Bad request")})
    @GetMapping(path = "/{consent-id}")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "x-request-id", value = "2f77a125-aa7a-45c0-b414-cea25a116035", required = true, dataType = "UUID", paramType = "header"),
        @ApiImplicitParam(name = "digest", value = "digest of the payload request", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "signature", value = "98c0", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "tpp-signature-certificate", value = "some certificate", dataType = "String", paramType = "header")})
    public ResponseEntity<AccountConsent> getAccountConsentsInformationById(
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created resource", required = true)
        @PathVariable("consent-id") String consentId) {
        ResponseObject<AccountConsent> response = consentService.getAccountConsentById(consentId);
        return responseMapper.ok(response);
    }

    @ApiOperation(value = " Delete information consent object", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "No Content"),
        @ApiResponse(code = 404, message = "Not Found")})
    @DeleteMapping(path = "/{consent-id}")
    @ApiImplicitParam(name = "x-request-id", value = "2f77a125-aa7a-45c0-b414-cea25a116035", required = true, dataType = "UUID", paramType = "header")
    public ResponseEntity<Void> deleteAccountConsent(
        @ApiParam(name = "consent-id", value = "The resource-id of consent to be deleted", required = true)
        @PathVariable("consent-id") String consentId) {
        ResponseObject<Void> response = consentService.deleteAccountConsentsById(consentId);
        return responseMapper.delete(response);
    }

}
