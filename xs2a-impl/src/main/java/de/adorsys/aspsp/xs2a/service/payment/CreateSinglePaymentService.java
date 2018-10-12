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

package de.adorsys.aspsp.xs2a.service.payment;

import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppInfo;
import de.adorsys.aspsp.xs2a.domain.consent.Xsa2CreatePisConsentAuthorisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.authorization.AuthorisationMethodService;
import de.adorsys.aspsp.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.aspsp.xs2a.service.consent.PisConsentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CreateSinglePaymentService implements CreatePaymentService<SinglePayment, SinglePaymentInitiateResponse> {
    private final ScaPaymentService scaPaymentService;
    private final PisConsentService pisConsentService;
    private final PisScaAuthorisationService pisScaAuthorisationService;
    private final AuthorisationMethodService authorisationMethodService;

    /**
     * Initiates single payment
     *
     * @param singlePayment Single payment information
     * @param paymentProduct  The addressed payment product
     * @param tppExplicitAuthorisationPreferred  If it equals "true", the TPP prefers a redirect over an embedded SCA approach.
     * If it equals "false", the TPP prefers not to be redirected for SCA.
     * @param consentId  consent identification
     * @param tppInfo  information about particular TPP
     * @return Response containing information about created periodic payment or corresponding error
     */
    @Override
    public ResponseObject<SinglePaymentInitiateResponse> createPayment(SinglePayment singlePayment, PaymentProduct paymentProduct, boolean tppExplicitAuthorisationPreferred, String consentId, TppInfo tppInfo) {
        SinglePaymentInitiateResponse response = scaPaymentService.createSinglePayment(singlePayment, tppInfo, paymentProduct);
        response.setPisConsentId(consentId);

        updateSinglePaymentInPisConsent(singlePayment, paymentProduct, consentId, response);

        boolean implicitMethod = authorisationMethodService.isImplicitMethod(tppExplicitAuthorisationPreferred);
        if (implicitMethod) {
            Optional<Xsa2CreatePisConsentAuthorisationResponse> consentAuthorisation = pisScaAuthorisationService.createConsentAuthorisation(response.getPaymentId(), PaymentType.SINGLE);
            if (!consentAuthorisation.isPresent()) {
                return ResponseObject.<SinglePaymentInitiateResponse>builder()
                           .fail(new MessageError(MessageErrorCode.CONSENT_INVALID))
                           .build();
            }
            Xsa2CreatePisConsentAuthorisationResponse authorisationResponse = consentAuthorisation.get();
            response.setAuthorizationId(authorisationResponse.getAuthorizationId());
            response.setScaStatus(authorisationResponse.getScaStatus());
        }
        return ResponseObject.<SinglePaymentInitiateResponse>builder()
                   .body(response)
                   .build();
    }

    private void updateSinglePaymentInPisConsent(SinglePayment singlePayment, PaymentProduct paymentProduct, String consentId, SinglePaymentInitiateResponse response) {
        singlePayment.setPaymentId(response.getPaymentId());
        singlePayment.setTransactionStatus(response.getTransactionStatus());

        pisConsentService.updatePaymentInPisConsent(singlePayment, paymentProduct, consentId);
    }
}
