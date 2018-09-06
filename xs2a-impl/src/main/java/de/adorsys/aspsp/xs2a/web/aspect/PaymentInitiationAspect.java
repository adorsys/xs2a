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

import de.adorsys.aspsp.xs2a.web12.PaymentController12;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class PaymentInitiationAspect extends AbstractPaymentLink<PaymentController12> {

    /* TODO refactor links creation according to 1.2 spec https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/283
    @AfterReturning(pointcut = "execution(* de.adorsys.aspsp.xs2a.service.PaymentService.createPaymentInitiation(..)) && args(paymentProduct,..)", returning = "result", argNames = "result,paymentProduct")
    public ResponseEntity<PaymentInitialisationResponse> invokeAspect(ResponseEntity<PaymentInitialisationResponse> result, String paymentProduct) {
        if (!hasError(result)) {
            PaymentInitialisationResponse body = result.getBody();
            body.setLinks(buildPaymentLinks(body, paymentProduct));
        }
        return new ResponseEntity<>(result.getBody(), result.getHeaders(), result.getStatusCode());
    }
    */
}
