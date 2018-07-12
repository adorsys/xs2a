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

import de.adorsys.aspsp.xs2a.domain.MulticurrencyAccountLevel;
import de.adorsys.aspsp.xs2a.domain.ScaApproach;
import de.adorsys.aspsp.xs2a.service.AspspProfileService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Profile("debug_mode")
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "aspsp-profile/for-debug")
@Api(value = "Update aspsp profile ", tags = "Update aspsp profile.  Only for DEBUG!",
     description = "Provides access to update aspsp profile",
     authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
public class AspspProfileUpdateController {
    private final AspspProfileService aspspProfileService;

    @PutMapping(path = "/frequency-per-day")
    @ApiOperation(value = "Updates frequency per day. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok", response = String.class),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateFrequencyPerDay(@RequestBody int frequencyPerDay) {
        aspspProfileService.updateFrequencyPerDay(frequencyPerDay);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/combined-service-indicator")
    @ApiOperation(value = "Updates combined service indicator. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok", response = String.class),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateCombinedServiceIndicator(@RequestBody boolean combinedServiceIndicator) {
        aspspProfileService.updateCombinedServiceIndicator(combinedServiceIndicator);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/available-payment-products")
    @ApiOperation(value = "Updates available payment products. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok", response = String.class),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateAvailablePaymentProducts(@RequestBody List<String> availablePaymentProducts) {
        aspspProfileService.updateAvailablePaymentProducts(availablePaymentProducts);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/available-payment-types")
    @ApiOperation(value = "Updates available payment types. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok", response = String.class),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateAvailablePaymentTypes(@RequestBody List<String> types) {
        aspspProfileService.updateAvailablePaymentTypes(types);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/sca-approach")
    @ApiOperation(value = "Updates sca approach. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok", response = String.class),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateScaApproach(@RequestBody String scaApproach) {
        aspspProfileService.updateScaApproach(ScaApproach.valueOf(scaApproach.trim().toUpperCase()));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/tpp-signature-required")
    @ApiOperation(value = "Updates signature of the request by the TPP. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok", response = String.class),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateTppSignatureRequired(@RequestBody boolean tppSignatureRequired) {
        aspspProfileService.updateTppSignatureRequired(tppSignatureRequired);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/redirect-url-to-aspsp-pis")
    @ApiOperation(value = "Updates value of PIS redirect url to aspsp. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok", response = String.class),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateRedirectUrlToAspsp(@RequestBody String redirectUrlToAspsp) {
        aspspProfileService.updatePisRedirectUrlToAspsp(redirectUrlToAspsp);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/redirect-url-to-aspsp-ais")
    @ApiOperation(value = "Updates value of AIS redirect url to aspsp. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok", response = String.class),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateAisRedirectUrlToAspsp(@RequestBody String redirectUrlToAspsp) {
        aspspProfileService.updateAisRedirectUrlToAspsp(redirectUrlToAspsp);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/multicurrency-account-level")
    @ApiOperation(value = "Updates supported multicurrency account levels. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok", response = String.class),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateMulticurrencyAccountLevel(@RequestBody String multicurrencyAccountLevel) {
        aspspProfileService.updateMulticurrencyAccountLevel(MulticurrencyAccountLevel.valueOf(multicurrencyAccountLevel.trim().toUpperCase()));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
