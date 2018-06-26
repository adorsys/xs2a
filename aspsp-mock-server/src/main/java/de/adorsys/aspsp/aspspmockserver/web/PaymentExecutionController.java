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

import de.adorsys.aspsp.aspspmockserver.service.PaymentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@RequiredArgsConstructor
@Controller
@RequestMapping(path = "/view/payment/confirmation/consent")
@Api(tags = "Payment execution controller", description = "Provides access to proceeding the payment after psu gave consent")
public class PaymentExecutionController {
    private final PaymentService paymentService;

    @PostMapping(path = "/", params = "decision=yes")
    @ApiOperation(value = "Proceeds payment and changes the status of the corresponding consent")
    public ModelAndView proceedPayment(@ModelAttribute("paymentConfirmation") PaymentConfirmation paymentConfirmation) {
        return paymentService.addPaymentWithRedirectApproach(paymentConfirmation.getConsentId())
                   .map(paym -> new ModelAndView("paymentSuccessPage", "paymentDetails", paym))
                   .orElse(new ModelAndView("paymentFailurePage"));
    }

    @PostMapping(path = "/", params = "decision=no")
    @ApiOperation(value = "Shows payment failure page")
    public ModelAndView declinePayment(
        @ModelAttribute("paymentConfirmation") PaymentConfirmation paymentConfirmation) {
        return new ModelAndView("consentDeclinedPage");
    }
}
