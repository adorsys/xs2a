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

import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.PaymentCancellationAuthorisationNeededDecider;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.psd2.xs2a.web.controller.PaymentController;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PaymentCancellationAspect extends AbstractLinkAspect<PaymentController> {
    private final PaymentCancellationAuthorisationNeededDecider cancellationScaNeededDecider;

    public PaymentCancellationAspect(ScaApproachResolver scaApproachResolver, MessageService messageService, PaymentCancellationAuthorisationNeededDecider cancellationScaNeededDecider) {
        super(scaApproachResolver, messageService);
        this.cancellationScaNeededDecider = cancellationScaNeededDecider;
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.PaymentService.cancelPayment(..)) && args( paymentType, paymentProduct, paymentId)", returning = "result", argNames = "result,paymentType,paymentProduct,paymentId")
    public ResponseObject<CancelPaymentResponse> cancelPayment(ResponseObject<CancelPaymentResponse> result, PaymentType paymentType, String paymentProduct, String paymentId) {
        if (!result.hasError()) {
            CancelPaymentResponse response = result.getBody();
            response.setLinks(buildCancellationLinks(response, paymentType, paymentProduct, paymentId));
            return result;
        }
        return enrichErrorTextMessage(result);
    }

    private Links buildCancellationLinks(CancelPaymentResponse response, PaymentType paymentType, String paymentProduct, String paymentId) {
        Links links = new Links();

        if (isStartAuthorisationLinksNeeded(response)) {
            links.setStartAuthorisation(buildPath("/v1/{payment-service}/{payment-product}/{payment-id}/cancellation-authorisations", paymentType.getValue(), paymentProduct, paymentId));
            links.setSelf(buildPath("/v1/{payment-service}/{payment-product}/{payment-id}", paymentType.getValue(), paymentProduct, paymentId));
            links.setStatus(buildPath("/v1/{payment-service}/{payment-product}/{payment-id}/status", paymentType.getValue(), paymentProduct, paymentId));
        }
        return links;
    }

    private boolean isStartAuthorisationLinksNeeded(CancelPaymentResponse response) {
        return response.getTransactionStatus().isNotFinalisedStatus()
                   && response.getTransactionStatus() != TransactionStatus.RCVD
                   && cancellationScaNeededDecider.isScaRequired(response.isStartAuthorisationRequired());
    }
}
