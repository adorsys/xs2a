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

import de.adorsys.aspsp.xs2a.domain.BookingStatus;
import de.adorsys.aspsp.xs2a.domain.MulticurrencyAccountLevel;
import de.adorsys.aspsp.xs2a.domain.ScaApproach;
import de.adorsys.aspsp.xs2a.domain.SupportedAccountReferenceField;
import de.adorsys.aspsp.xs2a.service.AspspProfileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "aspsp-profile")
@Api(value = "Aspsp profile", tags = "Aspsp profile", description = "Provides access to aspsp profile")
public class AspspProfileController {

    private final AspspProfileService aspspProfileService;

    @GetMapping(path = "/frequency-per-day")
    @ApiOperation(value = "Reads frequency per day")
    @ApiResponse(code = 200, message = "Ok", response = String.class)
    public ResponseEntity<Integer> getFrequencyPerDay() {
        return new ResponseEntity<>(aspspProfileService.getFrequencyPerDay(), HttpStatus.OK);
    }

    @GetMapping(path = "/combined-service-indicator")
    @ApiOperation(value = "Reads combined service indicator value")
    @ApiResponse(code = 200, message = "Ok", response = String.class)
    public ResponseEntity<Boolean> getCombinedServiceIndicator() {
        return new ResponseEntity<>(aspspProfileService.isCombinedServiceIndicator(), HttpStatus.OK);
    }

    @GetMapping(path = "/available-payment-products")
    @ApiOperation(value = "Reads list of available payment products")
    @ApiResponse(code = 200, message = "Ok", response = String.class)
    public ResponseEntity<List<String>> getAvailablePaymentProducts() {
        return new ResponseEntity<>(aspspProfileService.getAvailablePaymentProducts(), HttpStatus.OK);
    }

    @GetMapping(path = "/available-payment-types")
    @ApiOperation(value = "Reads list of available payment types ")
    @ApiResponse(code = 200, message = "Ok", response = String.class)
    public ResponseEntity<List<String>> getAvailablePaymentTypes() {
        return new ResponseEntity<>(aspspProfileService.getAvailablePaymentTypes(), HttpStatus.OK);
    }

    @GetMapping(path = "/sca-approach")
    @ApiOperation(value = "Reads sca approach value")
    @ApiResponse(code = 200, message = "Ok", response = ScaApproach.class)
    public ResponseEntity<ScaApproach> getScaApproach() {
        return new ResponseEntity<>(aspspProfileService.getScaApproach(), HttpStatus.OK);
    }

    @GetMapping(path = "/tpp-signature-required")
    @ApiOperation(value = "Reads signature of the request by the TPP")
    @ApiResponse(code = 200, message = "Ok", response = String.class)
    public ResponseEntity<Boolean> getTppSignatureRequired() {
        return new ResponseEntity<>(aspspProfileService.isTppSignatureRequired(), HttpStatus.OK);
    }

    @GetMapping(path = "/redirect-url-to-aspsp-pis")
    @ApiOperation(value = "Reads Pis redirect url to aspsp")
    @ApiResponse(code = 200, message = "Ok", response = String.class)
    public ResponseEntity<String> getPisRedirectUrlToAspsp() {
        return new ResponseEntity<>(aspspProfileService.getPisRedirectUrlToAspsp(), HttpStatus.OK);
    }

    @GetMapping(path = "/redirect-url-to-aspsp-ais")
    @ApiOperation(value = "Reads Ais redirect url to aspsp")
    @ApiResponse(code = 200, message = "Ok", response = String.class)
    public ResponseEntity<String> getAisRedirectUrlToAspsp() {
        return new ResponseEntity<>(aspspProfileService.getAisRedirectUrlToAspsp(), HttpStatus.OK);
    }

    @GetMapping(path = "/multicurrency-account-level")
    @ApiOperation(value = "Reads supported multicurrency account levels, on which XS2A interface can handle sub-accounts in accordance of capabilities of ASPSP Core systems")
    @ApiResponse(code = 200, message = "Ok", response = MulticurrencyAccountLevel.class)
    public ResponseEntity<MulticurrencyAccountLevel> getMulticurrencyAccountLevel() {
        return new ResponseEntity<>(aspspProfileService.getMulticurrencyAccountLevel(), HttpStatus.OK);
    }

    @GetMapping(path = "/available-booking-statuses")
    @ApiOperation(value = "Reads available booking statuses, such as booked, pending and both")
    @ApiResponse(code = 200, message = "Ok", response = String.class)
    public ResponseEntity<List<BookingStatus>> getAvailableBookingStatuses() {
        return new ResponseEntity<>(aspspProfileService.getAvailableBookingStatuses(), HttpStatus.OK);
    }

    @GetMapping(path = "/supported-account-reference-fields")
    @ApiOperation(value = "Reads supported Account Reference Fields, this list is used for AccountReference validation at xs2a")
    @ApiResponse(code = 200, message = "Ok", response = SupportedAccountReferenceField.class)
    public ResponseEntity<List<SupportedAccountReferenceField>> getAccountReferenceFields() {
        return new ResponseEntity<>(aspspProfileService.getSupportedAccountReferenceFields(), HttpStatus.OK);
    }

    @GetMapping(path = "/consent-lifetime")
    @ApiOperation(value = "Reads the value of a maximum lifetime of consent")
    @ApiResponse(code = 200, message = "Ok", response = Integer.class)
    public ResponseEntity<Integer> getConsentLifetime() {
        return new ResponseEntity<>(aspspProfileService.getConsentLifetime(), HttpStatus.OK);
    }

    @GetMapping(path = "/transaction-lifetime")
    @ApiOperation(value = "Reads the value of a maximum lifetime of transaction")
    @ApiResponse(code = 200, message = "Ok", response = Integer.class)
    public ResponseEntity<Integer> getTransactionLifetime() {
        return new ResponseEntity<>(aspspProfileService.getTransactionLifetime(), HttpStatus.OK);
    }
}
