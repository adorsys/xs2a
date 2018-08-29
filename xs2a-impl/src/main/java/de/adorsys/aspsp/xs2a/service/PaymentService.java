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
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.service.payment.ReadPayment;
import de.adorsys.aspsp.xs2a.service.payment.ReadPaymentFactory;
import de.adorsys.aspsp.xs2a.service.payment.ScaPaymentService;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.*;
import static de.adorsys.aspsp.xs2a.exception.MessageCategory.ERROR;

@Slf4j
@Service
@AllArgsConstructor
public class PaymentService {
    private final PaymentSpi paymentSpi;
    private final PaymentMapper paymentMapper;
    private final ScaPaymentService scaPaymentService;
    private final ReadPaymentFactory readPaymentFactory;

    /**
     * Retrieves payment status from ASPSP
     *
     * @param paymentId   String representation of payment primary ASPSP identifier
     * @param paymentType The addressed payment category Single, Periodic or Bulk
     * @return Information about the status of a payment
     */
    public ResponseObject<TransactionStatus> getPaymentStatusById(String paymentId, PaymentType paymentType) {
        TransactionStatus transactionStatus = paymentMapper.mapToTransactionStatus(paymentSpi.getPaymentStatusById(paymentId, paymentMapper.mapToSpiPaymentType(paymentType), new AspspConsentData("zzzzzzzzzzzzzz".getBytes())).getPayload());
        //TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
        return Optional.ofNullable(transactionStatus)
                   .map(tr -> ResponseObject.<TransactionStatus>builder().body(tr).build())
                   .orElseGet(() -> ResponseObject.<TransactionStatus>builder().fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_403))).build());
    }

    /**
     * Initiates periodic payment
     *
     * @param periodicPayment Periodic payment information
     * @param paymentProduct  The addressed payment product
     * @return Response containing information about created periodic payment or corresponding error
     */
    public ResponseObject<PaymentInitialisationResponse> initiatePeriodicPayment(PeriodicPayment periodicPayment, String tppSignatureCertificate, String paymentProduct) {
        return periodicPayment.areValidExecutionAndPeriodDates()
                   ? ResponseObject.<PaymentInitialisationResponse>builder().body(scaPaymentService.createPeriodicPayment(periodicPayment, paymentMapper.mapToTppInfo(tppSignatureCertificate), paymentProduct)).build()
                   : mapToFailResponse(periodicPayment, EXECUTION_DATE_INVALID);
    }

    /**
     * Initiates a bulk payment
     *
     * @param payments       List of single payments forming bulk payment
     * @param paymentProduct The addressed payment product
     * @return List of payment initiation responses containing information about created payments or an error if non of the payments could pass the validation
     */
    public ResponseObject<List<PaymentInitialisationResponse>> createBulkPayments(List<SinglePayment> payments, String tppSignatureCertificate, String paymentProduct) {
        if (CollectionUtils.isEmpty(payments)) {
            return ResponseObject.<List<PaymentInitialisationResponse>>builder()
                       .fail(new MessageError(new TppMessageInformation(ERROR, FORMAT_ERROR)))
                       .build();
        }
        List<SinglePayment> validPayments = new ArrayList<>();
        List<PaymentInitialisationResponse> invalidPayments = new ArrayList<>();
        for (SinglePayment payment : payments) {
            if (payment.isValidExecutionDateAndTime()) {
                validPayments.add(payment);
            } else {
                invalidPayments.add(paymentMapper.mapToPaymentInitResponseFailedPayment(payment, EXECUTION_DATE_INVALID));
            }
        }
        if (CollectionUtils.isNotEmpty(validPayments)) {
            List<PaymentInitialisationResponse> paymentResponses = scaPaymentService.createBulkPayment(validPayments, paymentMapper.mapToTppInfo(tppSignatureCertificate), paymentProduct);
            if (CollectionUtils.isNotEmpty(paymentResponses) && paymentResponses.stream()
                                                                    .anyMatch(pr -> pr.getTransactionStatus() != TransactionStatus.RJCT)) {
                paymentResponses.addAll(invalidPayments);
                return ResponseObject.<List<PaymentInitialisationResponse>>builder()
                           .body(paymentResponses).build();//TODO Temporary fix should be updated along migration to 1.2
            }
        }
        return ResponseObject.<List<PaymentInitialisationResponse>>builder()
                   .fail(new MessageError(new TppMessageInformation(ERROR, PAYMENT_FAILED))).build();
    }

    /**
     * Initiates a single payment
     *
     * @param singlePayment  Single payment information
     * @param paymentProduct The addressed payment product
     * @return Response containing information about created single payment or corresponding error
     */
    public ResponseObject<PaymentInitialisationResponse> createPaymentInitiation(SinglePayment singlePayment, String tppSignatureCertificate, String paymentProduct) {
        return singlePayment.isValidExecutionDateAndTime()
                   ? ResponseObject.<PaymentInitialisationResponse>builder().body(scaPaymentService.createSinglePayment(singlePayment, paymentMapper.mapToTppInfo(tppSignatureCertificate), paymentProduct)).build()
                   : mapToFailResponse(singlePayment, EXECUTION_DATE_INVALID);
    }

    /**
     * Retrieves payment from ASPSP by its ASPSP identifier, product and payment type
     *
     * @param paymentType type of payment (payments, bulk-payments, periodic-payments)
     * @param paymentId   ASPSP identifier of the payment
     * @return Response containing information about payment or corresponding error
     */
    public ResponseObject<Object> getPaymentById(PaymentType paymentType, String paymentId) {
        ReadPayment service = readPaymentFactory.getService(paymentType.getValue());
        Optional<Object> payment = Optional.ofNullable(service.getPayment(paymentId, "TMP")); //NOT USED IN 1.2
        return payment.isPresent()
                   ? ResponseObject.builder().body(payment.get()).build()
                   : ResponseObject.builder().fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_403))).build();
    }

    public <T, R> ResponseObject<R> createPayment(T payment, PaymentType paymentType, PaymentProduct paymentProduct, String tppSignatureCertificate) {
        ResponseObject<R> response;
        if (paymentType == PaymentType.SINGLE) {
            response = (ResponseObject<R>) createPaymentInitiation((SinglePayment) payment, tppSignatureCertificate, paymentProduct.getCode());
        } else if (paymentType == PaymentType.PERIODIC) {
            response = (ResponseObject<R>) initiatePeriodicPayment((PeriodicPayment) payment, tppSignatureCertificate, paymentProduct.getCode());
        } else {
            response = (ResponseObject<R>) createBulkPayments((List<SinglePayment>) payment, tppSignatureCertificate, paymentProduct.getCode());
        }
        return response;
    }

    private ResponseObject<PaymentInitialisationResponse> mapToFailResponse(SinglePayment payment, MessageErrorCode errorCode) {
        PaymentInitialisationResponse response = paymentMapper.mapToPaymentInitResponseFailedPayment(payment, errorCode);
        return ResponseObject.<PaymentInitialisationResponse>builder().body(response).build();
    }
}
