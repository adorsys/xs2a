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

import de.adorsys.aspsp.xs2a.component.JsonConverter;
import de.adorsys.aspsp.xs2a.consent.api.CmsScaStatus;
import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.consent.Xs2aChosenScaMethod;
import de.adorsys.aspsp.xs2a.domain.consent.Xs2aUpdatePisConsentPsuDataResponse;
import de.adorsys.aspsp.xs2a.service.message.MessageService;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.aspsp.xs2a.web.PaymentController;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class UpdatePisConsentPsuDataAspect extends AbstractLinkAspect<PaymentController> {
    private final static String PSU_AUTHORISATION_URL = "/v1/{paymentService}/{paymentId}/authorisations/{authorisationId}";

    public UpdatePisConsentPsuDataAspect(int maxNumberOfCharInTransactionJson, AspspProfileServiceWrapper aspspProfileService, JsonConverter jsonConverter, MessageService messageService) {
        super(maxNumberOfCharInTransactionJson, aspspProfileService, jsonConverter, messageService);
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
                body.setChosenScaMethod(getChosenScaMethod(request.getAuthenticationMethodId()));
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

    private Xs2aChosenScaMethod getChosenScaMethod(String authenticationMethodId) {
        Xs2aChosenScaMethod method = new Xs2aChosenScaMethod();
        method.setAuthenticationMethodId(authenticationMethodId);
        method.setAuthenticationType(authenticationMethodId);
        return method;
    }

    private String buildAuthorisationLink(String paymentService, String paymentId, String authorisationId) {
        return buildPath(PSU_AUTHORISATION_URL, paymentService, paymentId, authorisationId);
    }

    private boolean isScaStatusFinalised(String scaAuthenticationData, CmsScaStatus scaStatus) {
        return StringUtils.isNotBlank(scaAuthenticationData)
                   && scaStatus == CmsScaStatus.FINALISED;
    }

    private boolean isScaStatusMethodSelected(String authenticationMethodId, CmsScaStatus scaStatus) {
        return StringUtils.isNotBlank(authenticationMethodId)
                   && scaStatus == CmsScaStatus.SCAMETHODSELECTED;
    }

    private boolean isScaStatusMethodAuthenticated(CmsScaStatus scaStatus) {
        return scaStatus == CmsScaStatus.PSUAUTHENTICATED;
    }
}
