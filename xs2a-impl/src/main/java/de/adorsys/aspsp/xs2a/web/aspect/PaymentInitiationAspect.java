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

import de.adorsys.aspsp.xs2a.component.JsonConverter;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentType;
import de.adorsys.aspsp.xs2a.service.message.MessageService;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileService;
import de.adorsys.aspsp.xs2a.web12.PaymentController12;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class PaymentInitiationAspect extends AbstractPaymentLink<PaymentController12> {
    public PaymentInitiationAspect(int maxNumberOfCharInTransactionJson, AspspProfileService aspspProfileService, JsonConverter jsonConverter, MessageService messageService) {
        super(maxNumberOfCharInTransactionJson, aspspProfileService, jsonConverter, messageService);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.aspsp.xs2a.service.PaymentService.createPayment(..)) && args(payment, paymentType, ..)", returning = "result", argNames = "result,payment,paymentType")
    public ResponseObject<?> createPaymentAspect(ResponseObject<?> result, Object payment, PaymentType paymentType) {
        if (!result.hasError()) {
            return enrichLink(result, paymentType);
        }
        return enrichErrorTextMessage(result);
    }
}
