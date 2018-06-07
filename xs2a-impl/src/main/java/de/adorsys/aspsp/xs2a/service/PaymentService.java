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

import de.adorsys.aspsp.xs2a.domain.AccountReference;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.domain.MessageCode.PAYMENT_FAILED;
import static de.adorsys.aspsp.xs2a.exception.MessageCategory.ERROR;

@Service
@AllArgsConstructor
public class PaymentService {
    private final PaymentSpi paymentSpi;
    private final PaymentMapper paymentMapper;
    private final AccountService accountService;

    public ResponseObject<TransactionStatus> getPaymentStatusById(String paymentId, String paymentProduct) {
        TransactionStatus transactionStatus = paymentMapper.mapToTransactionStatus(paymentSpi.getPaymentStatusById(paymentId, paymentProduct));

        return ResponseObject.<TransactionStatus>builder()
                   .body(transactionStatus).build();
    }

    public ResponseObject<PaymentInitialisationResponse> initiatePeriodicPayment(PeriodicPayment periodicPayment, String paymentProduct, boolean tppRedirectPreferred) {
        PaymentInitialisationResponse paymentInitiation = null;

        if (periodicPayment != null
                && areAccountsExist(periodicPayment.getDebtorAccount(), periodicPayment.getCreditorAccount())) {

            SpiPaymentInitialisationResponse spiPeriodicPayment = paymentSpi.initiatePeriodicPayment(paymentMapper.mapToSpiPeriodicPayment(periodicPayment), paymentProduct, tppRedirectPreferred);
            paymentInitiation = paymentMapper.mapToPaymentInitializationResponse(spiPeriodicPayment);
        }

        return Optional.ofNullable(paymentInitiation)
                   .map(resp -> ResponseObject.<PaymentInitialisationResponse>builder().body(resp).build())
                   .orElse(ResponseObject.<PaymentInitialisationResponse>builder()
                               .fail(new MessageError(new TppMessageInformation(ERROR, PAYMENT_FAILED)))
                               .build());
    }

    public ResponseObject<List<PaymentInitialisationResponse>> createBulkPayments(List<SinglePayments> payments, String paymentProduct, boolean tppRedirectPreferred) {
        List<SinglePayments> validatedPayments = payments.stream()
                                                     .filter(Objects::nonNull)
                                                     .filter(pmt -> areAccountsExist(pmt.getDebtorAccount(), pmt.getCreditorAccount()))
                                                     .collect(Collectors.toList());

        List<SpiSinglePayments> spiPayments = paymentMapper.mapToSpiSinglePaymentList(validatedPayments);
        List<SpiPaymentInitialisationResponse> spiPaymentInitiations = paymentSpi.createBulkPayments(spiPayments, paymentProduct, tppRedirectPreferred);
        List<PaymentInitialisationResponse> paymentResponses = spiPaymentInitiations.stream()
                                                                   .map(paymentMapper::mapToPaymentInitializationResponse)
                                                                   .collect(Collectors.toList());
        return CollectionUtils.isEmpty(paymentResponses)
                   ? ResponseObject.<List<PaymentInitialisationResponse>>builder()
                         .fail(new MessageError(new TppMessageInformation(ERROR, PAYMENT_FAILED))).build()
                   : ResponseObject.<List<PaymentInitialisationResponse>>builder()
                         .body(paymentResponses).build();
    }

    public ResponseObject<PaymentInitialisationResponse> createPaymentInitiation(SinglePayments singlePayment, String paymentProduct, boolean tppRedirectPreferred) {
        PaymentInitialisationResponse paymentInitialisationResponse = null;

        if (singlePayment != null
                && areAccountsExist(singlePayment.getDebtorAccount(), singlePayment.getCreditorAccount())) {
            SpiSinglePayments spiSinglePayments = paymentMapper.mapToSpiSinglePayments(singlePayment);
            SpiPaymentInitialisationResponse spiPaymentInitiation = paymentSpi.createPaymentInitiation(spiSinglePayments, paymentProduct, tppRedirectPreferred);
            paymentInitialisationResponse = paymentMapper.mapToPaymentInitializationResponse(spiPaymentInitiation);
        }

        return Optional.ofNullable(paymentInitialisationResponse)
                   .map(resp -> ResponseObject.<PaymentInitialisationResponse>builder().body(resp).build())
                   .orElse(ResponseObject.<PaymentInitialisationResponse>builder()
                               .fail(new MessageError(new TppMessageInformation(ERROR, PAYMENT_FAILED)))
                               .build());
    }

    private boolean areAccountsExist(AccountReference debtorAccount, AccountReference creditorAccount) {
        return accountService.isAccountExists(debtorAccount)
                   && accountService.isAccountExists(creditorAccount);
    }
}
