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

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponseType;
import de.adorsys.psd2.xs2a.domain.authorisation.CancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.controller.PaymentController;
import de.adorsys.psd2.xs2a.web.link.PisAuthorisationCancellationLinks;
import de.adorsys.psd2.xs2a.web.link.UpdatePisCancellationPsuDataLinks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentAuthorisationCancellationAspectService extends BaseAspectService<PaymentController> {
    private ScaApproachResolver scaApproachResolver;
    private RedirectLinkBuilder redirectLinkBuilder;
    private RedirectIdService redirectIdService;
    private RequestProviderService requestProviderService;

    @Autowired
    public PaymentAuthorisationCancellationAspectService(ScaApproachResolver scaApproachResolver,
                                                         RedirectLinkBuilder redirectLinkBuilder, AspspProfileServiceWrapper aspspProfileServiceWrapper,
                                                         RedirectIdService redirectIdService, RequestProviderService requestProviderService) {
        super(aspspProfileServiceWrapper);
        this.scaApproachResolver = scaApproachResolver;
        this.redirectLinkBuilder = redirectLinkBuilder;
        this.redirectIdService = redirectIdService;
        this.requestProviderService = requestProviderService;
    }

    public ResponseObject<CancellationAuthorisationResponse> createPisAuthorisationAspect(ResponseObject<CancellationAuthorisationResponse> result,
                                                                                          Xs2aCreatePisAuthorisationRequest request) {
        if (!result.hasError()) {

            CancellationAuthorisationResponse body = result.getBody();
            AuthorisationResponseType authorisationResponseType = body.getAuthorisationResponseType();

            if (authorisationResponseType == AuthorisationResponseType.START) {
                Xs2aCreatePisCancellationAuthorisationResponse response = (Xs2aCreatePisCancellationAuthorisationResponse) result.getBody();
                response.setLinks(new PisAuthorisationCancellationLinks(getHttpUrl(), scaApproachResolver, redirectLinkBuilder,
                                                                        redirectIdService, request.getPaymentService().getValue(),
                                                                        request.getPaymentProduct(), request.getPaymentId(),
                                                                        body.getAuthorisationId(), getScaRedirectFlow(), body.getInternalRequestId(),
                                                                        requestProviderService.getInstanceId()));
            } else if (authorisationResponseType == AuthorisationResponseType.UPDATE) {
                Xs2aUpdatePisCommonPaymentPsuDataResponse response = (Xs2aUpdatePisCommonPaymentPsuDataResponse) result.getBody();
                Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildXs2aUpdatePisCommonPaymentPsuDataRequest(request.getPaymentId(),
                                                                                                                       response.getAuthorisationId(),
                                                                                                                       request.getPsuData(),
                                                                                                                       request.getPaymentProduct(),
                                                                                                                       request.getPaymentService(),
                                                                                                                       request.getPassword());
                response.setLinks(new UpdatePisCancellationPsuDataLinks(getHttpUrl(), scaApproachResolver, updateRequest,
                                                                        body.getScaStatus(), response.getChosenScaMethod()));
            } else {
                throw new IllegalArgumentException("Unknown authorisation response type: " + authorisationResponseType);
            }
        }
        return result;
    }

    public ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> updatePisCancellationAuthorizationAspect(ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> result,
                                                                                                              Xs2aUpdatePisCommonPaymentPsuDataRequest request) {
        if (!result.hasError()) {
            Xs2aUpdatePisCommonPaymentPsuDataResponse body = result.getBody();
            body.setLinks(new UpdatePisCancellationPsuDataLinks(getHttpUrl(), scaApproachResolver, request,
                                                                body.getScaStatus(), body.getChosenScaMethod()));
        }

        return result;
    }

    private Xs2aUpdatePisCommonPaymentPsuDataRequest buildXs2aUpdatePisCommonPaymentPsuDataRequest(String paymentId,
                                                                                                   String authorisationId,
                                                                                                   PsuIdData psuIdData,
                                                                                                   String paymentProduct,
                                                                                                   PaymentType paymentService,
                                                                                                   String password) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
        updateRequest.setPaymentId(paymentId);
        updateRequest.setAuthorisationId(authorisationId);
        updateRequest.setPsuData(psuIdData);
        updateRequest.setPassword(password);
        updateRequest.setPaymentProduct(paymentProduct);
        updateRequest.setPaymentService(paymentService);
        return updateRequest;
    }
}
