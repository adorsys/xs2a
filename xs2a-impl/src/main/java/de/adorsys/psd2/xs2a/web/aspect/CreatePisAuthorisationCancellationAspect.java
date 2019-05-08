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

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.controller.PaymentController;
import de.adorsys.psd2.xs2a.web.link.PisAuthorisationCancellationLinks;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class CreatePisAuthorisationCancellationAspect extends AbstractLinkAspect<PaymentController> {
    private ScaApproachResolver scaApproachResolver;
    private final RedirectLinkBuilder redirectLinkBuilder;

    public CreatePisAuthorisationCancellationAspect(ScaApproachResolver scaApproachResolver, MessageService messageService,
                                                    RedirectLinkBuilder redirectLinkBuilder, AspspProfileService aspspProfileService) {
        super(messageService, aspspProfileService);
        this.scaApproachResolver = scaApproachResolver;
        this.redirectLinkBuilder = redirectLinkBuilder;
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.PaymentCancellationAuthorisationService.createPisCancellationAuthorization(..)) && args( paymentId, psuData, paymentType, paymentProduct)", returning = "result", argNames = "result,paymentId,psuData,paymentType,paymentProduct")
    public ResponseObject<Xs2aCreatePisCancellationAuthorisationResponse> createPisAuthorizationAspect(ResponseObject<Xs2aCreatePisCancellationAuthorisationResponse> result, String paymentId, PsuIdData psuData, PaymentType paymentType, String paymentProduct) {
        if (!result.hasError()) {
            Xs2aCreatePisCancellationAuthorisationResponse body = result.getBody();
            body.setLinks(new PisAuthorisationCancellationLinks(getHttpUrl(), scaApproachResolver, redirectLinkBuilder,
                                                                paymentType.getValue(), paymentProduct, paymentId, body.getAuthorisationId(), psuData));
            return result;
        }
        return enrichErrorTextMessage(result);
    }

}
