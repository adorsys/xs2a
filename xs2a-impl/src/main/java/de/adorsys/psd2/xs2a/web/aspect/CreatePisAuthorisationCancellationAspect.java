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
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.controller.PaymentController;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class CreatePisAuthorisationCancellationAspect extends AbstractLinkAspect<PaymentController> {
    private final RedirectLinkBuilder redirectLinkBuilder;

    public CreatePisAuthorisationCancellationAspect(ScaApproachResolver scaApproachResolver, MessageService messageService, RedirectLinkBuilder redirectLinkBuilder) {
        super(scaApproachResolver, messageService);
        this.redirectLinkBuilder = redirectLinkBuilder;
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.PaymentCancellationAuthorisationService.createPisCancellationAuthorization(..)) && args( paymentId, psuData, paymentType, paymentProduct)", returning = "result", argNames = "result,paymentId,psuData,paymentType,paymentProduct")
    public ResponseObject<Xs2aCreatePisCancellationAuthorisationResponse> createPisAuthorizationAspect(ResponseObject<Xs2aCreatePisCancellationAuthorisationResponse> result, String paymentId, PsuIdData psuData, PaymentType paymentType, String paymentProduct) {
        if (!result.hasError()) {
            Xs2aCreatePisCancellationAuthorisationResponse body = result.getBody();
            body.setLinks(buildLink(paymentType.getValue(), paymentProduct, paymentId, body.getAuthorisationId()));
            return result;
        }
        return enrichErrorTextMessage(result);
    }

    private Links buildLink(String paymentService, String paymentProduct, String paymentId, String authorizationId) {
        Links links = new Links();
        links.setSelf(buildPath("/v1/{payment-service}/{payment-product}/{payment-id}", paymentService, paymentProduct, paymentId));
        links.setStatus(buildPath("/v1/{payment-service}/{payment-product}/{payment-id}/status", paymentService, paymentProduct, paymentId));

        if (scaApproachResolver.resolveScaApproach() == ScaApproach.EMBEDDED) {
            return addEmbeddedRelatedLinks(links, paymentService, paymentProduct, paymentId, authorizationId);
        } else if (scaApproachResolver.resolveScaApproach() == ScaApproach.REDIRECT) {
            return addRedirectRelatedLinks(links, paymentService, paymentProduct, paymentId, authorizationId);
        } else if (scaApproachResolver.resolveScaApproach() == ScaApproach.OAUTH) {
            links.setScaOAuth("scaOAuth"); //TODO generate link for oauth https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/326
        }
        return links;
    }

    private Links addEmbeddedRelatedLinks(Links links, String paymentService, String paymentProduct, String paymentId, String authorizationId) {
        links.setStartAuthorisationWithPsuAuthentication(buildPath("/v1/{paymentService}/{payment-product}/{paymentId}/cancellation-authorisations/{authorisationId}", paymentService, paymentProduct, paymentId, authorizationId));
        return links;
    }

    private Links addRedirectRelatedLinks(Links links, String paymentService, String paymentProduct, String paymentId, String authorizationId) {
        String link = redirectLinkBuilder.buildPaymentCancellationScaRedirectLink(paymentId, authorizationId);
        links.setScaRedirect(link);
        links.setScaStatus(buildPath("/v1/{payment-service}/{payment-product}/{payment-id}/authorisations/{authorisation-id}", paymentService, paymentProduct, paymentId, authorizationId));

        return links;
    }
}
