/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service.link;

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponseType;
import de.adorsys.psd2.xs2a.domain.authorisation.CancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.controller.PaymentController;
import de.adorsys.psd2.xs2a.web.link.PisAuthorisationCancellationLinks;
import de.adorsys.psd2.xs2a.web.link.UpdatePisCancellationPsuDataLinks;
import de.adorsys.psd2.xs2a.web.link.holder.LinkParameters;
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
                LinkParameters linkParameters = LinkParameters.builder().httpUrl(getHttpUrl())
                    .paymentService(request.getPaymentService().getValue())
                    .paymentProduct(request.getPaymentProduct())
                    .paymentId(request.getPaymentId())
                    .authorisationId(body.getAuthorisationId())
                    .internalRequestId(body.getInternalRequestId())
                    .instanceId(requestProviderService.getInstanceId()).build();
                response.setLinks(new PisAuthorisationCancellationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder,
                                                                        redirectIdService, getScaRedirectFlow()));
            } else if (authorisationResponseType == AuthorisationResponseType.UPDATE) {
                Xs2aUpdatePisCommonPaymentPsuDataResponse response = (Xs2aUpdatePisCommonPaymentPsuDataResponse) result.getBody();
                PaymentAuthorisationParameters updateRequest = buildXs2aUpdatePisCommonPaymentPsuDataRequest(request.getPaymentId(),
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
                                                                                                              PaymentAuthorisationParameters request) {
        if (!result.hasError()) {
            Xs2aUpdatePisCommonPaymentPsuDataResponse body = result.getBody();
            body.setLinks(new UpdatePisCancellationPsuDataLinks(getHttpUrl(), scaApproachResolver, request,
                                                                body.getScaStatus(), body.getChosenScaMethod()));
        }

        return result;
    }

    private PaymentAuthorisationParameters buildXs2aUpdatePisCommonPaymentPsuDataRequest(String paymentId,
                                                                                         String authorisationId,
                                                                                         PsuIdData psuIdData,
                                                                                         String paymentProduct,
                                                                                         PaymentType paymentService,
                                                                                         String password) {
        PaymentAuthorisationParameters updateRequest = new PaymentAuthorisationParameters();
        updateRequest.setPaymentId(paymentId);
        updateRequest.setAuthorisationId(authorisationId);
        updateRequest.setPsuData(psuIdData);
        updateRequest.setPassword(password);
        updateRequest.setPaymentProduct(paymentProduct);
        updateRequest.setPaymentService(paymentService);
        return updateRequest;
    }
}
