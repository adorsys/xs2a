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

import de.adorsys.psd2.consent.api.pis.proto.PisPaymentCancellationRequest;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.PaymentCancellationAuthorisationNeededDecider;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.controller.PaymentController;
import de.adorsys.psd2.xs2a.web.link.PaymentCancellationLinks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentCancellationAspectService extends BaseAspectService<PaymentController> {
    private PaymentCancellationAuthorisationNeededDecider cancellationScaNeededDecider;
    private ScaApproachResolver scaApproachResolver;
    private RedirectLinkBuilder redirectLinkBuilder;
    private AuthorisationMethodDecider authorisationMethodDecider;
    private RedirectIdService redirectIdService;
    private RequestProviderService requestProviderService;

    @Autowired
    public PaymentCancellationAspectService(PaymentCancellationAuthorisationNeededDecider cancellationScaNeededDecider,
                                            AspspProfileServiceWrapper aspspProfileServiceWrapper,
                                            ScaApproachResolver scaApproachResolver,
                                            RedirectLinkBuilder redirectLinkBuilder,
                                            AuthorisationMethodDecider authorisationMethodDecider,
                                            RedirectIdService redirectIdService,
                                            RequestProviderService requestProviderService) {
        super(aspspProfileServiceWrapper);
        this.cancellationScaNeededDecider = cancellationScaNeededDecider;
        this.scaApproachResolver = scaApproachResolver;
        this.redirectLinkBuilder = redirectLinkBuilder;
        this.authorisationMethodDecider = authorisationMethodDecider;
        this.redirectIdService = redirectIdService;
        this.requestProviderService = requestProviderService;
    }

    public ResponseObject<CancelPaymentResponse> cancelPayment(ResponseObject<CancelPaymentResponse> result, PisPaymentCancellationRequest request) {
        if (!result.hasError()) {
            CancelPaymentResponse response = result.getBody();

            boolean isScaRequired = cancellationScaNeededDecider.isScaRequired(response.isStartAuthorisationRequired());
            if (isStartAuthorisationLinksNeeded(isScaRequired, response.getTransactionStatus())) {

                // in payment cancellation case 'multilevelScaRequired' is always false
                boolean isExplicitMethod = authorisationMethodDecider.isExplicitMethod(request.getTppExplicitAuthorisationPreferred(), false);
                response.setLinks(new PaymentCancellationLinks(getHttpUrl(), scaApproachResolver, redirectLinkBuilder,
                                                               redirectIdService, response, isExplicitMethod,
                                                               isAuthorisationConfirmationRequestMandated(),
                                                               requestProviderService.getInstanceId()));
            }

        }
        return result;
    }

    private boolean isStartAuthorisationLinksNeeded(boolean isScaRequired, TransactionStatus transactionStatus) {
        return transactionStatus.isNotFinalisedStatus()
                   && transactionStatus != TransactionStatus.RCVD
                   && isScaRequired;
    }
}
