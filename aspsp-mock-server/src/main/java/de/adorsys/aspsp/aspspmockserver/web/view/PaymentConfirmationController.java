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
import de.adorsys.aspsp.xs2a.spi.domain.payment.AspspPayment;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
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
    private final PaymentConfirmationService paymentConfirmationService;
    private final PaymentService paymentService;

    @GetMapping(path = "/{iban}/{consent-id}/{payment-id}")
    @ApiOperation(value = "Sends TAN to psu`s email, validates TAN sent to PSU`s e-mail and returns a link to continue as authenticated user")
    public ModelAndView showConfirmationPage(@PathVariable("iban") String iban,
                                             @PathVariable("consent-id") String consentId,
                                             @PathVariable("payment-id") String paymentId) {

        paymentConfirmationService.generateAndSendTanForPsuByIban(iban);
        String decodedPaymentId = new String(Base64.getDecoder().decode(paymentId));

        return new ModelAndView("tanConfirmationPage", "paymentConfirmation", new PaymentConfirmation(iban, consentId, decodedPaymentId));
    }

    @PostMapping
    @ApiOperation(value = "Displays content of email TAN confirmation page")
    public ModelAndView confirmTan(@ModelAttribute("paymentConfirmation") PaymentConfirmation paymentConfirmation,
                                   ModelAndView model) {
        Optional<AspspPayment> payment = paymentService.getPaymentById(paymentConfirmation.getPaymentId());
        if(payment.isPresent()) {
            model.addObject("paymentDetails", payment.get());
            model.addObject("paymentConfirmation", paymentConfirmation);
            String viewName = paymentConfirmationService.isTanNumberValidByIban(paymentConfirmation.getIban(), paymentConfirmation.getTanNumber(), paymentConfirmation.getConsentId())
                                  ? "consentConfirmationPage"
                                  : "tanConfirmationError";
            model.setViewName(viewName);
        }
        else {
            model.setViewName("paymentFailurePage");
        }

        return model;
    }

    @PostMapping(path = "/consent", params = "decision=confirmed")
    @ApiOperation(value = "Proceeds payment and changes the status of the corresponding consent")
    public ModelAndView proceedPayment(@ModelAttribute("paymentConfirmation") PaymentConfirmation paymentConfirmation) {
        paymentService.updatePaymentConsentStatus(paymentConfirmation.getConsentId(), VALID);
        return new ModelAndView("paymentSuccessPage", "paymentDetails", paymentService.getPaymentById(paymentConfirmation.getPaymentId()).get());
    }

    @PostMapping(path = "/consent", params = "decision=revoked")
    @ApiOperation(value = "Shows payment failure page")
    public ModelAndView revokePaymentConsent(
        @ModelAttribute("paymentConfirmation") PaymentConfirmation paymentConfirmation) {
        paymentService.updatePaymentConsentStatus(paymentConfirmation.getConsentId(), REVOKED_BY_PSU);
        return new ModelAndView("consentRevokedPage");
    }
}
