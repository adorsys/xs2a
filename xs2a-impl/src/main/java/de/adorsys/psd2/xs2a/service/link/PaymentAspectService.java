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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentAspectService extends BaseAspectService<PaymentController> {
    private ScaApproachResolver scaApproachResolver;
    private AuthorisationMethodDecider authorisationMethodDecider;
    private RedirectLinkBuilder redirectLinkBuilder;
    private RedirectIdService redirectIdService;
    private RequestProviderService requestProviderService;

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

    public ResponseObject<PaymentInitiationResponse> createPaymentAspect(ResponseObject<PaymentInitiationResponse> result, PaymentInitiationParameters requestParameters) {
        if (!result.hasError()) {
            PaymentInitiationResponse body = result.getBody();
            boolean explicitPreferred = requestParameters.isTppExplicitAuthorisationPreferred();
            boolean explicitMethod = authorisationMethodDecider.isExplicitMethod(explicitPreferred, body.isMultilevelScaRequired());
            boolean signingBasketModeActive = authorisationMethodDecider.isSigningBasketModeActive(explicitPreferred);

            body.setLinks(new PaymentInitiationLinks(getHttpUrl(), scaApproachResolver, redirectLinkBuilder,
                                                     redirectIdService,
                                                     requestParameters, body, explicitMethod, signingBasketModeActive,
                                                     isAuthorisationConfirmationRequestMandated(), requestProviderService.getInstanceId()));
        }
        return result;
    }
}
