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

package de.adorsys.psd2.aspsp.profile.web;

import de.adorsys.psd2.aspsp.profile.domain.BookingStatus;
import de.adorsys.psd2.aspsp.profile.domain.MulticurrencyAccountLevel;
import de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileUpdateService;
import de.adorsys.psd2.xs2a.core.profile.PaymentProduct;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
    description = "Provides access to update aspsp profile")
public class AspspProfileUpdateController {
    private final AspspProfileUpdateService aspspProfileService;

    @PutMapping(path = "/frequency-per-day")
    @ApiOperation(value = "Updates frequency per day. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateFrequencyPerDay(@RequestBody int frequencyPerDay) {
        aspspProfileService.updateFrequencyPerDay(frequencyPerDay);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/combined-service-indicator")
    @ApiOperation(value = "Updates combined service indicator. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateCombinedServiceIndicator(@RequestBody boolean combinedServiceIndicator) {
        aspspProfileService.updateCombinedServiceIndicator(combinedServiceIndicator);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/available-payment-products")
    @ApiOperation(value = "Updates available payment products. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateAvailablePaymentProducts(@RequestBody List<PaymentProduct> availablePaymentProducts) {
        aspspProfileService.updateAvailablePaymentProducts(availablePaymentProducts);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/available-payment-types")
    @ApiOperation(value = "Updates available payment types. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateAvailablePaymentTypes(@RequestBody List<PaymentType> types) {
        aspspProfileService.updateAvailablePaymentTypes(types);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/sca-approach")
    @ApiOperation(value = "Updates sca approach. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateScaApproach(@RequestBody String scaApproach) {
        aspspProfileService.updateScaApproach(ScaApproach.valueOf(scaApproach.trim().toUpperCase()));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/tpp-signature-required")
    @ApiOperation(value = "Updates signature of the request by the TPP. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateTppSignatureRequired(@RequestBody boolean tppSignatureRequired) {
        aspspProfileService.updateTppSignatureRequired(tppSignatureRequired);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/bank-offered-consent-support")
    @ApiOperation(value = "Updates bankOfferedConsentSupport status. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateBankOfferedConsentSupport(@RequestBody Boolean bankOfferedConsentSupport) {
        aspspProfileService.updateBankOfferedConsentSupport(bankOfferedConsentSupport);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/redirect-url-to-aspsp-pis")
    @ApiOperation(value = "Updates value of PIS redirect url to aspsp. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateRedirectUrlToAspsp(@RequestBody String redirectUrlToAspsp) {
        aspspProfileService.updatePisRedirectUrlToAspsp(redirectUrlToAspsp);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/redirect-url-to-aspsp-ais")
    @ApiOperation(value = "Updates value of AIS redirect url to aspsp. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateAisRedirectUrlToAspsp(@RequestBody String redirectUrlToAspsp) {
        aspspProfileService.updateAisRedirectUrlToAspsp(redirectUrlToAspsp);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/multicurrency-account-level")
    @ApiOperation(value = "Updates supported multicurrency account levels. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateMulticurrencyAccountLevel(@RequestBody String multicurrencyAccountLevel) {
        aspspProfileService.updateMulticurrencyAccountLevel(MulticurrencyAccountLevel.valueOf(multicurrencyAccountLevel.trim().toUpperCase()));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/available-booking-statuses")
    @ApiOperation(value = "Updates supported booking statuses. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateBookingStatuses(@RequestBody List<BookingStatus> bookingStatuses) {
        aspspProfileService.updateAvailableBookingStatuses(bookingStatuses);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/supported-account-reference-fields")
    @ApiOperation(value = "Updates supported Account Reference fields. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateAccountReferenceFields(@RequestBody List<SupportedAccountReferenceField> referenceFields) {
        aspspProfileService.updateSupportedAccountReferenceFields(referenceFields);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/consent-lifetime")
    @ApiOperation(value = "Updates the value of a maximum lifetime of consent. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateConsentLifetime(@RequestBody int consentLifetime) {
        aspspProfileService.updateConsentLifetime(consentLifetime);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/transaction-lifetime")
    @ApiOperation(value = "Updates the value of a maximum lifetime of transaction. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateTransactionLifetime(@RequestBody int transactionLifetime) {
        aspspProfileService.updateTransactionLifetime(transactionLifetime);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/all-psd2-support")
    @ApiOperation(value = "Updates AllPsd2Support status. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateAllPsd2Support(@RequestBody Boolean allPsd2Support) {
        aspspProfileService.updateAllPsd2Support(allPsd2Support);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/transactions-without-balances-supported")
    @ApiOperation(value = "Update the value of transactions without balances supported. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateTransactionsWithoutBalancesSupported(@RequestBody boolean transactionsWithoutBalancesSupported) {
        aspspProfileService.updateTransactionsWithoutBalancesSupported(transactionsWithoutBalancesSupported);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/signing-basket-supported")
    @ApiOperation(value = "Update the value of signing basket support. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateSigningBasketSupported(@RequestBody boolean signingBasketSupported) {
        aspspProfileService.updateSigningBasketSupported(signingBasketSupported);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/payment-cancellation-authorization-mandated")
    @ApiOperation(value = "Update the value of payment cancellation authorization mandated. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updatePaymentCancellationAuthorizationMandated(@RequestBody boolean paymentCancellationAuthorizationMandated) {
        aspspProfileService.updatePaymentCancellationAuthorizationMandated(paymentCancellationAuthorizationMandated);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "/piis-consent-supported")
    @ApiOperation(value = "Update the value of PIIS consent supported. Only for DEBUG!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updatePiisConsentSupported(@RequestBody boolean piisConsentSupported) {
        aspspProfileService.updatePiisConsentSupported(piisConsentSupported);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
