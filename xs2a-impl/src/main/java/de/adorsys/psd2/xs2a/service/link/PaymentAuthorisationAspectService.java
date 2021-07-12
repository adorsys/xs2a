/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.link;

import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.controller.PaymentController;
import de.adorsys.psd2.xs2a.web.link.CreatePisAuthorisationLinks;
import de.adorsys.psd2.xs2a.web.link.UpdatePisAuthorisationLinks;
import de.adorsys.psd2.xs2a.web.link.UpdatePisPsuDataLinks;
import de.adorsys.psd2.xs2a.web.link.holder.LinkParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentAuthorisationAspectService extends BaseAspectService<PaymentController> {
    private ScaApproachResolver scaApproachResolver;
    private RedirectLinkBuilder redirectLinkBuilder;
    private RedirectIdService redirectIdService;
    private RequestProviderService requestProviderService;

    @Autowired
    public PaymentAuthorisationAspectService(ScaApproachResolver scaApproachResolver,
                                             AspspProfileServiceWrapper aspspProfileServiceWrapper, RedirectLinkBuilder redirectLinkBuilder,
                                             RedirectIdService redirectIdService, RequestProviderService requestProviderService) {
        super(aspspProfileServiceWrapper);
        this.scaApproachResolver = scaApproachResolver;
        this.redirectLinkBuilder = redirectLinkBuilder;
        this.redirectIdService = redirectIdService;
        this.requestProviderService = requestProviderService;
    }

    public ResponseObject<AuthorisationResponse> createPisAuthorizationAspect(ResponseObject<AuthorisationResponse> result,
                                                                              Xs2aCreatePisAuthorisationRequest request) {
        if (!result.hasError()) {
            if (result.getBody() instanceof Xs2aCreatePisAuthorisationResponse) {
                Xs2aCreatePisAuthorisationResponse response = (Xs2aCreatePisAuthorisationResponse) result.getBody();

                LinkParameters linkParameters = LinkParameters.builder()
                    .httpUrl(getHttpUrl())
                    .authorisationId(response.getAuthorisationId())
                    .internalRequestId(response.getInternalRequestId())
                    .isAuthorisationConfirmationRequestMandated(isAuthorisationConfirmationRequestMandated())
                    .instanceId(requestProviderService.getInstanceId())
                    .build();
                response.setLinks(new CreatePisAuthorisationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder,
                                                                  redirectIdService, request,
                                                                  getScaRedirectFlow()));
            } else if (result.getBody() instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse) {
                Xs2aUpdatePisCommonPaymentPsuDataResponse response = (Xs2aUpdatePisCommonPaymentPsuDataResponse) result.getBody();
                response.setLinks(new UpdatePisAuthorisationLinks(getHttpUrl(), scaApproachResolver, response, request));
            }
        }
        return result;
    }

    public ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> updatePisAuthorizationAspect(ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> result,
                                                                                                  PaymentAuthorisationParameters request) {
        if (!result.hasError()) {
            Xs2aUpdatePisCommonPaymentPsuDataResponse body = result.getBody();
            body.setLinks(new UpdatePisPsuDataLinks(getHttpUrl(), scaApproachResolver, request, body.getScaStatus(),
                                                    body.getChosenScaMethod()));
        }

        return result;
    }
}
