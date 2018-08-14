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

import de.adorsys.aspsp.xs2a.consent.api.ConsentStatus;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentResponse;
import de.adorsys.aspsp.xs2a.service.PISConsentService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/pis/consent")
@Api(value = "api/v1/pis/consent", tags = "PIS, Consents", description = "Provides access to consent management system for PIS")
public class PisConsentController {
    private final PISConsentService pisConsentService;

    @PostMapping(path = "/")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = String.class),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<String> createPaymentConsent(@RequestBody PisConsentRequest request) {
        return pisConsentService.createPaymentConsent(request)
                   .map(consentId -> new ResponseEntity<>(consentId, HttpStatus.CREATED))
                   .orElse(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @GetMapping(path = "/{consent-id}/status")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ConsentStatus.class),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<ConsentStatus> getConsentStatusById(
        @ApiParam(name = "consent-id", value = "The payment consent identification assigned to the created payment consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId) {
        return pisConsentService.getConsentStatusById(consentId)
                   .map(status -> new ResponseEntity<>(status, HttpStatus.OK))
                   .orElse(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @GetMapping(path = "/{consent-id}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = PisConsentResponse.class),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<PisConsentResponse> getConsentById(
        @ApiParam(name = "consent-id", value = "The payment consent identification assigned to the created payment consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId) {
        return pisConsentService.getConsentById(consentId)
                   .map(pc -> new ResponseEntity<>(pc, HttpStatus.OK))
                   .orElse(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @PutMapping(path = "/{consent-id}/status/{status}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateConsentStatus(
        @ApiParam(name = "consent-id", value = "The payment consent identification assigned to the created payment consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId,
        @ApiParam(value = "The following code values are permitted 'received', 'valid', 'rejected', 'expired', 'revoked by psu', 'terminated by tpp'. These values might be extended by ASPSP by more values.", allowableValues = "RECEIVED,  REJECTED, VALID, REVOKED_BY_PSU,  EXPIRED,  TERMINATED_BY_TPP")
        @PathVariable("status") String status) {
        return pisConsentService.updateConsentStatusById(consentId, ConsentStatus.valueOf(status))
                   .map(updated -> new ResponseEntity<Void>(HttpStatus.OK))
                   .orElse(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }
}
