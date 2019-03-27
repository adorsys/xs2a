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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
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
import de.adorsys.psd2.xs2a.domain.pis.*;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.consent.PisPsuDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.consent.CmsToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentInfoMapper;
import de.adorsys.psd2.xs2a.service.payment.*;
import de.adorsys.psd2.xs2a.service.profile.StandardPaymentProductsResolver;
import de.adorsys.psd2.xs2a.service.validator.PaymentValidationService;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.pis.payment.*;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.*;

@Slf4j
@Service
@AllArgsConstructor
public class PaymentService {
    private final ReadPaymentFactory readPaymentFactory;
    private final ReadPaymentStatusFactory readPaymentStatusFactory;
    private final SpiPaymentFactory spiPaymentFactory;
    private final Xs2aPisCommonPaymentService pisCommonPaymentService;
    private final Xs2aUpdatePaymentStatusAfterSpiService updatePaymentStatusAfterSpiService;
    private final PisAspspDataService pisAspspDataService;
    private final PisPsuDataService pisPsuDataService;
    private final TppService tppService;
    private final CreateSinglePaymentService createSinglePaymentService;
    private final CreatePeriodicPaymentService createPeriodicPaymentService;
    private final CreateBulkPaymentService createBulkPaymentService;
    private final CancelPaymentService cancelPaymentService;
    private final Xs2aEventService xs2aEventService;
    private final CreateCommonPaymentService createCommonPaymentService;
    private final ReadCommonPaymentService readCommonPaymentService;
    private final Xs2aToSpiPaymentInfoMapper xs2aToSpiPaymentInfoMapper;
    private final CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper;
    private final SpiContextDataProvider spiContextDataProvider;
    private final ReadCommonPaymentStatusService readCommonPaymentStatusService;
    private final RequestProviderService requestProviderService;
    private final PaymentValidationService paymentValidationService;
    private final StandardPaymentProductsResolver standardPaymentProductsResolver;
    private final CreatePaymentValidator createPaymentValidator;
    private final GetPaymentByIdValidator getPaymentByIdValidator;
    private final GetPaymentStatusByIdValidator getPaymentStatusByIdValidator;
    private final CancelPaymentValidator cancelPaymentValidator;

    /**
     * Initiates a payment though "payment service" corresponding service method
     *
     * @param payment                     Payment information
     * @param paymentInitiationParameters Parameters for payment initiation
     * @return Response containing information about created payment or corresponding error
     */
    public ResponseObject createPayment(Object payment, PaymentInitiationParameters paymentInitiationParameters) {
        xs2aEventService.recordTppRequest(EventType.PAYMENT_INITIATION_REQUEST_RECEIVED, payment);

        ValidationResult validationResult = createPaymentValidator.validate(paymentInitiationParameters);
        if (validationResult.isNotValid()) {
            return ResponseObject.builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        TppInfo tppInfo = tppService.getTppInfo();
        tppInfo.setTppRedirectUri(paymentInitiationParameters.getTppRedirectUri());

        if (standardPaymentProductsResolver.isRawPaymentProduct(paymentInitiationParameters.getPaymentProduct())) {
            CommonPayment request = new CommonPayment();
            request.setPaymentType(paymentInitiationParameters.getPaymentType());
            request.setPaymentProduct(paymentInitiationParameters.getPaymentProduct());
            request.setPaymentData((byte[]) payment);
            request.setTppInfo(tppInfo);
            request.setPsuDataList(Collections.singletonList(paymentInitiationParameters.getPsuData()));

            return createCommonPaymentService.createPayment(request, paymentInitiationParameters, tppInfo);
        }

        if (paymentInitiationParameters.getPaymentType() == PaymentType.SINGLE) {
            return processSinglePayment((SinglePayment) payment, paymentInitiationParameters, tppInfo);
        } else if (paymentInitiationParameters.getPaymentType() == PaymentType.PERIODIC) {
            return processPeriodicPayment((PeriodicPayment) payment, paymentInitiationParameters, tppInfo);
        } else {
            return processBulkPayment((BulkPayment) payment, paymentInitiationParameters, tppInfo);
        }
    }

    /**
     * Retrieves payment from ASPSP by its ASPSP identifier, product and payment type
     *
     * @param paymentType    type of payment (payments, bulk-payments, periodic-payments)
     * @param paymentProduct payment product used for payment creation (e.g. sepa-credit-transfers, instant-sepa-credit-transfers...)
     * @param paymentId      ASPSP identifier of the payment
     * @return Response containing information about payment or corresponding error
     */
    public ResponseObject getPaymentById(PaymentType paymentType, String paymentProduct, String paymentId) {
        xs2aEventService.recordPisTppRequest(paymentId, EventType.GET_PAYMENT_REQUEST_RECEIVED);
        Optional<PisCommonPaymentResponse> pisCommonPaymentOptional = pisCommonPaymentService.getPisCommonPaymentById(paymentId);

        if (!pisCommonPaymentOptional.isPresent()) {
            return ResponseObject.builder()
                       .fail(PIS_404, of(RESOURCE_UNKNOWN_404, "Payment not found"))
                       .build();
        }

        PisCommonPaymentResponse commonPaymentResponse = pisCommonPaymentOptional.get();
        ValidationResult validationResult = getPaymentByIdValidator.validate(new GetPaymentByIdPO(commonPaymentResponse, paymentType, paymentProduct));
        if (validationResult.isNotValid()) {
            return ResponseObject.builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        CommonPayment commonPayment = cmsToXs2aPaymentMapper.mapToXs2aCommonPayment(commonPaymentResponse);
        AspspConsentData aspspConsentData = pisAspspDataService.getAspspConsentData(paymentId);
        PaymentInformationResponse response;

        // TODO should be refactored https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/533
        if (commonPayment.getPaymentData() != null) {
            response = readCommonPaymentService.getPayment(commonPayment, readPsuIdDataFromList(commonPayment.getPsuDataList()), aspspConsentData);
        } else {
            List<PisPayment> pisPayments = getPisPaymentFromCommonPaymentResponse(commonPaymentResponse);
            if (CollectionUtils.isEmpty(pisPayments)) {
                return ResponseObject.builder()
                           .fail(PIS_400, of(FORMAT_ERROR, "Payment not found"))
                           .build();
            }

            ReadPaymentService<PaymentInformationResponse> readPaymentService = readPaymentFactory.getService(paymentType.getValue());
            response = readPaymentService.getPayment(pisPayments, commonPaymentResponse.getPaymentProduct(), readPsuIdDataFromList(commonPayment.getPsuDataList()), aspspConsentData); //NOT USED IN 1.2
        }

        if (response.hasError()) {
            return ResponseObject.builder()
                       .fail(response.getErrorHolder())
                       .build();
        }
        return ResponseObject.builder()
                   .body(response.getPayment())
                   .build();
    }

    /**
     * Retrieves payment status from ASPSP
     *
     * @param paymentType    The addressed payment category Single, Periodic or Bulk
     * @param paymentProduct payment product used for payment creation (e.g. sepa-credit-transfers, instant-sepa-credit-transfers...)
     * @param paymentId      String representation of payment primary ASPSP identifier
     * @return Information about the status of a payment
     */
    public ResponseObject<TransactionStatus> getPaymentStatusById(PaymentType paymentType, String paymentProduct, String paymentId) {//NOPMD //TODO refactor method  and remove https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/683
        xs2aEventService.recordPisTppRequest(paymentId, EventType.GET_TRANSACTION_STATUS_REQUEST_RECEIVED);
        Optional<PisCommonPaymentResponse> pisCommonPaymentOptional = pisCommonPaymentService.getPisCommonPaymentById(paymentId);

        if (!pisCommonPaymentOptional.isPresent()) {
            return ResponseObject.<TransactionStatus>builder()
                       .fail(PIS_404, of(RESOURCE_UNKNOWN_404, "Payment not found"))
                       .build();
        }

        PisCommonPaymentResponse pisCommonPaymentResponse = pisCommonPaymentOptional.get();
        ValidationResult validationResult = getPaymentStatusByIdValidator.validate(new GetPaymentStatusByIdPO(pisCommonPaymentResponse, paymentType, paymentProduct));
        if (validationResult.isNotValid()) {
            return ResponseObject.<TransactionStatus>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        // TODO temporary solution: payment initiation workflow should be clarified https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/582
        if (pisCommonPaymentResponse.getTransactionStatus() == TransactionStatus.RJCT) {
            return ResponseObject.<TransactionStatus>builder().body(TransactionStatus.RJCT).build();
        }

        AspspConsentData aspspConsentData = pisAspspDataService.getAspspConsentData(paymentId);
        List<PsuIdData> psuData = pisPsuDataService.getPsuDataByPaymentId(paymentId);
        SpiContextData spiContextData = spiContextDataProvider.provideWithPsuIdData(readPsuIdDataFromList(psuData));

        ReadPaymentStatusResponse readPaymentStatusResponse;

        // TODO should be refactored https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/533
        if (pisCommonPaymentResponse.getPaymentData() != null) {
            readPaymentStatusResponse = readCommonPaymentStatusService.readPaymentStatus(pisCommonPaymentResponse, spiContextData, aspspConsentData);
        } else {
            List<PisPayment> pisPayments = getPisPaymentFromCommonPaymentResponse(pisCommonPaymentResponse);
            if (CollectionUtils.isEmpty(pisPayments)) {
                return ResponseObject.<TransactionStatus>builder()
                           .fail(PIS_400, of(FORMAT_ERROR, "Payment not found"))
                           .build();
            }

            ReadPaymentStatusService readPaymentStatusService = readPaymentStatusFactory.getService(ReadPaymentStatusFactory.SERVICE_PREFIX + paymentType.getValue());
            readPaymentStatusResponse = readPaymentStatusService.readPaymentStatus(pisPayments, pisCommonPaymentResponse.getPaymentProduct(), spiContextData, aspspConsentData);
        }

        if (readPaymentStatusResponse.hasError()) {
            ErrorHolder errorHolder = readPaymentStatusResponse.getErrorHolder();
            return ResponseObject.<TransactionStatus>builder()
                       .fail(errorHolder)
                       .build();
        }

        TransactionStatus transactionStatus = readPaymentStatusResponse.getStatus();

        if (transactionStatus == null) {
            return ResponseObject.<TransactionStatus>builder()
                       .fail(PIS_403, of(RESOURCE_UNKNOWN_403))
                       .build();
        }

        if (!updatePaymentStatusAfterSpiService.updatePaymentStatus(paymentId, transactionStatus)) {
            log.info("X-Request-ID: [{}], Payment ID: [{}], Transaction status: [{}]. Update of a payment status in the CMS has failed.",
                     requestProviderService.getRequestId(), paymentId, transactionStatus);
        }

        return ResponseObject.<TransactionStatus>builder().body(transactionStatus).build();
    }

    /**
     * Cancels payment by its ASPSP identifier and payment type
     *
     * @param paymentType        type of payment (payments, bulk-payments, periodic-payments)
     * @param paymentProduct     payment product used for payment creation (e.g. sepa-credit-transfers, instant-sepa-credit-transfers...)
     * @param encryptedPaymentId ASPSP identifier of the payment
     * @return Response containing information about cancelled payment or corresponding error
     */
    public ResponseObject<CancelPaymentResponse> cancelPayment(PaymentType paymentType, String paymentProduct, String encryptedPaymentId) {
        xs2aEventService.recordPisTppRequest(encryptedPaymentId, EventType.PAYMENT_CANCELLATION_REQUEST_RECEIVED);
        Optional<PisCommonPaymentResponse> pisCommonPaymentOptional = pisCommonPaymentService.getPisCommonPaymentById(encryptedPaymentId);

        if (!pisCommonPaymentOptional.isPresent()) {
            return ResponseObject.<CancelPaymentResponse>builder()
                       .fail(PIS_404, of(RESOURCE_UNKNOWN_404, "Payment not found"))
                       .build();
        }

        PisCommonPaymentResponse pisCommonPaymentResponse = pisCommonPaymentOptional.get();
        ValidationResult validationResult = cancelPaymentValidator.validate(new CancelPaymentPO(pisCommonPaymentResponse, paymentType, paymentProduct));
        if (validationResult.isNotValid()) {
            return ResponseObject.<CancelPaymentResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        if (isFinalisedPayment(pisCommonPaymentResponse)) {
            return ResponseObject.<CancelPaymentResponse>builder()
                       .fail(PIS_400, of(RESOURCE_BLOCKED))
                       .build();
        }

        SpiPayment spiPayment;

        if (standardPaymentProductsResolver.isRawPaymentProduct(paymentProduct)) {
            CommonPayment commonPayment = cmsToXs2aPaymentMapper.mapToXs2aCommonPayment(pisCommonPaymentResponse);
            spiPayment = xs2aToSpiPaymentInfoMapper.mapToSpiPaymentInfo(commonPayment);
        } else {
            List<PisPayment> pisPayments = getPisPaymentFromCommonPaymentResponse(pisCommonPaymentResponse);
            if (CollectionUtils.isEmpty(pisPayments)) {
                return ResponseObject.<CancelPaymentResponse>builder()
                           .fail(PIS_404, of(RESOURCE_UNKNOWN_404, "Payment not found"))
                           .build();
            }

            Optional<? extends SpiPayment> spiPaymentOptional = spiPaymentFactory.createSpiPaymentByPaymentType(pisPayments, pisCommonPaymentResponse.getPaymentProduct(), paymentType);
            spiPayment = spiPaymentOptional.get();
        }

        List<PsuIdData> psuData = pisCommonPaymentResponse.getPsuData();
        return cancelPaymentService.initiatePaymentCancellation(readPsuIdDataFromList(psuData), spiPayment, encryptedPaymentId);
    }

    private boolean isFinalisedPayment(PisCommonPaymentResponse response) {
        return response.getTransactionStatus().isFinalisedStatus();
    }

    private List<PisPayment> getPisPaymentFromCommonPaymentResponse(PisCommonPaymentResponse pisCommonPaymentResponse) {
        List<PisPayment> pisPayments = Optional.of(pisCommonPaymentResponse)
                                           .map(PisCommonPaymentResponse::getPayments)
                                           .orElseGet(Collections::emptyList);

        pisPayments.forEach(pmt -> {
            pmt.setPaymentId(pisCommonPaymentResponse.getExternalId());
            pmt.setTransactionStatus(pisCommonPaymentResponse.getTransactionStatus());
            pmt.setPsuDataList(pisCommonPaymentResponse.getPsuData());
            pmt.setStatusChangeTimestamp(pisCommonPaymentResponse.getStatusChangeTimestamp());
        });

        return pisPayments;
    }

    private PsuIdData readPsuIdDataFromList(List<PsuIdData> psuIdDataList) { //TODO rework psudata list
        if (CollectionUtils.isNotEmpty(psuIdDataList)) {
            return psuIdDataList.get(0);
        }
        return null;
    }

    private ResponseObject processSinglePayment(SinglePayment singePayment, PaymentInitiationParameters paymentInitiationParameters, TppInfo tppInfo) {

        ResponseObject singlePaymentValidationResult = paymentValidationService.validateSinglePayment(singePayment);

        return singlePaymentValidationResult.hasError()
                   ? singlePaymentValidationResult
                   : createSinglePaymentService.createPayment(singePayment, paymentInitiationParameters, tppInfo);
    }

    private ResponseObject processPeriodicPayment(PeriodicPayment periodicPayment, PaymentInitiationParameters paymentInitiationParameters, TppInfo tppInfo) {

        ResponseObject periodicPaymentValidationResponse = paymentValidationService.validatePeriodicPayment(periodicPayment);

        return periodicPaymentValidationResponse.hasError()
                   ? periodicPaymentValidationResponse
                   : createPeriodicPaymentService.createPayment(periodicPayment, paymentInitiationParameters, tppInfo);
    }

    private ResponseObject processBulkPayment(BulkPayment bulkPayment, PaymentInitiationParameters paymentInitiationParameters, TppInfo tppInfo) {

        ResponseObject bulkPaymentValidationResponse = paymentValidationService.validateBulkPayment(bulkPayment);

        return bulkPaymentValidationResponse.hasError()
                   ? bulkPaymentValidationResponse
                   : createBulkPaymentService.createPayment(bulkPayment, paymentInitiationParameters, tppInfo);
    }
}
