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

import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPisCommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.BulkPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aToCmsPisCommonPaymentRequestMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateBulkPaymentService implements CreatePaymentService<BulkPayment, BulkPaymentInitiationResponse> {
    private final ScaPaymentService scaPaymentService;
    private final Xs2aPisCommonPaymentService pisCommonPaymentService;
    private final AuthorisationMethodDecider authorisationMethodDecider;
    private final PisScaAuthorisationService pisScaAuthorisationService;
    private final Xs2aPisCommonPaymentMapper xs2aPisCommonPaymentMapper;
    private final Xs2aToCmsPisCommonPaymentRequestMapper xs2aToCmsPisCommonPaymentRequestMapper;
    private final PisAspspDataService pisAspspDataService;

    /**
     * Initiates bulk payment
     *
     * @param bulkPayment                 Periodic payment information
     * @param paymentInitiationParameters payment initiation parameters
     * @param tppInfo                     information about particular TPP
     * @return Response containing information about created periodic payment or corresponding error
     */
    @Override
    public ResponseObject<BulkPaymentInitiationResponse> createPayment(BulkPayment bulkPayment, PaymentInitiationParameters paymentInitiationParameters, TppInfo tppInfo) {

        PsuIdData psuData = paymentInitiationParameters.getPsuData();

        BulkPaymentInitiationResponse response = scaPaymentService.createBulkPayment(bulkPayment, tppInfo, paymentInitiationParameters.getPaymentProduct(), psuData);

        PisPaymentInfo pisPaymentInfo = xs2aToCmsPisCommonPaymentRequestMapper.mapToPisPaymentInfo(paymentInitiationParameters, tppInfo, response.getTransactionStatus(), response.getPaymentId());
        Xs2aPisCommonPayment pisCommonPayment = xs2aPisCommonPaymentMapper.mapToXs2aPisCommonPayment(pisCommonPaymentService.createCommonPayment(pisPaymentInfo), psuData);

        String externalPaymentId = pisCommonPayment.getPaymentId();

        if (StringUtils.isBlank(externalPaymentId)) {
            return ResponseObject.<BulkPaymentInitiationResponse>builder()
                       .fail(new MessageError(MessageErrorCode.PAYMENT_FAILED))
                       .build();
        }

        AspspConsentData aspspConsentData = response.getAspspConsentData();
        pisAspspDataService.updateAspspConsentData(new AspspConsentData(aspspConsentData.getAspspConsentData(), externalPaymentId));

        bulkPayment.setTransactionStatus(response.getTransactionStatus());
        bulkPayment.setPaymentId(response.getPaymentId());

        BulkPayment bulkPaymentUpdated = setRandomIdsToPaymentListInBulkPayment(bulkPayment);
        pisCommonPaymentService.updateBulkPaymentInCommonPayment(bulkPaymentUpdated, paymentInitiationParameters, pisCommonPayment.getPaymentId());

        response.setPaymentId(externalPaymentId);

        boolean implicitMethod = authorisationMethodDecider.isImplicitMethod(paymentInitiationParameters.isTppExplicitAuthorisationPreferred());
        if (implicitMethod) {
            Optional<Xs2aCreatePisAuthorisationResponse> consentAuthorisation = pisScaAuthorisationService.createCommonPaymentAuthorisation(externalPaymentId, PaymentType.BULK, paymentInitiationParameters.getPsuData());
            if (!consentAuthorisation.isPresent()) {
                return ResponseObject.<BulkPaymentInitiationResponse>builder()
                           .fail(new MessageError(MessageErrorCode.PAYMENT_FAILED))
                           .build();
            }
            Xs2aCreatePisAuthorisationResponse authorisationResponse = consentAuthorisation.get();
            response.setAuthorizationId(authorisationResponse.getAuthorisationId());
            response.setScaStatus(authorisationResponse.getScaStatus());
        }

        return ResponseObject.<BulkPaymentInitiationResponse>builder()
                   .body(response)
                   .build();
    }

    private BulkPayment setRandomIdsToPaymentListInBulkPayment(BulkPayment bulkPayment) {
        List<SinglePayment> payments = bulkPayment.getPayments();

        for (SinglePayment payment : payments) {
            payment.setPaymentId(UUID.randomUUID().toString());
        }

        bulkPayment.setPayments(payments);
        return bulkPayment;
    }
}
