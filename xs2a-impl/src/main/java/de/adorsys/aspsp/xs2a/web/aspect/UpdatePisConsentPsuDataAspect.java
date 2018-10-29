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

import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataResponse;
import de.adorsys.aspsp.xs2a.service.message.MessageService;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.aspsp.xs2a.web.PaymentController;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.apache.commons.lang3.StringUtils;
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

    @AfterReturning(pointcut = "execution(* de.adorsys.aspsp.xs2a.service.ConsentService.updatePisConsentPsuData(..)) && args(request)", returning = "result", argNames = "result,request")
    public ResponseObject<Xs2aUpdatePisConsentPsuDataResponse> updatePisConsentAuthorizationAspect(ResponseObject<Xs2aUpdatePisConsentPsuDataResponse> result, UpdatePisConsentPsuDataRequest request) {
        if (!result.hasError()) {
            Xs2aUpdatePisConsentPsuDataResponse body = result.getBody();
            Links links = buildLink(request);

            if (isScaStatusMethodAuthenticated(request.getScaStatus())) {

                links.setSelectAuthenticationMethod(buildAuthorisationLink(request.getPaymentService(), request.getPaymentId(), request.getAuthorizationId()));
                links.setUpdatePsuAuthentication(buildAuthorisationLink(request.getPaymentService(), request.getPaymentId(), request.getAuthorizationId()));
            } else if (isScaStatusMethodSelected(request.getAuthenticationMethodId(), request.getScaStatus())) {

                links.setAuthoriseTransaction(buildAuthorisationLink(request.getPaymentService(), request.getPaymentId(), request.getAuthorizationId()));
            } else if (isScaStatusFinalised(request.getScaAuthenticationData(), request.getScaStatus())) {

                links.setScaStatus(buildAuthorisationLink(request.getPaymentService(), request.getPaymentId(), request.getAuthorizationId()));
            }

            body.setLinks(links);
            return result;
        }

        return enrichErrorTextMessage(result);
    }

    private Links buildLink(UpdatePisConsentPsuDataRequest request) {
        Links links = new Links();
        links.setSelf(buildPath("/v1/{paymentService}/{paymentId}", request.getPaymentService(), request.getPaymentId()));
        links.setStatus(buildPath("/v1/{paymentService}/{paymentId}/status", request.getPaymentService(), request.getPaymentId()));
        return links;
    }

    private String buildAuthorisationLink(String paymentService, String paymentId, String authorisationId) {
        return buildPath(PSU_AUTHORISATION_URL, paymentService, paymentId, authorisationId);
    }

    private boolean isScaStatusFinalised(String scaAuthenticationData, ScaStatus scaStatus) {
        return StringUtils.isNotBlank(scaAuthenticationData)
                   && scaStatus == ScaStatus.FINALISED;
    }

    private boolean isScaStatusMethodSelected(String authenticationMethodId, ScaStatus scaStatus) {
        return StringUtils.isNotBlank(authenticationMethodId)
                   && scaStatus == ScaStatus.SCAMETHODSELECTED;
    }

    private boolean isScaStatusMethodAuthenticated(ScaStatus scaStatus) {
        return scaStatus == ScaStatus.PSUAUTHENTICATED;
    }
}
