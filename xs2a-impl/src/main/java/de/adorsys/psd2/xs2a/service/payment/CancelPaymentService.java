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

package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.CancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.psd2.xs2a.service.PaymentCancellationAuthorisationService;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.PaymentCancellationAuthorisationNeededDecider;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aCancelPaymentMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentCancellationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentCancellationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.PIS_CANC_405;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CANCELLATION_INVALID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CancelPaymentService {
    private final PaymentCancellationSpi paymentCancellationSpi;
    private final Xs2aUpdatePaymentAfterSpiService updatePaymentStatusAfterSpiService;
    private final PaymentCancellationAuthorisationNeededDecider cancellationScaNeededDecider;
    private final SpiContextDataProvider spiContextDataProvider;
    private final SpiErrorMapper spiErrorMapper;
    private final SpiToXs2aCancelPaymentMapper spiToXs2aCancelPaymentMapper;
    private final AuthorisationMethodDecider authorisationMethodDecider;
    private final PaymentCancellationAuthorisationService paymentCancellationAuthorisationService;
    private final RequestProviderService requestProviderService;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;

    /**
     * Cancels payment with or without performing strong customer authentication
     *
     * @param payment                           Payment to be cancelled
     * @param encryptedPaymentId                encrypted identifier of the payment
     * @param tppExplicitAuthorisationPreferred value of TPP's choice of authorisation method
     * @param tppRedirectUri                    TPP's redirect URIs
     * @return Response containing information about cancelled payment or corresponding error
     */
    public ResponseObject<CancelPaymentResponse> initiatePaymentCancellation(SpiPayment payment, String encryptedPaymentId,
                                                                             Boolean tppExplicitAuthorisationPreferred,
                                                                             TppRedirectUri tppRedirectUri) {
        SpiContextData spiContextData = spiContextDataProvider.provide();

        SpiAspspConsentDataProvider aspspConsentDataProvider =
            aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(encryptedPaymentId);

        SpiResponse<SpiPaymentCancellationResponse> spiResponse = paymentCancellationSpi.initiatePaymentCancellation(spiContextData, payment, aspspConsentDataProvider);

        UUID internalRequestId = requestProviderService.getInternalRequestId();
        UUID xRequestId = requestProviderService.getRequestId();

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);
            log.info("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}]. Initiate Payment Cancellation has failed. Error msg: {}.",
                     internalRequestId, xRequestId, encryptedPaymentId, errorHolder);
            return ResponseObject.<CancelPaymentResponse>builder()
                       .fail(errorHolder)
                       .build();
        }

        CancelPaymentResponse cancelPaymentResponse = spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(spiResponse.getPayload(), payment, encryptedPaymentId);
        TransactionStatus resultStatus = cancelPaymentResponse.getTransactionStatus();

        if (resultStatus != null) {
            updatePaymentStatusAfterSpiService.updatePaymentStatus(encryptedPaymentId, resultStatus);
        } else {
            resultStatus = payment.getPaymentStatus();
            cancelPaymentResponse.setTransactionStatus(resultStatus);
        }

        updatePaymentStatusAfterSpiService.updatePaymentCancellationTppRedirectUri(encryptedPaymentId, tppRedirectUri);
        updatePaymentStatusAfterSpiService.updatePaymentCancellationInternalRequestId(encryptedPaymentId, internalRequestId.toString());
        cancelPaymentResponse.setInternalRequestId(internalRequestId.toString());

        if (resultStatus == TransactionStatus.CANC) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}]. Initiate Payment Cancellation has failed. Payment status - CANCELED",
                     internalRequestId, xRequestId, encryptedPaymentId);
            return ResponseObject.<CancelPaymentResponse>builder()
                       .body(cancelPaymentResponse)
                       .build();
        }

        if (resultStatus.isFinalisedStatus()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}]. Initiate Payment Cancellation has failed. Payment has finalised status",
                     internalRequestId, xRequestId, encryptedPaymentId);
            return ResponseObject.<CancelPaymentResponse>builder()
                       .fail(PIS_CANC_405, of(CANCELLATION_INVALID))
                       .build();
        }

        if (resultStatus == TransactionStatus.RCVD
                || cancellationScaNeededDecider.isNoScaRequired(cancelPaymentResponse.isStartAuthorisationRequired())) {
            payment.setPaymentStatus(resultStatus);
            return proceedNoScaCancellation(payment, spiContextData, aspspConsentDataProvider, encryptedPaymentId);
        }

        // in payment cancellation case 'multilevelScaRequired' is always false
        boolean implicitMethod = authorisationMethodDecider.isImplicitMethod(tppExplicitAuthorisationPreferred, false);
        if (implicitMethod) {
            Xs2aCreatePisAuthorisationRequest request = new Xs2aCreatePisAuthorisationRequest(encryptedPaymentId, new PsuIdData(null, null, null, null, null), payment.getPaymentProduct(), payment.getPaymentType(), null);
            ResponseObject<CancellationAuthorisationResponse> authorisationResponse = paymentCancellationAuthorisationService.createPisCancellationAuthorisation(request);

            if (authorisationResponse.hasError()) {
                log.info("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}]. Initiate Payment Cancellation has failed. Can't create implicit authorisation",
                         internalRequestId, xRequestId, encryptedPaymentId);
                return ResponseObject.<CancelPaymentResponse>builder()
                           .fail(PIS_CANC_405, of(CANCELLATION_INVALID))
                           .build();
            }

            CancellationAuthorisationResponse authorisationResponseBody = authorisationResponse.getBody();
            cancelPaymentResponse.setAuthorizationId(authorisationResponseBody.getCancellationId());
            cancelPaymentResponse.setScaStatus(authorisationResponseBody.getScaStatus());
        }

        return ResponseObject.<CancelPaymentResponse>builder()
                   .body(cancelPaymentResponse)
                   .build();

    }

    private ResponseObject<CancelPaymentResponse> proceedNoScaCancellation(SpiPayment payment, SpiContextData spiContextData, SpiAspspConsentDataProvider aspspConsentDataProvider, String encryptedPaymentId) {
        SpiResponse<SpiResponse.VoidResponse> spiResponse = paymentCancellationSpi.cancelPaymentWithoutSca(spiContextData, payment, aspspConsentDataProvider);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);
            log.info("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}]. Proceed no SCA Cancellation failed. Can't Cancel Payment without SCA at SPI level. Error msg: {}.",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), encryptedPaymentId, errorHolder);
            return ResponseObject.<CancelPaymentResponse>builder()
                       .fail(errorHolder)
                       .build();
        }

        updatePaymentStatusAfterSpiService.updatePaymentStatus(encryptedPaymentId, TransactionStatus.CANC);
        CancelPaymentResponse cancelPaymentResponse = new CancelPaymentResponse();
        cancelPaymentResponse.setTransactionStatus(TransactionStatus.CANC);
        cancelPaymentResponse.setInternalRequestId(requestProviderService.getInternalRequestIdString());

        return ResponseObject.<CancelPaymentResponse>builder()
                   .body(cancelPaymentResponse)
                   .build();
    }
}
