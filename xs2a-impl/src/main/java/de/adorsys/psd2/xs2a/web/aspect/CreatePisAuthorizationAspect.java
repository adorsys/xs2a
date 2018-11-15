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

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.Xsa2CreatePisConsentAuthorisationResponse;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.PaymentController;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class CreatePisAuthorizationAspect extends AbstractLinkAspect<PaymentController> {
    public CreatePisAuthorizationAspect(AspspProfileServiceWrapper aspspProfileService, MessageService messageService) {
        super(aspspProfileService, messageService);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.ConsentService.createPisConsentAuthorization(..)) && args(paymentId, paymentType, psuData)", returning = "result", argNames = "result,paymentId,paymentType,psuData")
    public ResponseObject<Xsa2CreatePisConsentAuthorisationResponse> createPisConsentAuthorizationAspect(ResponseObject<Xsa2CreatePisConsentAuthorisationResponse> result, String paymentId, PaymentType paymentType, PsuIdData psuData) {
        if (!result.hasError()) {
            Xsa2CreatePisConsentAuthorisationResponse body = result.getBody();
            body.setLinks(buildLink(paymentType.getValue(), paymentId, body.getAuthorizationId()));
            return result;
        }
        return enrichErrorTextMessage(result);
    }

    private Links buildLink(String paymentService, String paymentId, String authorizationId) {
        Links links = new Links();
        links.setSelf(buildPath("/v1/{payment-service}/{payment-id}", paymentService, paymentId));
        links.setStatus(buildPath("/v1/{payment-service}/{payment-id}/status", paymentService, paymentId));
        links.setStartAuthorisationWithPsuAuthentication(buildPath("/v1/{paymentService}/{paymentId}/authorisations/{authorizationId}", paymentService, paymentId, authorizationId));
        return links;
    }
}
