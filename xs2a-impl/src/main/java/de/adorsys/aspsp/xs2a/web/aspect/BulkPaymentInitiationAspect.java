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

import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentType;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayment;
import de.adorsys.aspsp.xs2a.web12.PaymentController12;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
public class BulkPaymentInitiationAspect extends AbstractPaymentLink<PaymentController12> {

    @AfterReturning(pointcut = "execution(* de.adorsys.aspsp.xs2a.service.PaymentService.createBulkPayments(..)) && args(payments, tppSignatureCertificate, paymentProduct)", returning = "result", argNames = "result,payments,tppSignatureCertificate,paymentProduct")
    public ResponseObject<List<PaymentInitialisationResponse>> invokeAspect(ResponseObject<List<PaymentInitialisationResponse>> result, List<SinglePayment> payments, String tppSignatureCertificate, String paymentProduct) {
        if (!result.hasError()) {
            result.getBody().stream()
                .map(this::setLinksAndReturnResponse)
                .collect(Collectors.toList());
            return result;
        }
        return enrichErrorTextMessage(result);
    }

    private PaymentInitialisationResponse setLinksAndReturnResponse(PaymentInitialisationResponse response) {
        response.setLinks(buildPaymentLinks(response, PaymentType.BULK.getValue()));
        return response;
    }
}
