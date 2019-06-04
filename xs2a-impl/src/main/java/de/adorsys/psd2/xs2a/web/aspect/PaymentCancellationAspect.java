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

package de.adorsys.psd2.xs2a.web.aspect;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.PaymentCancellationAuthorisationNeededDecider;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.controller.PaymentController;
import de.adorsys.psd2.xs2a.web.link.PaymentCancellationLinks;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PaymentCancellationAspect extends AbstractLinkAspect<PaymentController> {
    private final PaymentCancellationAuthorisationNeededDecider cancellationScaNeededDecider;
    private ScaApproachResolver scaApproachResolver;
    private final RedirectLinkBuilder redirectLinkBuilder;
    private AuthorisationMethodDecider authorisationMethodDecider;

    public PaymentCancellationAspect(MessageService messageService, PaymentCancellationAuthorisationNeededDecider cancellationScaNeededDecider,
                                     AspspProfileService aspspProfileService,
                                     ScaApproachResolver scaApproachResolver,
                                     RedirectLinkBuilder redirectLinkBuilder,
                                     AuthorisationMethodDecider authorisationMethodDecider) {
        super(messageService, aspspProfileService);
        this.cancellationScaNeededDecider = cancellationScaNeededDecider;
        this.scaApproachResolver = scaApproachResolver;
        this.redirectLinkBuilder = redirectLinkBuilder;
        this.authorisationMethodDecider = authorisationMethodDecider;
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.PaymentService.cancelPayment(..)) && args( paymentType, paymentProduct, paymentId,tppExplicitAuthorisationPreferred)", returning = "result", argNames = "result,paymentType,paymentProduct,paymentId,tppExplicitAuthorisationPreferred")
    public ResponseObject<CancelPaymentResponse> cancelPayment(ResponseObject<CancelPaymentResponse> result, PaymentType paymentType, String paymentProduct, String paymentId, Boolean tppExplicitAuthorisationPreferred) {
        if (!result.hasError()) {
            CancelPaymentResponse response = result.getBody();

            boolean isScaRequired = cancellationScaNeededDecider.isScaRequired(response.isStartAuthorisationRequired());
            if (isStartAuthorisationLinksNeeded(isScaRequired, response.getTransactionStatus())) {

                // in payment cancellation case 'multilevelScaRequired' is always false
                boolean isExplicitMethod = authorisationMethodDecider.isExplicitMethod(tppExplicitAuthorisationPreferred, false);
                response.setLinks(new PaymentCancellationLinks(getHttpUrl(), scaApproachResolver, redirectLinkBuilder,
                                                               response, isExplicitMethod));
            }

            return result;
        }
        return enrichErrorTextMessage(result);
    }

    private boolean isStartAuthorisationLinksNeeded(boolean isScaRequired, TransactionStatus transactionStatus) {
        return transactionStatus.isNotFinalisedStatus()
                   && transactionStatus != TransactionStatus.RCVD
                   && isScaRequired;
    }
}
