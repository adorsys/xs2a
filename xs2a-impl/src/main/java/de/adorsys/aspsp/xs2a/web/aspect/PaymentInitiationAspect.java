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
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.aspsp.xs2a.service.authorization.AuthorisationMethodService;
import de.adorsys.aspsp.xs2a.service.message.MessageService;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.aspsp.xs2a.web.PaymentController;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class PaymentInitiationAspect extends AbstractPaymentLink<PaymentController> {
    public PaymentInitiationAspect(AspspProfileServiceWrapper aspspProfileService, MessageService messageService, AuthorisationMethodService authorisationMethodService) {
        super(aspspProfileService, messageService, authorisationMethodService);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.aspsp.xs2a.service.PaymentService.createPayment(..)) && args(payment,requestParameters, ..)", returning = "result", argNames = "result,payment,requestParameters")
    public ResponseObject<?> createPaymentAspect(ResponseObject<?> result, Object payment, PaymentInitiationParameters requestParameters) {
        if (!result.hasError()) {
            return enrichLink(result, requestParameters);
        }
        return enrichErrorTextMessage(result);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.api.PaymentApi._initiatePayment(..)) && args(body,..)", returning = "result", argNames = "result,body")
    public ResponseEntity<?> testAspect(ResponseEntity<?> result, Object body) {
        log.info("fdfdfdfdfdfdf");
        return result;
    }
}
