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

package de.adorsys.psd2.consent.web.aspsp.controller;

import de.adorsys.psd2.consent.aspsp.api.piis.CmsAspspPiisService;
import de.adorsys.psd2.consent.aspsp.api.piis.CreatePiisConsentRequest;
import de.adorsys.psd2.consent.aspsp.api.piis.CreatePiisConsentResponse;
import de.adorsys.psd2.xs2a.core.piis.PiisConsent;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "aspsp-api/v1/piis/consents")
@Api(value = "aspsp-api/v1/piis/consents", tags = "ASPSP PIIS, Consents", description = "Controller for cms-aspsp-api providing access to PIIS consents")
public class CmsAspspPiisController {
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";

    private final CmsAspspPiisService cmsAspspPiisService;

    @PostMapping(path = "/")
    @ApiOperation(value = "Creates new PIIS consent")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created", response = String.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CreatePiisConsentResponse> createConsent(@RequestBody CreatePiisConsentRequest request,
                                                                   @ApiParam(value = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceeding AIS service in the same session. ")
                                                                   @RequestHeader(value = "psu-id", required = false) String psuId,
                                                                   @ApiParam(value = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility. ")
                                                                   @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
                                                                   @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
                                                                   @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
                                                                   @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
                                                                   @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType) {
        PsuIdData psuIdData = getPsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType);

        return cmsAspspPiisService.createConsent(psuIdData, request)
                   .map(consentId -> new ResponseEntity<>(new CreatePiisConsentResponse(consentId), HttpStatus.CREATED))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @GetMapping(path = "/")
    @ApiOperation(value = "Returns a list of PIIS Consent objects by PSU ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<List<PiisConsent>> getConsentsForPsu(
        @ApiParam(value = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceeding AIS service in the same session. ")
        @RequestHeader(value = "psu-id", required = false) String psuId,
        @ApiParam(value = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility. ")
        @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @ApiParam(value = "ID of the particular service instance")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        PsuIdData psuIdData = getPsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType);
        return new ResponseEntity<>(cmsAspspPiisService.getConsentsForPsu(psuIdData, instanceId), HttpStatus.OK);
    }

    @DeleteMapping(path = "/{consent-id}")
    @ApiOperation(value = "Terminates PIIS Consent object by its ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Boolean> terminateConsent(
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created account consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId,
        @ApiParam(value = "ID of the particular service instance")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return new ResponseEntity<>(cmsAspspPiisService.terminateConsent(consentId, instanceId), HttpStatus.OK);
    }

    private PsuIdData getPsuIdData(String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType) {
        return new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType);
    }
}
