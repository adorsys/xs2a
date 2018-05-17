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

import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.web.BulkPaymentInitiationController;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
public class BulkPaymentInitiationAspect extends AbstractPaymentLink<BulkPaymentInitiationController> {

    @AfterReturning(pointcut = "execution(* de.adorsys.aspsp.xs2a.web.BulkPaymentInitiationController.createBulkPaymentInitiation(..)) && args(paymentProduct,..)", returning = "result")
    public ResponseEntity<List<PaymentInitialisationResponse>> invokeAspect(ResponseEntity<List<PaymentInitialisationResponse>> result, String paymentProduct) {
        List<PaymentInitialisationResponse> body = result.getBody();
        List<PaymentInitialisationResponse> newBody = body.stream()
            .map(paym -> setLinksAndReturnResponse(paym, paymentProduct))
            .collect(Collectors.toList());

        return new ResponseEntity(newBody, result.getHeaders(), result.getStatusCode());
    }

    private PaymentInitialisationResponse setLinksAndReturnResponse(PaymentInitialisationResponse paymentInitialisationResponse, String paymentProduct) {
        paymentInitialisationResponse.setLinks(buildPaymentLinks(paymentInitialisationResponse, paymentProduct));
        return paymentInitialisationResponse;
    }
}
