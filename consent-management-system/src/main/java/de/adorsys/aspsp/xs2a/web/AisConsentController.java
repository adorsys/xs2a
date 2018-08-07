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

import de.adorsys.aspsp.xs2a.consent.api.ConsentActionRequest;
import de.adorsys.aspsp.xs2a.consent.api.ConsentStatus;
import de.adorsys.aspsp.xs2a.consent.api.ais.AisAccountConsent;
import de.adorsys.aspsp.xs2a.consent.api.ais.AisConsentRequest;
import de.adorsys.aspsp.xs2a.service.AISConsentService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "ais/consent")
@Api(value = "ais/consent", tags = "AIS, Consents", description = "Provides access to consent management system for AIS")
public class AisConsentController {
    private final AISConsentService aisConsentService;

    @PostMapping(path = "/")
    @ApiOperation(value = "Create consent for given psu id and accesses.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created", response = String.class),
        @ApiResponse(code = 204, message = "No Content")})
    public ResponseEntity<String> createConsent(@RequestBody AisConsentRequest request) {
        return aisConsentService.createConsent(request)
                   .map(consentId -> new ResponseEntity<>(consentId, HttpStatus.CREATED))
                   .orElse(new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    @PostMapping(path = "/action")
    @ApiOperation(value = "Save information about uses of consent")
    public ResponseEntity<Void> saveConsentActionLog(@RequestBody ConsentActionRequest request) {
        aisConsentService.checkConsentAndSaveActionLog(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/{consent-id}")
    @ApiOperation(value = "Read account consent by given consent id.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AisAccountConsent.class),
        @ApiResponse(code = 204, message = "No Content")})
    public ResponseEntity<AisAccountConsent> getConsentById(
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created account consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId) {
        return aisConsentService.getAisAccountConsentById(consentId)
                   .map(consent -> new ResponseEntity<>(consent, HttpStatus.OK))
                   .orElse(new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    @GetMapping(path = "/{consent-id}/status")
    @ApiOperation(value = "Can check the status of an account information consent resource.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ConsentStatus.class),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<ConsentStatus> getConsentStatusById(
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created account consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId) {
        return aisConsentService.getConsentStatusById(consentId)
                   .map(status -> new ResponseEntity<>(status, HttpStatus.OK))
                   .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping(path = "/{consent-id}/status/{status}")
    @ApiOperation(value = "Update consent status in the consent identified by given consent id.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Void> updateConsentStatus(
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created account consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId,
        @ApiParam(value = "The following code values are permitted 'VALID', 'REJECTED', 'REVOKED_BY_PSU', 'TERMINATED_BY_TPP'. These values might be extended by ASPSP by more values.", example = "VALID")
        @PathVariable("status") String status) {
        return aisConsentService.updateConsentStatusById(consentId, ConsentStatus.valueOf(status))
                   .map(updated -> new ResponseEntity<Void>(HttpStatus.OK))
                   .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
