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
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.controller.PaymentController;
import de.adorsys.psd2.xs2a.web.link.PaymentInitiationLinks;
import de.adorsys.psd2.xs2a.web.link.holder.LinkParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentAspectService extends BaseAspectService<PaymentController> {
    private final ScaApproachResolver scaApproachResolver;
    private final AuthorisationMethodDecider authorisationMethodDecider;
    private final RedirectLinkBuilder redirectLinkBuilder;
    private final RedirectIdService redirectIdService;
    private final RequestProviderService requestProviderService;

    @Autowired
    public PaymentAspectService(ScaApproachResolver scaApproachResolver,
                                AuthorisationMethodDecider authorisationMethodDecider, RedirectLinkBuilder redirectLinkBuilder,
                                AspspProfileServiceWrapper aspspProfileServiceWrapper, RedirectIdService redirectIdService,
                                RequestProviderService requestProviderService) {
        super(aspspProfileServiceWrapper);
        this.scaApproachResolver = scaApproachResolver;
        this.authorisationMethodDecider = authorisationMethodDecider;
        this.redirectLinkBuilder = redirectLinkBuilder;
        this.redirectIdService = redirectIdService;
        this.requestProviderService = requestProviderService;
    }

    public ResponseObject<PaymentInitiationResponse> createPaymentAspect(ResponseObject<PaymentInitiationResponse> result,
                                                                         PaymentInitiationParameters requestParameters) {
        if (!result.hasError()) {
            PaymentInitiationResponse body = result.getBody();
            boolean explicitPreferred = requestParameters.isTppExplicitAuthorisationPreferred();
            boolean isExplicitMethod = authorisationMethodDecider.isExplicitMethod(explicitPreferred, body.isMultilevelScaRequired());
            boolean isSigningBasketModeActive = authorisationMethodDecider.isSigningBasketModeActive(explicitPreferred);

            LinkParameters linkParameters =  LinkParameters.builder()
                .httpUrl(getHttpUrl())
                .isExplicitMethod(isExplicitMethod)
                .isSigningBasketModeActive(isSigningBasketModeActive)
                .isAuthorisationConfirmationRequestMandated(isAuthorisationConfirmationRequestMandated())
                .instanceId(requestProviderService.getInstanceId())
                .build();

            body.setLinks(new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder,
                                                     redirectIdService, requestParameters, body, getScaRedirectFlow()));
        }
        return result;
    }
}
