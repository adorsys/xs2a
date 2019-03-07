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
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.controller.PaymentController;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.DECOUPLED;
import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.EMBEDDED;

@Slf4j
@Aspect
@Component
public class CreatePisAuthorizationAspect extends AbstractLinkAspect<PaymentController> {

    public CreatePisAuthorizationAspect(ScaApproachResolver scaApproachResolver, MessageService messageService, AspspProfileServiceWrapper profileService) {
        super(scaApproachResolver, messageService, profileService);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.PaymentAuthorisationService.createPisAuthorization(..)) && args(paymentId, paymentType, paymentProduct, psuData)", returning = "result", argNames = "result,paymentId,paymentType,paymentProduct,psuData")
    public ResponseObject<Xs2aCreatePisAuthorisationResponse> createPisAuthorizationAspect(ResponseObject<Xs2aCreatePisAuthorisationResponse> result, String paymentId, PaymentType paymentType, String paymentProduct, PsuIdData psuData) {
        if (!result.hasError()) {
            Xs2aCreatePisAuthorisationResponse body = result.getBody();
            body.setLinks(buildLink(paymentType.getValue(), paymentProduct, paymentId, body.getAuthorisationId(), psuData));
            return result;
        }
        return enrichErrorTextMessage(result);
    }

    private Links buildLink(String paymentService, String paymentProduct, String paymentId, String authorizationId, PsuIdData psuData) {
        Links links = new Links();
        links.setSelf(buildPath("/v1/{payment-service}/{payment-product}/{payment-id}", paymentService, paymentProduct, paymentId));
        links.setStatus(buildPath("/v1/{payment-service}/{payment-product}/{payment-id}/status", paymentService, paymentProduct, paymentId));
        if (EnumSet.of(EMBEDDED, DECOUPLED).contains(scaApproachResolver.resolveScaApproach())) {
            String path = "/v1/{paymentService}/{paymentProduct}/{paymentId}/authorisations/{authorisationId}";
            if (psuData.isEmpty()) {
                links.setStartAuthorisationWithPsuIdentification(buildPath(path, paymentService, paymentProduct, paymentId, authorizationId));
            } else {
                links.setStartAuthorisationWithPsuAuthentication(buildPath(path, paymentService, paymentProduct, paymentId, authorizationId));
            }
        }

        return links;
    }
}
