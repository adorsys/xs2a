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
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPisCommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aToCmsPisCommonPaymentRequestMapper;
import de.adorsys.psd2.xs2a.service.payment.sca.ScaPaymentService;
import de.adorsys.psd2.xs2a.service.payment.sca.ScaPaymentServiceResolver;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.PAYMENT_FAILED;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_400;

@Service
@RequiredArgsConstructor
public class CreateSinglePaymentService implements CreatePaymentService<SinglePayment, SinglePaymentInitiationResponse> {
    private final ScaPaymentServiceResolver scaPaymentServiceResolver;
    private final Xs2aPisCommonPaymentService pisCommonPaymentService;
    private final PisScaAuthorisationServiceResolver pisScaAuthorisationServiceResolver;
    private final AuthorisationMethodDecider authorisationMethodDecider;
    private final Xs2aPisCommonPaymentMapper xs2aPisCommonPaymentMapper;
    private final Xs2aToCmsPisCommonPaymentRequestMapper xs2aToCmsPisCommonPaymentRequestMapper;
    private final PisAspspDataService pisAspspDataService;

    /**
     * Initiates single payment
     *
     * @param singlePayment               Single payment information
     * @param paymentInitiationParameters payment initiation parameters
     * @param tppInfo                     information about particular TPP
     * @return Response containing information about created periodic payment or corresponding error
     */
    @Override
    public ResponseObject<SinglePaymentInitiationResponse> createPayment(SinglePayment singlePayment, PaymentInitiationParameters paymentInitiationParameters, TppInfo tppInfo) {
        PsuIdData psuData = paymentInitiationParameters.getPsuData();

        ScaPaymentService scaPaymentService = scaPaymentServiceResolver.getService();
        SinglePaymentInitiationResponse response = scaPaymentService.createSinglePayment(singlePayment, tppInfo, paymentInitiationParameters.getPaymentProduct(), psuData);

        if (response.hasError()) {
            return buildErrorResponse(response.getErrorHolder());
        }

        PisPaymentInfo pisPaymentInfo = xs2aToCmsPisCommonPaymentRequestMapper.mapToPisPaymentInfo(paymentInitiationParameters, tppInfo, response);
        Xs2aPisCommonPayment pisCommonPayment = xs2aPisCommonPaymentMapper.mapToXs2aPisCommonPayment(pisCommonPaymentService.createCommonPayment(pisPaymentInfo), psuData);

        String externalPaymentId = pisCommonPayment.getPaymentId();

        if (StringUtils.isBlank(externalPaymentId)) {
            return ResponseObject.<SinglePaymentInitiationResponse>builder()
                       .fail(PIS_400, of(PAYMENT_FAILED))
                       .build();
        }

        AspspConsentData aspspConsentData = response.getAspspConsentData();
        pisAspspDataService.updateAspspConsentData(new AspspConsentData(aspspConsentData.getAspspConsentData(), externalPaymentId));

        singlePayment.setTransactionStatus(response.getTransactionStatus());
        singlePayment.setPaymentId(response.getPaymentId());
        pisCommonPaymentService.updateSinglePaymentInCommonPayment(singlePayment, paymentInitiationParameters, pisCommonPayment.getPaymentId());

        response.setPaymentId(externalPaymentId);

        boolean implicitMethod = authorisationMethodDecider.isImplicitMethod(paymentInitiationParameters.isTppExplicitAuthorisationPreferred(), response.isMultilevelScaRequired());
        if (implicitMethod) {
            PisScaAuthorisationService pisScaAuthorisationService = pisScaAuthorisationServiceResolver.getService();

            Optional<Xs2aCreatePisAuthorisationResponse> consentAuthorisation = pisScaAuthorisationService.createCommonPaymentAuthorisation(externalPaymentId, PaymentType.SINGLE, psuData);
            if (!consentAuthorisation.isPresent()) {
                return ResponseObject.<SinglePaymentInitiationResponse>builder()
                           .fail(PIS_400, of(PAYMENT_FAILED))
                           .build();
            }
            Xs2aCreatePisAuthorisationResponse authorisationResponse = consentAuthorisation.get();
            response.setAuthorizationId(authorisationResponse.getAuthorisationId());
            response.setScaStatus(authorisationResponse.getScaStatus());
        }

        return ResponseObject.<SinglePaymentInitiationResponse>builder()
                   .body(response)
                   .build();
    }
}
