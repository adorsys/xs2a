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

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponseType;
import de.adorsys.psd2.xs2a.domain.authorisation.CancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.controller.PaymentController;
import de.adorsys.psd2.xs2a.web.link.PisAuthorisationCancellationLinks;
import de.adorsys.psd2.xs2a.web.link.UpdatePisCancellationPsuDataLinks;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class CreatePisAuthorisationCancellationAspect extends AbstractLinkAspect<PaymentController> {
    private final ScaApproachResolver scaApproachResolver;
    private final RedirectLinkBuilder redirectLinkBuilder;
    private final RedirectIdService redirectIdService;

    public CreatePisAuthorisationCancellationAspect(ScaApproachResolver scaApproachResolver, MessageService messageService,
                                                    RedirectLinkBuilder redirectLinkBuilder, AspspProfileService aspspProfileService,
                                                    RedirectIdService redirectIdService) {
        super(messageService, aspspProfileService);
        this.scaApproachResolver = scaApproachResolver;
        this.redirectLinkBuilder = redirectLinkBuilder;
        this.redirectIdService = redirectIdService;
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.PaymentCancellationAuthorisationService.createPisCancellationAuthorisation(..)) && args(request)", returning = "result", argNames = "result,request")
    public ResponseObject<CancellationAuthorisationResponse> createPisAuthorisationAspect(ResponseObject<CancellationAuthorisationResponse> result, Xs2aCreatePisAuthorisationRequest request) {
        if (result.hasError()) {
            return enrichErrorTextMessage(result);
        }

        CancellationAuthorisationResponse body = result.getBody();
        AuthorisationResponseType authorisationResponseType = body.getAuthorisationResponseType();

        if (authorisationResponseType == AuthorisationResponseType.START) {
            Xs2aCreatePisCancellationAuthorisationResponse response = (Xs2aCreatePisCancellationAuthorisationResponse) result.getBody();
            response.setLinks(new PisAuthorisationCancellationLinks(getHttpUrl(), scaApproachResolver, redirectLinkBuilder,
                                                                    redirectIdService,
                                                                    request.getPaymentService(), request.getPaymentProduct(), request.getPaymentId(), body.getCancellationId(), getScaRedirectFlow()));
        } else if (authorisationResponseType == AuthorisationResponseType.UPDATE) {
            Xs2aUpdatePisCommonPaymentPsuDataResponse response = (Xs2aUpdatePisCommonPaymentPsuDataResponse) result.getBody();
            Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildXs2aUpdatePisCommonPaymentPsuDataRequest(request.getPaymentId(),
                                                                                                                   response.getCancellationId(),
                                                                                                                   request.getPsuData(),
                                                                                                                   request.getPaymentProduct(),
                                                                                                                   request.getPaymentService(),
                                                                                                                   request.getPassword());
            response.setLinks(new UpdatePisCancellationPsuDataLinks(getHttpUrl(), scaApproachResolver, updateRequest,
                                                                    body.getScaStatus(), response.getChosenScaMethod()));
        } else {
            throw new IllegalArgumentException("Unknown authorisation response type: " + authorisationResponseType);
        }

        return result;
    }

    private Xs2aUpdatePisCommonPaymentPsuDataRequest buildXs2aUpdatePisCommonPaymentPsuDataRequest(String paymentId,
                                                                                                   String cancellationId,
                                                                                                   PsuIdData psuIdData,
                                                                                                   String paymentProduct,
                                                                                                   String paymentService,
                                                                                                   String password) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
        updateRequest.setPaymentId(paymentId);
        updateRequest.setAuthorisationId(cancellationId);
        updateRequest.setPsuData(psuIdData);
        updateRequest.setPassword(password);
        updateRequest.setPaymentProduct(paymentProduct);
        updateRequest.setPaymentService(paymentService);
        return updateRequest;
    }
}
