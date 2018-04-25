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
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.ais.consent.AccountConsent;
import de.adorsys.aspsp.xs2a.domain.ais.consent.CreateConsentReq;
import de.adorsys.aspsp.xs2a.domain.ais.consent.CreateConsentResp;
import de.adorsys.aspsp.xs2a.service.ConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.ResponseMapper;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping(path = "api/v1/consents")
@Api(value = "api/v1/consents", tags = "AISP Consents", description = "Provides access to the Psu Consents")
public class ConsentInformationController {
    private final ConsentService consentService;
    private final ResponseMapper responseMapper;

    @ApiOperation(value = "Creates an account information consent resource at the ASPSP regarding access to accounts specified in this request.")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "OK", response = CreateConsentResp.class), @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(method = RequestMethod.POST)
    @ApiImplicitParams({
    @ApiImplicitParam(name = "tpp-transaction-id", value = "16d40f49-a110-4344-a949-f99828ae13c9", required = true, dataType = "UUID", paramType = "header"),
    @ApiImplicitParam(name = "tpp-request-id", value = "21d40f65-a150-8343-b539-b9a822ae98c0", required = true, dataType = "UUID", paramType = "header"),
    @ApiImplicitParam(name = "psu-id", value = "31d50f56-a543-3664-b345-b9a822ae98a7", dataType = "UUID", paramType = "header")})
    public ResponseEntity<CreateConsentResp> createAccountConsent(
    @RequestHeader(name = "psu-id", required = false) String psuId,
    @ApiParam(name = "tppRedirectPreferred", value = "If it equals “true”, the TPP prefers a redirect over an embedded SCA approach.")
    @RequestParam(name = "tppRedirectPreferred", required = false) boolean tppRedirectPreferred,
    @ApiParam(name = "withBalance", value = "If contained, this function reads the list of accessible payment accounts including the balance.")
    @RequestParam(name = "withBalance", required = false) boolean withBalance,
    @Valid @RequestBody CreateConsentReq createConsent) {
        ResponseObject response = consentService.createAccountConsentsWithResponse(createConsent, withBalance, tppRedirectPreferred, psuId);


        return responseMapper.createdOrBadRequest(response);
    }

    @ApiOperation(value = "Can check the status of an account information consent resource")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Map.class),
    @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(value = "/{consent-id}/status", method = RequestMethod.GET)
    @ApiImplicitParams({
    @ApiImplicitParam(name = "tpp-transaction-id", value = "16d40f49-a110-4344-a949-f99828ae13c9", required = true, dataType = "UUID", paramType = "header"),
    @ApiImplicitParam(name = "tpp-request-id", value = "21d40f65-a150-8343-b539-b9a822ae98c0", required = true, dataType = "UUID", paramType = "header")})
    public ResponseEntity<Map<String, TransactionStatus>> getAccountConsentsStatusById(
    @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created resource", required = true)
    @PathVariable("consent-id") String consentId) {
        ResponseObject response = consentService.getAccountConsentsStatusById(consentId);
        return responseMapper.okOrNotFound(response);
    }

    @ApiOperation(value = "Returns the content of an account information consent object")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = AccountConsent.class),
    @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(value = "/{consent-id}", method = RequestMethod.GET)
    @ApiImplicitParams({
    @ApiImplicitParam(name = "tpp-transaction-id", value = "16d40f49-a110-4344-a949-f99828ae13c9", required = true, dataType = "UUID", paramType = "header"),
    @ApiImplicitParam(name = "tpp-request-id", value = "21d40f65-a150-8343-b539-b9a822ae98c0", required = true, dataType = "UUID", paramType = "header")})
    public ResponseEntity<AccountConsent> getAccountConsentsInformationById(
    @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created resource", required = true)
    @PathVariable("consent-id") String consentId) {
        ResponseObject response = consentService.getAccountConsentsById(consentId);
        return responseMapper.okOrNotFound(response);
    }

    @ApiOperation(value = " Delete information consent object")
    @ApiResponses(value = {@ApiResponse(code = 204, message = "No Content"),
    @ApiResponse(code = 404, message = "Not Found")})
    @RequestMapping(value = "/{consent-id}", method = RequestMethod.DELETE)
    @ApiImplicitParams({
    @ApiImplicitParam(name = "tpp-transaction-id", value = "16d40f49-a110-4344-a949-f99828ae13c9", required = true, dataType = "UUID", paramType = "header"),
    @ApiImplicitParam(name = "tpp-request-id", value = "21d40f65-a150-8343-b539-b9a822ae98c0", required = true, dataType = "UUID", paramType = "header")})
    public ResponseEntity<Void> deleteAccountConsent(
    @ApiParam(name = "consent-id", value = "The resource-id of consent to be deleted", required = true)
    @PathVariable("consent-id") String consentId) {
        ResponseObject response = consentService.deleteAccountConsentsById(consentId);

        return responseMapper.delete(response);
    }

}
