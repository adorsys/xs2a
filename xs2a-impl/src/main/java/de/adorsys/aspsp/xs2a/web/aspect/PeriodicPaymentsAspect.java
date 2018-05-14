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

package de.adorsys.aspsp.xs2a.web.aspect;

import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.web.PaymentInitiationController;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Slf4j
@Aspect
@Component
public class PeriodicPaymentsAspect extends AbstractLinkAspect<PaymentInitiationController> {

    @AfterReturning(pointcut = "execution(* de.adorsys.aspsp.xs2a.web.PeriodicPaymentsController.createPeriodicPayment(..)) && args(paymentProduct,..)", returning = "result")
    public ResponseEntity<PaymentInitialisationResponse> invokeAspect(ResponseEntity<PaymentInitialisationResponse> result, String paymentProduct) {
        PaymentInitialisationResponse body = result.getBody();
        body.setLinks(buildLink(body, paymentProduct));
        return new ResponseEntity(body, result.getHeaders(), result.getStatusCode());
    }

    private Links buildLink(PaymentInitialisationResponse body, String paymentProduct) {
        Class controller = getController();

        Links links = new Links();
        links.setRedirect(redirectLinkToSource);
        links.setSelf(linkTo(controller, paymentProduct).slash(body.getPaymentId()).toString());
        links.setUpdatePsuIdentification(linkTo(controller, paymentProduct).slash(body.getPaymentId()).toString());
        links.setUpdatePsuAuthentication(linkTo(controller, paymentProduct).slash(body.getPaymentId()).toString());
        links.setStatus(linkTo(controller, paymentProduct).slash("status").toString());
        return links;
    }
}
