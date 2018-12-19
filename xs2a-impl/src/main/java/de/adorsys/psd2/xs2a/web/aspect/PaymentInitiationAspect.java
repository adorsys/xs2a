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

package de.adorsys.psd2.xs2a.web.aspect;

import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodService;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.controller.PaymentController;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PaymentInitiationAspect extends AbstractPaymentLink<PaymentController> {
    public PaymentInitiationAspect(AspspProfileServiceWrapper aspspProfileService, MessageService messageService, AuthorisationMethodService authorisationMethodService, RedirectLinkBuilder redirectLinkBuilder) {
        super(aspspProfileService, messageService, authorisationMethodService, redirectLinkBuilder);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.PaymentService.createPayment(..)) && args(payment,requestParameters, ..)", returning = "result", argNames = "result,payment,requestParameters")
    public ResponseObject<?> createPaymentAspect(ResponseObject<?> result, Object payment, PaymentInitiationParameters requestParameters) {
        if (!result.hasError()) {
            return enrichLink(result, requestParameters);
        }
        return enrichErrorTextMessage(result);
    }
}
