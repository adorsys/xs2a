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

import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.domain.MessageCode.PAYMENT_FAILED;
import static de.adorsys.aspsp.xs2a.exception.MessageCategory.ERROR;
import static org.springframework.util.CollectionUtils.isEmpty;

@Service
@AllArgsConstructor
public class PaymentService {
    private final MessageService messageService;
    private final PaymentSpi paymentSpi;
    private final PaymentMapper paymentMapper;

    public ResponseObject<Map<String, TransactionStatus>> getPaymentStatusById(String paymentId, PaymentProduct paymentProduct) {
        Map<String, TransactionStatus> paymentStatusResponse = new HashMap<>();
        TransactionStatus transactionStatus = paymentMapper.mapGetPaymentStatusById(paymentSpi.getPaymentStatusById(paymentId, paymentProduct.getCode()));
        paymentStatusResponse.put("transactionStatus", transactionStatus);

        // TODO add error response
        return ResponseObject.<Map<String, TransactionStatus>>builder()
            .body(paymentStatusResponse).build();
    }

    public ResponseObject<PaymentInitialisationResponse> initiatePeriodicPayment(PeriodicPayment periodicPayment, PaymentProduct paymentProduct, boolean tppRedirectPreferred) {
        SpiPaymentInitialisationResponse spiPeriodicPayment = paymentSpi.initiatePeriodicPayment(paymentMapper.mapToSpiPeriodicPayment(periodicPayment), paymentProduct.getCode(), tppRedirectPreferred);
        PaymentInitialisationResponse paymentInitiation = paymentMapper.mapFromSpiPaymentInitializationResponse(spiPeriodicPayment);

        return Optional.ofNullable(paymentInitiation)
            .map(resp -> ResponseObject.<PaymentInitialisationResponse>builder().body(resp).build())
            .orElse(ResponseObject.<PaymentInitialisationResponse>builder()
                .fail(new MessageError(new TppMessageInformation(ERROR, PAYMENT_FAILED)
                    .text(messageService.getMessage(PAYMENT_FAILED.name()))))
                .build());
    }

    public ResponseObject<List<PaymentInitialisationResponse>> createBulkPayments(List<SinglePayments> payments, PaymentProduct paymentProduct, boolean tppRedirectPreferred) {

        List<SpiSinglePayments> spiPayments = paymentMapper.mapToSpiSinglePaymentList(payments);
        List<SpiPaymentInitialisationResponse> spiPaymentInitiation = paymentSpi.createBulkPayments(spiPayments, paymentProduct.getCode(), tppRedirectPreferred);
        List<PaymentInitialisationResponse> paymentInitialisationResponse = spiPaymentInitiation.stream()
            .map(paymentMapper::mapFromSpiPaymentInitializationResponse)
            .collect(Collectors.toList());

        return isEmpty(paymentInitialisationResponse)
            ? ResponseObject.<List<PaymentInitialisationResponse>>builder()
                .fail(new MessageError(new TppMessageInformation(ERROR, PAYMENT_FAILED)
                .text(messageService.getMessage(PAYMENT_FAILED.name())))).build()
            : ResponseObject.<List<PaymentInitialisationResponse>>builder()
                .body(paymentInitialisationResponse).build();
    }

    public ResponseObject<PaymentInitialisationResponse> createPaymentInitiation(SinglePayments singlePayment, PaymentProduct paymentProduct, boolean tppRedirectPreferred) {
        SpiSinglePayments spiSinglePayments = paymentMapper.mapToSpiSinglePayments(singlePayment);
        SpiPaymentInitialisationResponse spiPaymentInitiation = paymentSpi.createPaymentInitiation(spiSinglePayments, paymentProduct.getCode(), tppRedirectPreferred);
        PaymentInitialisationResponse paymentInitialisationResponse = paymentMapper.mapFromSpiPaymentInitializationResponse(spiPaymentInitiation);

        return Optional.ofNullable(paymentInitialisationResponse)
            .map(resp -> ResponseObject.<PaymentInitialisationResponse>builder().body(resp).build())
            .orElse(ResponseObject.<PaymentInitialisationResponse>builder()
                .fail(new MessageError(new TppMessageInformation(ERROR, PAYMENT_FAILED)
                 .text(messageService.getMessage(PAYMENT_FAILED.name()))))
                .build());
    }
}
