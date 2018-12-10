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

import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.controller.PaymentController;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class UpdatePisConsentPsuDataAspect extends AbstractLinkAspect<PaymentController> {
    private final static String PSU_AUTHORISATION_URL = "/v1/{paymentService}/{paymentId}/authorisations/{authorisationId}";

    public UpdatePisConsentPsuDataAspect(AspspProfileServiceWrapper aspspProfileService, MessageService messageService) {
        super(aspspProfileService, messageService);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.ConsentService.updatePisConsentPsuData(..)) && args( request)", returning = "result", argNames = "result,request")
    public ResponseObject<Xs2aUpdatePisConsentPsuDataResponse> updatePisConsentAuthorizationAspect(ResponseObject<Xs2aUpdatePisConsentPsuDataResponse> result, Xs2aUpdatePisConsentPsuDataRequest request) {
        if (!result.hasError()) {
            Xs2aUpdatePisConsentPsuDataResponse body = result.getBody();
            Links links = buildLink(request);

            if (isScaStatusMethodAuthenticated(body.getScaStatus())) {

                links.setSelectAuthenticationMethod(buildAuthorisationLink(request.getPaymentService(), request.getPaymentId(), request.getAuthorizationId()));
                links.setUpdatePsuAuthentication(buildAuthorisationLink(request.getPaymentService(), request.getPaymentId(), request.getAuthorizationId()));
            } else if (isScaStatusMethodSelected(body.getChosenScaMethod(), body.getScaStatus())) {

                links.setAuthoriseTransaction(buildAuthorisationLink(request.getPaymentService(), request.getPaymentId(), request.getAuthorizationId()));
            } else if (isScaStatusFinalised(body.getScaStatus())) {

                links.setScaStatus(buildAuthorisationLink(request.getPaymentService(), request.getPaymentId(), request.getAuthorizationId()));
            }

            body.setLinks(links);
            return result;
        }

        return enrichErrorTextMessage(result);
    }

    private Links buildLink(Xs2aUpdatePisConsentPsuDataRequest request) {
        Links links = new Links();
        links.setSelf(buildPath("/v1/{paymentService}/{paymentId}", request.getPaymentService(), request.getPaymentId()));
        links.setStatus(buildPath("/v1/{paymentService}/{paymentId}/status", request.getPaymentService(), request.getPaymentId()));
        return links;
    }

    private String buildAuthorisationLink(String paymentService, String paymentId, String authorisationId) {
        return buildPath(PSU_AUTHORISATION_URL, paymentService, paymentId, authorisationId);
    }

    private boolean isScaStatusFinalised(ScaStatus scaStatus) {
        return scaStatus == ScaStatus.FINALISED;
    }

    private boolean isScaStatusMethodSelected(Xs2aAuthenticationObject chosenScaMethod, ScaStatus scaStatus) {
        return chosenScaMethod != null
                   && scaStatus == ScaStatus.SCAMETHODSELECTED;
    }

    private boolean isScaStatusMethodAuthenticated(ScaStatus scaStatus) {
        return scaStatus == ScaStatus.PSUAUTHENTICATED;
    }
}
