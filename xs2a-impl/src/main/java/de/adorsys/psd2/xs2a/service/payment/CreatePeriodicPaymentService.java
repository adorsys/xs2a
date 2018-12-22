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

package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPisCommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisCommonPaymentMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CreatePeriodicPaymentService implements CreatePaymentService<PeriodicPayment, PeriodicPaymentInitiationResponse> {
    private final ScaPaymentService scaPaymentService;
    private final Xs2aPisCommonPaymentService pisCommonPaymentService;
    private final AuthorisationMethodDecider authorisationMethodDecider;
    private final PisScaAuthorisationService pisScaAuthorisationService;
    private final PisAspspDataService pisAspspDataService;
    private final Xs2aPisCommonPaymentMapper xs2aPisCommonPaymentMapper;

    /**
     * Initiates periodic payment
     *
     * @param periodicPayment             Periodic payment information
     * @param paymentInitiationParameters payment initiation parameters
     * @param tppInfo                     information about particular TPP
     * @return Response containing information about created periodic payment or corresponding error
     */
    @Override
    public ResponseObject<PeriodicPaymentInitiationResponse> createPayment(PeriodicPayment periodicPayment, PaymentInitiationParameters paymentInitiationParameters, TppInfo tppInfo) {
        Xs2aPisCommonPayment pisCommonPayment = xs2aPisCommonPaymentMapper.mapToXs2aPisCommonPayment(pisCommonPaymentService.createCommonPayment(paymentInitiationParameters, tppInfo), paymentInitiationParameters.getPsuData());

        if (StringUtils.isBlank(pisCommonPayment.getPaymentId())) {
            return ResponseObject.<PeriodicPaymentInitiationResponse>builder()
                       .fail(new MessageError(MessageErrorCode.PAYMENT_FAILED))
                       .build();
        }

        String externalPaymentId = pisCommonPayment.getPaymentId();

        // we need to get decrypted payment ID
        String internalPaymentId = pisAspspDataService.getInternalPaymentIdByEncryptedString(externalPaymentId);
        periodicPayment.setPaymentId(internalPaymentId);

        PeriodicPaymentInitiationResponse response = scaPaymentService.createPeriodicPayment(periodicPayment, tppInfo, paymentInitiationParameters.getPaymentProduct(), pisCommonPayment);
        response.setPaymentId(pisCommonPayment.getPaymentId());

        periodicPayment.setTransactionStatus(response.getTransactionStatus());

        pisCommonPaymentService.updatePeriodicPaymentInCommonPayment(periodicPayment, paymentInitiationParameters, pisCommonPayment.getPaymentId());

        boolean implicitMethod = authorisationMethodDecider.isImplicitMethod(paymentInitiationParameters.isTppExplicitAuthorisationPreferred());
        if (implicitMethod) {
            Optional<Xs2aCreatePisAuthorisationResponse> consentAuthorisation = pisScaAuthorisationService.createCommonPaymentAuthorisation(externalPaymentId, PaymentType.PERIODIC, paymentInitiationParameters.getPsuData());
            if (!consentAuthorisation.isPresent()) {
                return ResponseObject.<PeriodicPaymentInitiationResponse>builder()
                           .fail(new MessageError(MessageErrorCode.PAYMENT_FAILED))
                           .build();
            }
            Xs2aCreatePisAuthorisationResponse authorisationResponse = consentAuthorisation.get();
            response.setAuthorizationId(authorisationResponse.getAuthorisationId());
            response.setScaStatus(authorisationResponse.getScaStatus());
        }

        // we need to return encrypted payment ID
        response.setPaymentId(externalPaymentId);

        return ResponseObject.<PeriodicPaymentInitiationResponse>builder()
                   .body(response)
                   .build();
    }
}
