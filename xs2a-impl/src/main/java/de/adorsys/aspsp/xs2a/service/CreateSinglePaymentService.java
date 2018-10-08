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

package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppInfo;
import de.adorsys.aspsp.xs2a.domain.consent.Xsa2CreatePisConsentAuthorisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentType;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayment;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.authorization.AuthorisationMethodService;
import de.adorsys.aspsp.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.aspsp.xs2a.service.consent.PisConsentService;
import de.adorsys.aspsp.xs2a.service.payment.ScaPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.EXECUTION_DATE_INVALID;
import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.FORMAT_ERROR;

@Service
@RequiredArgsConstructor
public class CreateSinglePaymentService implements CreatePaymentService<PaymentInitialisationResponse, SinglePayment> {
    private final ScaPaymentService scaPaymentService;
    private final PisConsentService pisConsentService;
    private final PisScaAuthorisationService pisScaAuthorisationService;
    private final AuthorisationMethodService authorisationMethodService;
    private final AccountReferenceValidationService referenceValidationService;

    /**
     * Initiates a single payment
     *
     * @param payment        Single payment information
     * @param paymentProduct The addressed payment product
     * @return Response containing information about created single payment or corresponding error
     */
    @Override
    public ResponseObject<PaymentInitialisationResponse> createPayment(SinglePayment payment, String paymentProduct, boolean tppExplicitAuthorisationPreferred, String consentId, TppInfo tppInfo) {
        ResponseObject<SinglePayment> paymentResponse = createSinglePayment(payment, tppInfo, paymentProduct);
        SinglePayment singlePayment = paymentResponse.getBody();

        pisConsentService.updatePisConsentSinglePayment(singlePayment, paymentProduct, consentId);

        PaymentInitialisationResponse response = new PaymentInitialisationResponse();
        response.setPaymentId(singlePayment.getPaymentId());
        response.setPisConsentId(consentId);
        response.setPaymentType(PaymentType.SINGLE.name());
        response.setTransactionStatus(singlePayment.getTransactionStatus());

        boolean implicitMethod = authorisationMethodService.isImplicitMethod(tppExplicitAuthorisationPreferred);
        if (implicitMethod) {
            Optional<Xsa2CreatePisConsentAuthorisationResponse> consentAuthorisation = pisScaAuthorisationService.createConsentAuthorisation(singlePayment.getPaymentId(), PaymentType.SINGLE);
            if (!consentAuthorisation.isPresent()) {
                return ResponseObject.<PaymentInitialisationResponse>builder()
                           .fail(new MessageError(MessageErrorCode.CONSENT_INVALID))
                           .build();
            }
            Xsa2CreatePisConsentAuthorisationResponse authorisationResponse = consentAuthorisation.get();
            response.setAuthorizationId(authorisationResponse.getAuthorizationId());
            response.setScaStatus(authorisationResponse.getScaStatus());
        }

        return ResponseObject.<PaymentInitialisationResponse>builder()
                   .body(response)
                   .build();
    }

    private ResponseObject<SinglePayment> createSinglePayment(SinglePayment singlePayment, TppInfo tppInfo, String paymentProduct) {
        MessageErrorCode messageErrorCode = validateSinglePayment(singlePayment, singlePayment.isValidExecutionDateAndTime());
        if (messageErrorCode != null) {
            return ResponseObject.<SinglePayment>builder()
                       .fail(new MessageError(messageErrorCode))
                       .build();
        }
        return ResponseObject.<SinglePayment>builder()
                   .body(scaPaymentService.createSinglePayment(singlePayment, tppInfo, paymentProduct))
                   .build();
    }

    private MessageErrorCode validateSinglePayment(SinglePayment payment, boolean areValidDates) {
        if (!areValidDates) {
            return EXECUTION_DATE_INVALID;
        }
        if (referenceValidationService.isValidateAccountReferences(payment.getAccountReferences())) {
            return FORMAT_ERROR;
        }
        return null;
    }
}
