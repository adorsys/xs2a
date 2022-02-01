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
