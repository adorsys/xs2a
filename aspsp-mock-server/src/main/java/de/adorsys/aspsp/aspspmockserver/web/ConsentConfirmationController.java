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

import de.adorsys.aspsp.aspspmockserver.domain.Confirmation;
import de.adorsys.aspsp.aspspmockserver.domain.ConfirmationType;
import de.adorsys.aspsp.aspspmockserver.domain.spi.consent.SpiConsentStatus;
import de.adorsys.aspsp.aspspmockserver.service.ConsentService;
import de.adorsys.aspsp.aspspmockserver.service.TanConfirmationService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/consent/confirmation/ais")
@Api(tags = "Consent confirmation for online banking", description = "Provides access to consent confirmation for online banking")
public class ConsentConfirmationController {

    private final TanConfirmationService tanConfirmationService;
    private final ConsentService consentService;

    @PostMapping(path = "/{psu-id}")
    @ApiOperation(value = "Generates TAN for ais consent confirmation", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 400, message = "Bad request")
    })
    public ResponseEntity generateAndSendTan(@PathVariable("psu-id") String psuId) {
        return tanConfirmationService.generateAndSendTanForPsuById(psuId)
                   ? ResponseEntity.ok().build()
                   : ResponseEntity.badRequest().build();
    }

    @PutMapping
    @ApiOperation(value = "Validates TAN for consent confirmation", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 400, message = "Bad request")
    })
    public ResponseEntity confirmTan(@RequestBody Confirmation confirmation) {
        return tanConfirmationService.confirmTan(confirmation.getPsuId(), confirmation.getTanNumber(), confirmation.getConsentId(), ConfirmationType.CONSENT);
    }

    @PutMapping(path = "/{consent-id}/{status}")
    @ApiOperation(value = "Update ais consent status of the corresponding consent", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    public ResponseEntity<Void> updateAisConsentStatus(@PathVariable("consent-id") String consentId,
                                                       @PathVariable("status") SpiConsentStatus status) {
        consentService.updateAisConsentStatus(consentId, status);
        return ResponseEntity.ok().build();
    }
}
