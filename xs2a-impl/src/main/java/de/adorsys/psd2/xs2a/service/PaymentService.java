/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentCancellationRequest;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.pis.*;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.payment.PaymentServiceResolver;
import de.adorsys.psd2.xs2a.service.payment.Xs2aUpdatePaymentAfterSpiService;
import de.adorsys.psd2.xs2a.service.payment.cancel.CancelPaymentService;
import de.adorsys.psd2.xs2a.service.payment.create.CreatePaymentService;
import de.adorsys.psd2.xs2a.service.payment.read.ReadPaymentService;
import de.adorsys.psd2.xs2a.service.payment.status.ReadPaymentStatusService;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.pis.payment.*;
import de.adorsys.psd2.xs2a.service.validator.pis.payment.dto.CreatePaymentRequestObject;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.PIS_400;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.PIS_403;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;

@Slf4j
@Service
@AllArgsConstructor
public class PaymentService {

    private final Xs2aPisCommonPaymentService pisCommonPaymentService;
    private final Xs2aUpdatePaymentAfterSpiService updatePaymentAfterSpiService;
    private final TppService tppService;
    private final Xs2aEventService xs2aEventService;
    private final SpiContextDataProvider spiContextDataProvider;
    private final RequestProviderService requestProviderService;
    private final CreatePaymentValidator createPaymentValidator;
    private final GetPaymentByIdValidator getPaymentByIdValidator;
    private final GetPaymentStatusByIdValidator getPaymentStatusByIdValidator;
    private final CancelPaymentValidator cancelPaymentValidator;
    private final PaymentServiceResolver paymentServiceResolver;
    private final LoggingContextService loggingContextService;
    private final ScaApproachResolver scaApproachResolver;

    /**
     * Initiates a payment though "payment service" corresponding service method
     *
     * @param payment                     Payment information
     * @param paymentInitiationParameters Parameters for payment initiation
     * @return Response containing information about created payment or corresponding error
     */
    public ResponseObject<PaymentInitiationResponse> createPayment(byte[] payment, PaymentInitiationParameters paymentInitiationParameters) {
        xs2aEventService.recordTppRequest(EventType.PAYMENT_INITIATION_REQUEST_RECEIVED, payment);

        CreatePaymentRequestObject createPaymentRequestObject = new CreatePaymentRequestObject(payment, paymentInitiationParameters);
        ValidationResult validationResult = createPaymentValidator.validate(createPaymentRequestObject);
        if (validationResult.isNotValid()) {
            log.info("PaymentType [{}], PaymentProduct [{}]. Create payment - validation failed: [{}]",
                     paymentInitiationParameters.getPaymentType(), paymentInitiationParameters.getPaymentProduct(), validationResult.getMessageError());
            return ResponseObject.<PaymentInitiationResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        if (isNotSupportedScaApproach(scaApproachResolver.resolveScaApproach())) {
            throw new UnsupportedOperationException("Unsupported operation");
        }

        TppInfo tppInfo = tppService.getTppInfo();

        CreatePaymentService createPaymentService = paymentServiceResolver.getCreatePaymentService(paymentInitiationParameters);
        ResponseObject<PaymentInitiationResponse> responseObject = createPaymentService.createPayment(payment, paymentInitiationParameters, tppInfo);

        if (responseObject.hasError()) {
            log.info("Create payment failed: [{}]", responseObject.getError());
            return ResponseObject.<PaymentInitiationResponse>builder()
                       .fail(responseObject.getError())
                       .build();
        }

        PaymentInitiationResponse paymentInitiationResponse = responseObject.getBody();
        paymentInitiationResponse.setTppMessageInformation(createPaymentValidator.buildWarningMessages(createPaymentRequestObject));

        loggingContextService.storeTransactionAndScaStatus(paymentInitiationResponse.getTransactionStatus(), paymentInitiationResponse.getScaStatus());

        return ResponseObject.<PaymentInitiationResponse>builder()
                   .body(paymentInitiationResponse)
                   .build();
    }

    /**
     * Retrieves payment from ASPSP by its ASPSP identifier, product and payment type
     *
     * @param paymentType        type of payment (payments, bulk-payments, periodic-payments)
     * @param paymentProduct     payment product used for payment creation (e.g. sepa-credit-transfers, instant-sepa-credit-transfers...)
     * @param encryptedPaymentId ASPSP identifier of the payment
     * @return Response containing information about payment or corresponding error
     */
    public ResponseObject<CommonPayment> getPaymentById(PaymentType paymentType, String paymentProduct, String encryptedPaymentId) {
        xs2aEventService.recordPisTppRequest(encryptedPaymentId, EventType.GET_PAYMENT_REQUEST_RECEIVED);
        Optional<PisCommonPaymentResponse> pisCommonPaymentOptional = pisCommonPaymentService.getPisCommonPaymentById(encryptedPaymentId);

        if (!pisCommonPaymentOptional.isPresent()) {
            log.info("Payment-ID [{}]. Get payment failed. PIS CommonPayment not found by ID", encryptedPaymentId);
            return ResponseObject.<CommonPayment>builder()
                       .fail(PIS_403, of(RESOURCE_UNKNOWN_403))
                       .build();
        }

        PisCommonPaymentResponse commonPaymentResponse = pisCommonPaymentOptional.get();
        ValidationResult validationResult = getPaymentByIdValidator.validate(new GetPaymentByIdPO(commonPaymentResponse, paymentType, paymentProduct));
        if (validationResult.isNotValid()) {
            log.info("Payment-ID [{}]. Get payment - validation failed: {}", encryptedPaymentId, validationResult.getMessageError());
            return ResponseObject.<CommonPayment>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        PsuIdData psuIdData = getPsuIdDataFromRequest();
        ReadPaymentService readPaymentService = paymentServiceResolver.getReadPaymentService(commonPaymentResponse);
        String contentType = Optional.ofNullable(commonPaymentResponse.getContentType()).orElseGet(requestProviderService::getAcceptHeader);
        commonPaymentResponse.setContentType(contentType);
        PaymentInformationResponse<CommonPayment> response = readPaymentService.getPayment(commonPaymentResponse, psuIdData, encryptedPaymentId, contentType);

        if (response.hasError()) {
            log.info("Payment-ID [{}]. Read Payment failed: {}", encryptedPaymentId, response.getErrorHolder());
            return ResponseObject.<CommonPayment>builder()
                       .fail(response.getErrorHolder())
                       .build();
        }

        CommonPayment commonPayment = response.getPayment();
        String responseContentType = resolveContentType(contentType, commonPayment.getContentType());
        commonPayment.setContentType(responseContentType);
        loggingContextService.storeTransactionStatus(commonPayment.getTransactionStatus());

        return ResponseObject.<CommonPayment>builder()
                   .body(commonPayment)
                   .build();
    }

    /**
     * Retrieves payment status from ASPSP
     *
     * @param paymentType        The addressed payment category Single, Periodic or Bulk
     * @param paymentProduct     payment product used for payment creation (e.g. sepa-credit-transfers, instant-sepa-credit-transfers...)
     * @param encryptedPaymentId String representation of payment primary ASPSP identifier
     * @return Information about the status of a payment
     */
    public ResponseObject<GetPaymentStatusResponse> getPaymentStatusById(PaymentType paymentType, String paymentProduct, String encryptedPaymentId) {
        xs2aEventService.recordPisTppRequest(encryptedPaymentId, EventType.GET_TRANSACTION_STATUS_REQUEST_RECEIVED);
        Optional<PisCommonPaymentResponse> pisCommonPaymentOptional = pisCommonPaymentService.getPisCommonPaymentById(encryptedPaymentId);

        if (!pisCommonPaymentOptional.isPresent()) {
            log.info("Payment-ID [{}]. Get Payment Status failed. PIS CommonPayment not found by ID", encryptedPaymentId);
            return ResponseObject.<GetPaymentStatusResponse>builder()
                       .fail(PIS_403, of(RESOURCE_UNKNOWN_403))
                       .build();
        }

        PisCommonPaymentResponse pisCommonPaymentResponse = pisCommonPaymentOptional.get();

        ValidationResult validationResult = getPaymentStatusByIdValidator.validate(new GetPaymentStatusByIdPO(pisCommonPaymentResponse, paymentType, paymentProduct));
        if (validationResult.isNotValid()) {
            log.info("Payment-ID [{}]. Get payment status by ID - validation failed: {}", encryptedPaymentId, validationResult.getMessageError());
            return ResponseObject.<GetPaymentStatusResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        if (pisCommonPaymentResponse.getTransactionStatus() == TransactionStatus.RJCT) {
            return ResponseObject.<GetPaymentStatusResponse>builder().body(new GetPaymentStatusResponse(TransactionStatus.RJCT, null, MediaType.APPLICATION_JSON, null, null)).build();
        }

        SpiContextData spiContextData = spiContextDataProvider.provideWithPsuIdData(getPsuIdDataFromRequest());

        ReadPaymentStatusService readPaymentStatusService = paymentServiceResolver.getReadPaymentStatusService(pisCommonPaymentResponse);
        ReadPaymentStatusResponse readPaymentStatusResponse = readPaymentStatusService.readPaymentStatus(pisCommonPaymentResponse, spiContextData, encryptedPaymentId, requestProviderService.getAcceptHeader());

        if (readPaymentStatusResponse.hasError()) {
            ErrorHolder errorHolder = readPaymentStatusResponse.getErrorHolder();
            log.info("Payment-ID [{}]. Read Payment status failed: {}", encryptedPaymentId, errorHolder);
            return ResponseObject.<GetPaymentStatusResponse>builder()
                       .fail(errorHolder)
                       .build();
        }

        TransactionStatus transactionStatus = readPaymentStatusResponse.getStatus();

        if (transactionStatus == null) {
            log.info("Payment-ID [{}].  Get Payment Status by ID failed. Transaction status is null.", encryptedPaymentId);
            return ResponseObject.<GetPaymentStatusResponse>builder()
                       .fail(PIS_403, of(RESOURCE_UNKNOWN_403))
                       .build();
        }

        if (!updatePaymentAfterSpiService.updatePaymentStatus(encryptedPaymentId, transactionStatus)) {
            log.info("Payment ID: [{}], Transaction status: [{}]. Update of a payment status in the CMS has failed.",
                     encryptedPaymentId, transactionStatus);
        }

        loggingContextService.storeTransactionStatus(transactionStatus);

        GetPaymentStatusResponse response = new GetPaymentStatusResponse(transactionStatus, readPaymentStatusResponse.getFundsAvailable(), readPaymentStatusResponse.getResponseContentType(), readPaymentStatusResponse.getPaymentStatusRaw(), readPaymentStatusResponse.getPsuMessage());
        return ResponseObject.<GetPaymentStatusResponse>builder().body(response).build();
    }

    /**
     * Cancels payment by its ASPSP identifier and payment type
     *
     * @param paymentCancellationRequest {@link PisPaymentCancellationRequest}
     * @return Response containing information about cancelled payment or corresponding error
     */
    public ResponseObject<CancelPaymentResponse> cancelPayment(PisPaymentCancellationRequest paymentCancellationRequest) {
        xs2aEventService.recordPisTppRequest(paymentCancellationRequest.getEncryptedPaymentId(), EventType.PAYMENT_CANCELLATION_REQUEST_RECEIVED);
        Optional<PisCommonPaymentResponse> pisCommonPaymentOptional = pisCommonPaymentService.getPisCommonPaymentById(paymentCancellationRequest.getEncryptedPaymentId());

        if (!pisCommonPaymentOptional.isPresent()) {
            log.info("Payment-ID [{}]. Cancel payment has failed. Payment not found by ID.",
                     paymentCancellationRequest.getEncryptedPaymentId());
            return ResponseObject.<CancelPaymentResponse>builder()
                       .fail(PIS_403, of(RESOURCE_UNKNOWN_403))
                       .build();
        }

        PisCommonPaymentResponse pisCommonPaymentResponse = pisCommonPaymentOptional.get();
        CancelPaymentPO cancelPaymentPO = new CancelPaymentPO(pisCommonPaymentResponse, paymentCancellationRequest.getPaymentType(), paymentCancellationRequest.getPaymentProduct(), paymentCancellationRequest.getTppRedirectUri());

        ValidationResult validationResult = cancelPaymentValidator.validate(cancelPaymentPO);
        if (validationResult.isNotValid()) {
            log.info("Payment-ID [{}]. Cancel payment - validation failed: [{}]",
                     paymentCancellationRequest.getEncryptedPaymentId(), validationResult.getMessageError());
            return ResponseObject.<CancelPaymentResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        if (pisCommonPaymentResponse.getTransactionStatus().isFinalisedStatus()) {
            log.info("Payment-ID [{}]. Cancel payment has failed. Payment has finalised status",
                     paymentCancellationRequest.getEncryptedPaymentId());
            return ResponseObject.<CancelPaymentResponse>builder()
                       .fail(PIS_400, of(RESOURCE_BLOCKED))
                       .build();
        }

        CancelPaymentService cancelPaymentService = paymentServiceResolver.getCancelPaymentService(paymentCancellationRequest);
        ResponseObject<CancelPaymentResponse> responseObject = cancelPaymentService.cancelPayment(pisCommonPaymentResponse, paymentCancellationRequest);

        if (responseObject.hasError()) {
            log.info("Payment-ID: [{}]. Cancel payment failed: [{}]",
                     paymentCancellationRequest.getEncryptedPaymentId(),
                     responseObject.getError());
            return ResponseObject.<CancelPaymentResponse>builder()
                       .fail(responseObject.getError())
                       .build();
        }

        CancelPaymentResponse cancelPaymentResponse = responseObject.getBody();

        cancelPaymentResponse.setTppMessageInformation(cancelPaymentValidator.buildWarningMessages(cancelPaymentPO));

        loggingContextService.storeTransactionStatus(cancelPaymentResponse.getTransactionStatus());

        return ResponseObject.<CancelPaymentResponse>builder()
                   .body(cancelPaymentResponse)
                   .build();
    }

    private PsuIdData getPsuIdDataFromRequest() {
        PsuIdData psuIdData = requestProviderService.getPsuIdData();
        log.info("Corresponding PSU-ID {} was provided from request.", psuIdData);
        return psuIdData;
    }

    private boolean isNotSupportedScaApproach(ScaApproach scaApproach) {
        return !EnumSet.of(ScaApproach.REDIRECT, ScaApproach.EMBEDDED, ScaApproach.DECOUPLED).contains(scaApproach);
    }

    private String resolveContentType(String contentTypeBeforeSpi, String contentTypeAfterSpi) {
        String responseContentType = StringUtils.defaultIfBlank(contentTypeAfterSpi, contentTypeBeforeSpi);
        if (MediaType.ALL_VALUE.equals(responseContentType)) {
            responseContentType = MediaType.APPLICATION_JSON_VALUE;
        }
        return responseContentType;
    }
}
