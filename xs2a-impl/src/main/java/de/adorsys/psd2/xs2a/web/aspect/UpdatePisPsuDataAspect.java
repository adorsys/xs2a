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
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.link.PaymentAuthorisationAspectService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class UpdatePisPsuDataAspect {
    private PaymentAuthorisationAspectService paymentAuthorisationAspectService;

    public UpdatePisPsuDataAspect(PaymentAuthorisationAspectService paymentAuthorisationAspectService) {
        this.paymentAuthorisationAspectService = paymentAuthorisationAspectService;
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.PaymentAuthorisationService.updatePisCommonPaymentPsuData(..)) && args( request)", returning = "result", argNames = "result,request")
    public ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> updatePisAuthorizationAspect(ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> result, PaymentAuthorisationParameters request) {
        return paymentAuthorisationAspectService.updatePisAuthorizationAspect(result, request);
    }
}
