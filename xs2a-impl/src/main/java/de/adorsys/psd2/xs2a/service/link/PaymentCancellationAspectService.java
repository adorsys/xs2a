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
import de.adorsys.psd2.xs2a.web.link.holder.LinkParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentCancellationAspectService extends BaseAspectService<PaymentController> {
    private final PaymentCancellationAuthorisationNeededDecider cancellationScaNeededDecider;
    private final ScaApproachResolver scaApproachResolver;
    private final RedirectLinkBuilder redirectLinkBuilder;
    private final AuthorisationMethodDecider authorisationMethodDecider;
    private final RedirectIdService redirectIdService;
    private final RequestProviderService requestProviderService;

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
                LinkParameters linkParameters = LinkParameters.builder()
                    .httpUrl(getHttpUrl())
                    .isExplicitMethod(isExplicitMethod)
                    .isAuthorisationConfirmationRequestMandated(isAuthorisationConfirmationRequestMandated())
                    .instanceId(requestProviderService.getInstanceId())
                    .build();
                response.setLinks(new PaymentCancellationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder,
                                                               redirectIdService, response, getScaRedirectFlow()));
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
