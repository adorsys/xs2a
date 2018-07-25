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

package de.adorsys.aspsp.aspspmockserver.web.view;

import de.adorsys.aspsp.aspspmockserver.domain.PaymentConfirmation;
import de.adorsys.aspsp.aspspmockserver.service.PaymentService;
import de.adorsys.aspsp.aspspmockserver.service.PaymentConfirmationService;
import de.adorsys.aspsp.aspspmockserver.web.util.ApiError;
import de.adorsys.aspsp.xs2a.spi.domain.payment.AspspPayment;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Base64;
import java.util.Optional;

import static de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus.REVOKED_BY_PSU;
import static de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus.VALID;

@RequiredArgsConstructor
@Controller
@RequestMapping(path = "/view/payment/confirmation")
@Api(tags = "TAN confirmation", description = "Provides access to email TAN confirmation for payment execution")
public class PaymentConfirmationController {

    @Value("${pis-webapp.baseurl}")
    private String pisWebapp;

    private final PaymentConfirmationService paymentConfirmationService;
    private final PaymentService paymentService;

    @GetMapping(path = "/{iban}/{consent-id}/{payment-id}")
    @ApiOperation(value = "Sends TAN to psu`s email, validates TAN sent to PSU`s e-mail and returns a link to continue as authenticated user")
    public String showConfirmationPage(@PathVariable("iban") String iban,
                                             @PathVariable("consent-id") String consentId,
                                             @PathVariable("payment-id") String paymentId) {

        paymentConfirmationService.generateAndSendTanForPsuByIban(iban);

        return "redirect:" + pisWebapp + "/" + iban + "/" + consentId + "/" + paymentId;
    }

    @PostMapping
    @ApiOperation(value = "Validates tan")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 400, message = "Bad request"),
        @ApiResponse(code = 401, message = "Wrong tan")
    })
    public ResponseEntity confirmTan(@ModelAttribute("paymentConfirmation") PaymentConfirmation paymentConfirmation) {
        Optional<AspspPayment> payment = paymentService.getPaymentById(paymentConfirmation.getPaymentId());
        if(payment.isPresent()) {
            ResponseEntity responseEntity;
            if (paymentConfirmationService.isTanNumberValidByIban(paymentConfirmation.getIban(), paymentConfirmation.getTanNumber(), paymentConfirmation.getConsentId())) {
                responseEntity = new ResponseEntity(HttpStatus.OK);
            } else {
                ApiError error = new ApiError(HttpStatus.UNAUTHORIZED, "WRONG_TAN", "Unauthorized");
                responseEntity = new ResponseEntity(error, error.getStatus());
            }

            return responseEntity;
        }
        else {
            ApiError error = new ApiError(HttpStatus.BAD_REQUEST, "PAYMENT_MISSING", "Bad request");
            return new ResponseEntity(error, error.getStatus());
        }
    }

    @PostMapping(path = "/consent", params = "decision=confirmed")
    @ApiOperation(value = "Proceeds payment and changes the status of the corresponding consent")
    public ResponseEntity proceedPayment(@ModelAttribute("paymentConfirmation") PaymentConfirmation paymentConfirmation) {
        paymentService.updatePaymentConsentStatus(paymentConfirmation.getConsentId(), VALID);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(path = "/consent", params = "decision=revoked")
    @ApiOperation(value = "Shows payment failure page")
    public ResponseEntity revokePaymentConsent(
        @ModelAttribute("paymentConfirmation") PaymentConfirmation paymentConfirmation) {
        paymentService.updatePaymentConsentStatus(paymentConfirmation.getConsentId(), REVOKED_BY_PSU);
        return new ResponseEntity(HttpStatus.OK);
    }
}
