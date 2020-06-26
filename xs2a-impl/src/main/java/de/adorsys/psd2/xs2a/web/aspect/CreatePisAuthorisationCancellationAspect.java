/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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
import de.adorsys.psd2.xs2a.domain.authorisation.CancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationRequest;
import de.adorsys.psd2.xs2a.service.link.PaymentAuthorisationCancellationAspectService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class CreatePisAuthorisationCancellationAspect {

    private PaymentAuthorisationCancellationAspectService paymentAuthorisationCancellationAspectService;

    public CreatePisAuthorisationCancellationAspect(PaymentAuthorisationCancellationAspectService paymentAuthorisationCancellationAspectService) {
        this.paymentAuthorisationCancellationAspectService = paymentAuthorisationCancellationAspectService;
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.PaymentCancellationAuthorisationService.createPisCancellationAuthorisation(..)) && args(request)", returning = "result", argNames = "result,request")
    public ResponseObject<CancellationAuthorisationResponse> createPisAuthorisationAspect(ResponseObject<CancellationAuthorisationResponse> result, Xs2aCreatePisAuthorisationRequest request) {
        return paymentAuthorisationCancellationAspectService.createPisAuthorisationAspect(result, request);
    }


}
