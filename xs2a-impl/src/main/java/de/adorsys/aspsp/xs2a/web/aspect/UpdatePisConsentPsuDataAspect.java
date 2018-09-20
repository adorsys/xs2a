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
import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.consent.Xs2aUpdatePisConsentPsuDataResponse;
import de.adorsys.aspsp.xs2a.service.message.MessageService;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileService;
import de.adorsys.aspsp.xs2a.web12.PaymentController12;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class UpdatePisConsentPsuDataAspect extends AbstractLinkAspect<PaymentController12> {
    public UpdatePisConsentPsuDataAspect(int maxNumberOfCharInTransactionJson, AspspProfileService aspspProfileService, JsonConverter jsonConverter, MessageService messageService) {
        super(maxNumberOfCharInTransactionJson, aspspProfileService, jsonConverter, messageService);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.aspsp.xs2a.service.ConsentService.updatePisConsentPsuData(..)) && args(request)", returning = "result", argNames = "result,request")
    public ResponseObject<Xs2aUpdatePisConsentPsuDataResponse> createPisConsentAuthorizationAspect(ResponseObject<Xs2aUpdatePisConsentPsuDataResponse> result, UpdatePisConsentPsuDataRequest request) {
        if (!result.hasError()) {
            Xs2aUpdatePisConsentPsuDataResponse body = result.getBody();
            body.setLinks(buildLink(request));
            return result;
        }
        return enrichErrorTextMessage(result);
    }

    private Links buildLink(UpdatePisConsentPsuDataRequest request) {
        Links links = new Links();
        links.setSelf(buildPath("/v1/{paymentService}/{paymentId}", request.getPaymentService(), request.getPaymentId()));
        links.setStatus(buildPath("/v1/{paymentService}/{paymentId}/status", request.getPaymentService(), request.getPaymentId()));
        links.setSelectAuthenticationMethod(buildPath("/v1/{paymentService}/{paymentId}/authorisations/{authorizationId}", request.getPaymentService(), request.getPaymentId(), request.getAuthorizationId()));
        links.setUpdatePsuAuthentication(buildPath("/v1/{paymentService}/{paymentId}/authorisations/{authorizationId}", request.getPaymentService(), request.getPaymentId(), request.getAuthorizationId()));
        return links;
    }
}
