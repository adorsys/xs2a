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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.api.pis.proto.PisConsentResponse;
import de.adorsys.psd2.xs2a.config.factory.ReadPaymentFactory;
import de.adorsys.psd2.xs2a.config.factory.ReadPaymentStatusFactory;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.event.EventType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPisConsent;
import de.adorsys.psd2.xs2a.domain.pis.*;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.consent.PisConsentDataService;
import de.adorsys.psd2.xs2a.service.consent.PisPsuDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.consent.CmsToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aTransactionalStatusMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentInfoMapper;
import de.adorsys.psd2.xs2a.service.payment.*;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CommonPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.profile.PaymentType.PERIODIC;
import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;
import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.*;

@Slf4j
@Service
@AllArgsConstructor
public class PaymentService {
    private final ReadPaymentFactory readPaymentFactory;
    private final ReadPaymentStatusFactory readPaymentStatusFactory;
    private final SpiPaymentFactory spiPaymentFactory;
    private final Xs2aPisConsentService pisConsentService;
    private final Xs2aUpdatePaymentStatusAfterSpiService updatePaymentStatusAfterSpiService;
    private final PisConsentDataService pisConsentDataService;
    private final PisPsuDataService pisPsuDataService;
    private final TppService tppService;
    private final CreateSinglePaymentService createSinglePaymentService;
    private final CreatePeriodicPaymentService createPeriodicPaymentService;
    private final CreateBulkPaymentService createBulkPaymentService;
    private final Xs2aPisConsentMapper xs2aPisConsentMapper;
    private final CommonPaymentSpi commonPaymentSpi;
    private final SpiToXs2aTransactionalStatusMapper spiToXs2aTransactionalStatus;
    private final AspspProfileServiceWrapper profileService;
    private final CancelPaymentService cancelPaymentService;
    private final SpiErrorMapper spiErrorMapper;
    private final Xs2aEventService xs2aEventService;
    private final CreateCommonPaymentService createCommonPaymentService;
    private final ReadCommonPaymentService readCommonPaymentService;
    private final Xs2aToSpiPaymentInfoMapper xs2aToSpiPaymentInfoMapper;
    private final CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper;
    private final SpiContextDataProvider spiContextDataProvider;

    /**
     * Initiates a payment though "payment service" corresponding service method
     *
     * @param payment                     Payment information
     * @param paymentInitiationParameters Parameters for payment initiation
     * @return Response containing information about created payment or corresponding error
     */
    public ResponseObject createPayment(Object payment, PaymentInitiationParameters paymentInitiationParameters) {
        xs2aEventService.recordTppRequest(EventType.PAYMENT_INITIATION_REQUEST_RECEIVED, payment);

        TppInfo tppInfo = tppService.getTppInfo();
        tppInfo.setTppRedirectUri(paymentInitiationParameters.getTppRedirectUri());
        Xs2aPisConsent pisConsent = xs2aPisConsentMapper.mapToXs2aPisConsent(pisConsentService.createPisConsent(paymentInitiationParameters, tppInfo), paymentInitiationParameters.getPsuData());
        if (StringUtils.isBlank(pisConsent.getConsentId())) {
            return ResponseObject.builder()
                       .fail(new MessageError(CONSENT_UNKNOWN_400))
                       .build();
        }

        if (isRawPaymentProduct(paymentInitiationParameters.getPaymentProduct())) {
            CommonPayment request = new CommonPayment();
            request.setPaymentType(paymentInitiationParameters.getPaymentType());
            request.setPaymentProduct(paymentInitiationParameters.getPaymentProduct());
            request.setPaymentData((byte[]) payment);
            request.setTppInfo(tppInfo);

            return createCommonPaymentService.createPayment(request, paymentInitiationParameters, tppInfo, pisConsent);
        }

        if (paymentInitiationParameters.getPaymentType() == SINGLE) {
            return createSinglePaymentService.createPayment((SinglePayment) payment, paymentInitiationParameters, tppInfo, pisConsent);
        } else if (paymentInitiationParameters.getPaymentType() == PERIODIC) {
            return createPeriodicPaymentService.createPayment((PeriodicPayment) payment, paymentInitiationParameters, tppInfo, pisConsent);
        } else {
            return createBulkPaymentService.createPayment((BulkPayment) payment, paymentInitiationParameters, tppInfo, pisConsent);
        }
    }

    private boolean isRawPaymentProduct(String paymentProduct) {
        // TODO make correct value of method https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/533
        return paymentProduct.contains("pain.");
    }

    /**
     * Retrieves payment from ASPSP by its ASPSP identifier, product and payment type
     *
     * @param paymentType type of payment (payments, bulk-payments, periodic-payments)
     * @param paymentId   ASPSP identifier of the payment
     * @return Response containing information about payment or corresponding error
     */
    public ResponseObject getPaymentById(PaymentType paymentType, String paymentId) {
        xs2aEventService.recordPisTppRequest(paymentId, EventType.GET_PAYMENT_REQUEST_RECEIVED);
        AspspConsentData aspspConsentData = pisConsentDataService.getAspspConsentData(paymentId);
        // aspspConsentData.getConsentId() is used as a temporary solution for getting PisConsent by payment id. Please, don't use this approach in any places
        Optional<PisConsentResponse> pisConsentOptional = pisConsentService.getPisConsentById(aspspConsentData.getConsentId());

        if (!pisConsentOptional.isPresent()) {
            return ResponseObject.builder()
                       .fail(new MessageError(RESOURCE_UNKNOWN_403))
                       .build();
        }

        PisConsentResponse pisConsent = pisConsentOptional.get();

        PsuIdData psuData = pisPsuDataService.getPsuDataByPaymentId(paymentId);
        PaymentInformationResponse response;

        // TODO should be refactored https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/533
        if (pisConsent.getPaymentInfo() != null) {
            CommonPayment commonPayment = cmsToXs2aPaymentMapper.mapToXs2aCommonPayment(pisConsent.getPaymentInfo());
            response = readCommonPaymentService.getPayment(commonPayment, psuData, aspspConsentData);
        } else {
            PisPayment pisPayment = getPisPaymentFromConsent(pisConsent);

            if (pisPayment == null) {
                return ResponseObject.builder()
                           .fail(new MessageError(FORMAT_ERROR, "Payment not found"))
                           .build();
            }

            ReadPaymentService<PaymentInformationResponse> readPaymentService = readPaymentFactory.getService(paymentType.getValue());
            response = readPaymentService.getPayment(pisPayment, pisConsent.getPaymentProduct(), psuData, aspspConsentData); //NOT USED IN 1.2
        }

        if (response.hasError()) {
            return ResponseObject.builder()
                       .fail(new MessageError(response.getErrorHolder().getErrorCode(), response.getErrorHolder().getMessage()))
                       .build();
        }
        return ResponseObject.builder()
                   .body(response.getPayment())
                   .build();
    }

    /**
     * Retrieves payment status from ASPSP
     *
     * @param paymentType The addressed payment category Single, Periodic or Bulk
     * @param paymentId   String representation of payment primary ASPSP identifier
     * @return Information about the status of a payment
     */
    public ResponseObject<TransactionStatus> getPaymentStatusById(PaymentType paymentType, String paymentId) {
        xs2aEventService.recordPisTppRequest(paymentId, EventType.GET_TRANSACTION_STATUS_REQUEST_RECEIVED);

        AspspConsentData aspspConsentData = pisConsentDataService.getAspspConsentData(paymentId);
        // aspspConsentData.getConsentId() is used as a temporary solution for getting PisConsent by payment id. Please, don't use this approach in any places
        Optional<PisConsentResponse> pisConsentOptional = pisConsentService.getPisConsentById(aspspConsentData.getConsentId());

        if (!pisConsentOptional.isPresent()) {
            return ResponseObject.<TransactionStatus>builder()
                       .fail(new MessageError(FORMAT_ERROR, "Consent not found"))
                       .build();
        }

        PsuIdData psuData = pisPsuDataService.getPsuDataByPaymentId(paymentId);
        PisConsentResponse pisConsent = pisConsentOptional.get();
        SpiContextData spiContextData = spiContextDataProvider.provideWithPsuIdData(psuData);
        SpiResponse<SpiTransactionStatus> spiResponse;

        // TODO should be refactored https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/533
        if (pisConsent.getPaymentInfo() != null) {
            CommonPayment commonPayment = cmsToXs2aPaymentMapper.mapToXs2aCommonPayment(pisConsent.getPaymentInfo());
            SpiPaymentInfo request = xs2aToSpiPaymentInfoMapper.mapToSpiPaymentInfo(commonPayment);
            spiResponse = commonPaymentSpi.getPaymentStatusById(spiContextData, request, aspspConsentData);
        } else {
            PisPayment pisPayment = getPisPaymentFromConsent(pisConsent);

            if (pisPayment == null) {
                return ResponseObject.<TransactionStatus>builder()
                           .fail(new MessageError(FORMAT_ERROR, "Payment not found"))
                           .build();
            }

            ReadPaymentStatusService readPaymentStatusService = readPaymentStatusFactory.getService(ReadPaymentStatusFactory.SERVICE_PREFIX + paymentType.getValue());
            spiResponse = readPaymentStatusService.readPaymentStatus(pisPayment, pisConsent.getPaymentProduct(), spiContextData, aspspConsentData);
        }

        pisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse);
            return ResponseObject.<TransactionStatus>builder()
                       .fail(new MessageError(errorHolder.getErrorCode(), errorHolder.getMessage()))
                       .build();
        }

        TransactionStatus transactionStatus = spiToXs2aTransactionalStatus.mapToTransactionStatus(spiResponse.getPayload());

        if (transactionStatus == null) {
            return ResponseObject.<TransactionStatus>builder()
                       .fail(new MessageError(RESOURCE_UNKNOWN_403))
                       .build();
        }

        if (!updatePaymentStatusAfterSpiService.updatePaymentStatus(paymentId, transactionStatus)) {
            return ResponseObject.<TransactionStatus>builder()
                       .fail(new MessageError(FORMAT_ERROR, "Payment is finalised already, so its status cannot be changed"))
                       .build();
        }

        return ResponseObject.<TransactionStatus>builder().body(transactionStatus).build();
    }

    /**
     * Cancels payment by its ASPSP identifier and payment type
     *
     * @param paymentType type of payment (payments, bulk-payments, periodic-payments)
     * @param encryptedPaymentId   ASPSP identifier of the payment
     * @return Response containing information about cancelled payment or corresponding error
     */
    public ResponseObject<CancelPaymentResponse> cancelPayment(PaymentType paymentType, String encryptedPaymentId) {
        xs2aEventService.recordPisTppRequest(encryptedPaymentId, EventType.PAYMENT_CANCELLATION_REQUEST_RECEIVED);

        AspspConsentData aspspConsentData = pisConsentDataService.getAspspConsentData(encryptedPaymentId);
        // aspspConsentData.getConsentId() is used as a temporary solution for getting PisConsent by payment id. Please, don't use this approach in any places
        Optional<PisConsentResponse> pisConsentOptional = pisConsentService.getPisConsentById(aspspConsentData.getConsentId());

        if (!pisConsentOptional.isPresent()) {
            return ResponseObject.<CancelPaymentResponse>builder()
                       .fail(new MessageError(FORMAT_ERROR, "Consent not found"))
                       .build();
        }

        PisConsentResponse pisConsent = pisConsentOptional.get();
        SpiPayment spiPayment;

        if (pisConsent.getPaymentInfo() != null) {
            CommonPayment commonPayment = cmsToXs2aPaymentMapper.mapToXs2aCommonPayment(pisConsent.getPaymentInfo());
            spiPayment = xs2aToSpiPaymentInfoMapper.mapToSpiPaymentInfo(commonPayment);
        } else {
            PisPayment pisPayment = getPisPaymentFromConsent(pisConsent);
            if (pisPayment == null) {
                return ResponseObject.<CancelPaymentResponse>builder()
                           .fail(new MessageError(FORMAT_ERROR, "Payment not found"))
                           .build();
            }

            Optional<? extends SpiPayment> spiPaymentOptional = spiPaymentFactory.createSpiPaymentByPaymentType(pisPayment, pisConsent.getPaymentProduct(), paymentType);

            if (!spiPaymentOptional.isPresent()) {
                log.error("Unknown payment type: {}", paymentType);
                return ResponseObject.<CancelPaymentResponse>builder()
                           .fail(new MessageError(FORMAT_ERROR))
                           .build();
            }

            spiPayment = spiPaymentOptional.get();
        }

        Optional<PisConsentResponse> consent = pisConsentService.getPisConsentById(encryptedPaymentId);

        if (consent.isPresent() && isFinalisedPayment(consent.get())) {
            return ResponseObject.<CancelPaymentResponse>builder()
                       .fail(new MessageError(FORMAT_ERROR, "Payment is finalised already and cannot be cancelled"))
                       .build();
        }

        PsuIdData psuData = pisPsuDataService.getPsuDataByPaymentId(encryptedPaymentId);

        if (profileService.isPaymentCancellationAuthorizationMandated()) {
            return cancelPaymentService.initiatePaymentCancellation(psuData, spiPayment, encryptedPaymentId);
        } else {
            ResponseObject<CancelPaymentResponse> cancellationResponse = cancelPaymentService.cancelPaymentWithoutAuthorisation(psuData, spiPayment, encryptedPaymentId);
            pisConsentService.revokeConsentById(encryptedPaymentId);
            return cancellationResponse;
        }
    }

    private boolean isFinalisedPayment(PisConsentResponse consent) {
        List<PisPayment> finalisedPayments = consent.getPayments().stream()
                                                 .filter(p -> p.getTransactionStatus().isFinalisedStatus())
                                                 .collect(Collectors.toList());

        return CollectionUtils.isNotEmpty(finalisedPayments);
    }

    private PisPayment getPisPaymentFromConsent(PisConsentResponse pisConsentResponse) {
        return Optional.of(pisConsentResponse)
                   .map(PisConsentResponse::getPayments)
                   .map(payments -> payments.get(0))
                   .orElse(null);
    }
}
