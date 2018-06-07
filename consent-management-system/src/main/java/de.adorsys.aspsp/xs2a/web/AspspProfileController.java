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

import de.adorsys.aspsp.xs2a.service.AspspProfileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "aspsp-profile")
@Api(value = "Aspsp profile", tags = "Aspsp profile", description = "Provides access to aspsp profile")
public class AspspProfileController {
    private final AspspProfileService aspspProfileService;

    @GetMapping(path = "/frequency-per-day")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    public ResponseEntity<Integer> getFrequencyPerDay() {
        return new ResponseEntity<>(aspspProfileService.getFrequencyPerDay(), HttpStatus.OK);
    }

    @PutMapping(path = "/frequency-per-day")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    public ResponseEntity<Void> updateFrequencyPerDay(@RequestBody Integer frequencyPerDay) {
        aspspProfileService.updateFrequencyPerDay(frequencyPerDay);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(path = "/combined-service-indicator")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    public ResponseEntity<Boolean> getCombinedServiceIndicator() {
        return new ResponseEntity<>(aspspProfileService.isCombinedServiceIndicator(), HttpStatus.OK);
    }

    @PutMapping(path = "/combined-service-indicator")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    public ResponseEntity<Void> updateCombinedServiceIndicator(@RequestBody boolean combinedServiceIndicator) {
        aspspProfileService.updateCombinedServiceIndicator(combinedServiceIndicator);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(path = "/available-payment-products")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    public ResponseEntity<Set<String>> getAvailablePaymentProducts() {
        return new ResponseEntity<>(aspspProfileService.getAvailablePaymentProducts(), HttpStatus.OK);
    }

    @PutMapping(path = "/available-payment-products")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    public ResponseEntity<Void> updateAvailablePaymentProducts(@RequestBody List<String> availablePaymentProducts) {
        aspspProfileService.updateAvailablePaymentProducts(availablePaymentProducts);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(path = "/available-payment-types")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    public ResponseEntity<Set<String>> getAvailablePaymentTypes() {
        return new ResponseEntity<>(aspspProfileService.getAvailablePaymentTypes(), HttpStatus.OK);
    }

    @PutMapping(path = "/available-payment-types")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    public ResponseEntity<Void> updateAvailablePaymentTypes(@RequestBody List<String> types) {
        aspspProfileService.updateAvailablePaymentTypes(types);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(path = "/sca-approach")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    public ResponseEntity<String> getScaApproach() {
        return new ResponseEntity<>(aspspProfileService.getScaApproach(), HttpStatus.OK);
    }

    @PutMapping(path = "/sca-approach")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    public ResponseEntity<Void> updateAvailablePaymentTypes(@RequestBody String scaApproach) {
        aspspProfileService.updateScaApproach(scaApproach);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
