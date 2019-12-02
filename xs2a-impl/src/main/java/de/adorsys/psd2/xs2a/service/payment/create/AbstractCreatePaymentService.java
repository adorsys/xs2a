/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.payment.create;

import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPisCommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aToCmsPisCommonPaymentRequestMapper;
import de.adorsys.psd2.xs2a.service.payment.create.spi.PaymentInitiationService;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.OffsetDateTime;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.PAYMENT_FAILED;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_400;

@RequiredArgsConstructor
public abstract class AbstractCreatePaymentService<P extends CommonPayment, S extends PaymentInitiationService<P>> implements CreatePaymentService {
    protected final Xs2aPisCommonPaymentService pisCommonPaymentService;
    private final PisScaAuthorisationServiceResolver pisScaAuthorisationServiceResolver;
    private final AuthorisationMethodDecider authorisationMethodDecider;
    private final Xs2aPisCommonPaymentMapper xs2aPisCommonPaymentMapper;
    private final Xs2aToCmsPisCommonPaymentRequestMapper xs2aToCmsPisCommonPaymentRequestMapper;
    private final S paymentInitiationService;
    private final RequestProviderService requestProviderService;

    /**
     * Initiates payment
     *
     * @param payment                     payment information
     * @param paymentInitiationParameters payment initiation parameters
     * @param tppInfo                     information about particular TPP
     * @return Response containing information about created common payment or corresponding error
     */
    @Override
    public ResponseObject<PaymentInitiationResponse> createPayment(Object payment, PaymentInitiationParameters paymentInitiationParameters, TppInfo tppInfo) {
        PsuIdData psuData = paymentInitiationParameters.getPsuData();

        P paymentRequest = getPaymentRequest(payment, paymentInitiationParameters);
        OffsetDateTime creationTimestamp = OffsetDateTime.now();
        paymentRequest.setCreationTimestamp(creationTimestamp);
        PaymentInitiationResponse response = paymentInitiationService.initiatePayment(paymentRequest, paymentInitiationParameters.getPaymentProduct(), psuData);

        if (response.hasError()) {
            return buildErrorResponse(response.getErrorHolder());
        }

        String internalRequestId = requestProviderService.getInternalRequestIdString();
        PisPaymentInfo pisPaymentInfo = xs2aToCmsPisCommonPaymentRequestMapper.mapToPisPaymentInfo(paymentInitiationParameters, tppInfo, response, paymentRequest.getPaymentData(), internalRequestId, creationTimestamp);
        response.setInternalRequestId(internalRequestId);
        CreatePisCommonPaymentResponse cmsResponse = pisCommonPaymentService.createCommonPayment(pisPaymentInfo);
        response.setTppNotificationContentPreferred(cmsResponse.getTppNotificationContentPreferred());

        Xs2aPisCommonPayment pisCommonPayment = xs2aPisCommonPaymentMapper.mapToXs2aPisCommonPayment(cmsResponse, psuData);

        String externalPaymentId = pisCommonPayment.getPaymentId();

        if (StringUtils.isBlank(externalPaymentId)) {
            return ResponseObject.<PaymentInitiationResponse>builder()
                       .fail(PIS_400, of(PAYMENT_FAILED))
                       .build();
        }

        InitialSpiAspspConsentDataProvider aspspConsentDataProvider = response.getAspspConsentDataProvider();
        aspspConsentDataProvider.saveWith(externalPaymentId);

        updateCommonPayment(paymentRequest, paymentInitiationParameters, response, pisCommonPayment.getPaymentId());

        response.setPaymentId(externalPaymentId);

        boolean implicitMethod = authorisationMethodDecider.isImplicitMethod(paymentInitiationParameters.isTppExplicitAuthorisationPreferred(), response.isMultilevelScaRequired());
        if (implicitMethod) {
            PisScaAuthorisationService pisScaAuthorisationService = pisScaAuthorisationServiceResolver.getService();
            Optional<Xs2aCreatePisAuthorisationResponse> consentAuthorisation = pisScaAuthorisationService.createCommonPaymentAuthorisation(externalPaymentId, paymentRequest.getPaymentType(), paymentInitiationParameters.getPsuData());
            if (!consentAuthorisation.isPresent()) {
                return ResponseObject.<PaymentInitiationResponse>builder()
                           .fail(PIS_400, of(PAYMENT_FAILED))
                           .build();
            }

            Xs2aCreatePisAuthorisationResponse authorisationResponse = consentAuthorisation.get();
            response.setAuthorizationId(authorisationResponse.getAuthorisationId());
            response.setScaStatus(authorisationResponse.getScaStatus());
        }

        return ResponseObject.<PaymentInitiationResponse>builder()
                   .body(response)
                   .build();
    }

    protected abstract P getPaymentRequest(Object payment, PaymentInitiationParameters paymentInitiationParameters);

    protected abstract P updateCommonPayment(P paymentRequest, PaymentInitiationParameters paymentInitiationParameters,
                                             PaymentInitiationResponse response, String paymentId);
}
