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
import de.adorsys.aspsp.aspspmockserver.exception.ApiError;
import de.adorsys.aspsp.aspspmockserver.service.PaymentService;
import de.adorsys.aspsp.aspspmockserver.service.PsuService;
import de.adorsys.aspsp.aspspmockserver.service.TanConfirmationService;
import de.adorsys.psd2.aspsp.mock.api.psu.AspspAuthenticationObject;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/consent/confirmation/pis")
@Api(tags = "Payment confirmation for online banking", description = "Provides access to email TAN confirmation for payment execution")
public class PaymentConfirmationController {

    private final TanConfirmationService tanConfirmationService;
    private final PaymentService paymentService;
    private final PsuService psuService;

    @PostMapping(path = "/{psu-id}/{sca-method-selected}")
    @ApiOperation(value = "Generates TAN for pis consent confirmation", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 400, message = "Bad request"),
        @ApiResponse(code = 404, message = "Not Found")
    })
    public ResponseEntity<Void> generateAndSendTan(@PathVariable("psu-id") String psuId, @PathVariable("sca-method-selected") String authenticationMethodId) {
        List<AspspAuthenticationObject> scaMethods = psuService.getScaMethods(psuId);
        if (Objects.isNull(scaMethods)) {
            return ResponseEntity.notFound().build();
        }

        boolean isScaMethodUnknown = scaMethods.stream()
                                         .noneMatch(m -> authenticationMethodId.equals(m.getAuthenticationMethodId()));

        if (isScaMethodUnknown) {
            return ResponseEntity.badRequest().build();
        }

        return tanConfirmationService.sendUserAuthRequestWithPreSelectedScaMethod(psuId)
                   ? ResponseEntity.ok().build()
                   : ResponseEntity.notFound().build();
    }

    @PutMapping
    @ApiOperation(value = "Confirm TAN", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 400, message = "Bad request")
    })
    public ResponseEntity confirmTan(@RequestBody Confirmation confirmation) {
        return CollectionUtils.isNotEmpty(paymentService.getPaymentById(confirmation.getPaymentId()))
                   ? tanConfirmationService.confirmTan(confirmation.getPsuId(), confirmation.getTanNumber(), confirmation.getConsentId(), ConfirmationType.PAYMENT)
                   : new ResponseEntity<>(new ApiError(HttpStatus.BAD_REQUEST, "PAYMENT_MISSING", "Bad request"), HttpStatus.BAD_REQUEST);
    }
}
