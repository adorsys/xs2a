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

import de.adorsys.aspsp.xs2a.service.AisConsentService;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ais.AccessAccountInfo;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ais.AisConsentRequest;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ais.AvailableAccessRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "ais/consent")
public class AisConsentController {
    private final AisConsentService aisConsentService;

    @PostMapping(path = "/create")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    public ResponseEntity<String> create(@RequestBody AisConsentRequest request) {
        return aisConsentService.createConsent(request)
                   .map(consentId -> new ResponseEntity<>(consentId, HttpStatus.CREATED))
                   .orElse(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @PostMapping(path = "/available/access")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    public ResponseEntity<Map<String, Set<AccessAccountInfo>>> checkAvailable(@RequestBody AvailableAccessRequest request) {
        return ResponseEntity.ok(aisConsentService.checkAvailable(request));
    }

    @GetMapping(path = "/{consent-id}")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    public ResponseEntity<SpiAccountConsent> getSpiAccountConsentsById(@PathVariable("consent-id") String consentId) {
        return aisConsentService.getSpiAccountConsentById(consentId)
                   .map(consent -> new ResponseEntity<>(consent, HttpStatus.OK))
                   .orElse(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @GetMapping(path = "/{consent-id}/status")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    public ResponseEntity<SpiConsentStatus> getAccountConsentsStatusById(@PathVariable("consent-id") String consentId) {
        return aisConsentService.getConsentStatusById(consentId)
                   .map(status -> new ResponseEntity<>(status, HttpStatus.OK))
                   .orElse(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @PutMapping(path = "/{consent-id}/status/{status}")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    public ResponseEntity<Void> updateConsentStatus(
        @PathVariable("consent-id") String consentId,
        @PathVariable("status") String status) {
        return aisConsentService.updateConsentStatusById(consentId, SpiConsentStatus.valueOf(status))
                   .map(updated -> new ResponseEntity<Void>(HttpStatus.OK))
                   .orElse(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }
}
