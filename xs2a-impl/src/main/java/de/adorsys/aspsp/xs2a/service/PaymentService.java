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
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentType;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayment;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.service.payment.PaymentValidationService;
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
    private final PaymentValidationService paymentValidationService;
    private final ReadPaymentFactory readPaymentFactory;

    /**
     * Retrieves payment status from ASPSP
     *
     * @param paymentId      String representation of payment primary ASPSP identifier
     * @param paymentProduct The addressed payment product
     * @return Information about the status of a payment
     */
    public ResponseObject<TransactionStatus> getPaymentStatusById(String paymentId, String paymentProduct) {
        TransactionStatus transactionStatus = paymentMapper.mapToTransactionStatus(paymentSpi.getPaymentStatusById(paymentId, paymentProduct, new AspspConsentData("zzzzzzzzzzzzzz".getBytes())).getPayload()); //
        // https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
        return Optional.ofNullable(transactionStatus)
                   .map(tr -> ResponseObject.<TransactionStatus>builder()
                                  .body(tr).build())
                   .orElse(ResponseObject.<TransactionStatus>builder().fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_403))).build());
    }

    /**
     * Initiates periodic payment
     *
     * @param periodicPayment Periodic payment information
     * @param paymentProduct  The addressed payment product
     * @return Response containing information about created periodic payment or corresponding error
     */
    public ResponseObject<PaymentInitialisationResponse> initiatePeriodicPayment(PeriodicPayment periodicPayment, String paymentProduct) {
        Optional<MessageErrorCode> messageErrorCode = paymentValidationService.validatePeriodicPayment(periodicPayment, paymentProduct);
        if (messageErrorCode.isPresent()) {
            log.warn("Initiate periodic payment error: {} ", messageErrorCode.get().getName());
            return ResponseObject.<PaymentInitialisationResponse>builder()
                       .fail(new MessageError(new TppMessageInformation(ERROR, messageErrorCode.get())))
                       .build();
        }
        return scaPaymentService.createPeriodicPayment(periodicPayment, paymentProduct)
                   .map(resp -> ResponseObject.<PaymentInitialisationResponse>builder().body(resp).build())
                   .orElse(ResponseObject.<PaymentInitialisationResponse>builder()
                               .fail(new MessageError(new TppMessageInformation(ERROR, PAYMENT_FAILED)))
                               .build());
    }

    /**
     * Initiates a bulk payment
     *
     * @param payments       List of single payments forming bulk payment
     * @param paymentProduct The addressed payment product
     * @return List of payment initiation responses containing inforamtion about created payments or an error if non of the payments could pass the validation
     */
    public ResponseObject<List<PaymentInitialisationResponse>> createBulkPayments(List<SinglePayment> payments, String paymentProduct) {
        // TODO: should be validated by interceptors https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/166
        if (CollectionUtils.isEmpty(payments)) {
            return ResponseObject.<List<PaymentInitialisationResponse>>builder()
                       .fail(new MessageError(new TppMessageInformation(ERROR, FORMAT_ERROR)))
                       .build();
        }
        List<SinglePayment> validPayments = new ArrayList<>();
        List<PaymentInitialisationResponse> invalidPayments = new ArrayList<>();
        for (SinglePayment s : payments) {
            Optional<MessageErrorCode> messageErrorCode = paymentValidationService.validateSinglePayment(s, paymentProduct);
            if (messageErrorCode.isPresent()) {
                log.warn("Initiate bulk payment error: {} ", messageErrorCode.get().getName());
                paymentMapper.mapToPaymentInitResponseFailedPayment(s == null ? new SinglePayment() : s, messageErrorCode.get())
                    .map(invalidPayments::add);
            } else {
                validPayments.add(s);
            }
        }
        if (CollectionUtils.isNotEmpty(validPayments)) {
            List<PaymentInitialisationResponse> paymentResponses = scaPaymentService.createBulkPayment(validPayments, paymentProduct);
            if (CollectionUtils.isNotEmpty(paymentResponses) && paymentResponses.stream()
                                                                    .anyMatch(pr -> pr.getTransactionStatus() != TransactionStatus.RJCT)) {
                paymentResponses.addAll(invalidPayments);
                return ResponseObject.<List<PaymentInitialisationResponse>>builder()
                           .body(paymentResponses).build();
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
    public ResponseObject<PaymentInitialisationResponse> createPaymentInitiation(SinglePayment singlePayment, String paymentProduct) {
        Optional<MessageErrorCode> messageErrorCode = paymentValidationService.validateSinglePayment(singlePayment, paymentProduct);
        if (messageErrorCode.isPresent()) {
            log.warn("Initiate single payment error: {} ", messageErrorCode.get().getName());
            return ResponseObject.<PaymentInitialisationResponse>builder()
                       .fail(new MessageError(new TppMessageInformation(ERROR, messageErrorCode.get())))
                       .build();
        }
        return scaPaymentService.createSinglePayment(singlePayment, paymentProduct)
                   .map(resp -> ResponseObject.<PaymentInitialisationResponse>builder().body(resp).build())
                   .orElse(ResponseObject.<PaymentInitialisationResponse>builder()
                               .fail(new MessageError(new TppMessageInformation(ERROR, PAYMENT_FAILED)))
                               .build());
    }

    /**
     * Retrieves payment from ASPSP by its ASPSP identifier, product and payment type
     *
     * @param paymentType    type of payment (payments, bulk-payments, periodic-payments)
     * @param paymentProduct The addressed payment product
     * @param paymentId      ASPSP identifier of the payment
     * @return Response containing information about payment or corresponding error
     */
    public ResponseObject<Object> getPaymentById(PaymentType paymentType, String paymentProduct, String paymentId) {
        ReadPayment service = readPaymentFactory.getService(paymentType.getValue());
        Optional<Object> payment = Optional.ofNullable(service.getPayment(paymentProduct, paymentId));
        return payment.isPresent()
                   ? ResponseObject.builder().body(payment.get()).build()
                   : ResponseObject.builder().fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_403))).build();

    }
}
