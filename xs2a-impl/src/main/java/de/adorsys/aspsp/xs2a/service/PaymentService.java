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
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.consent.pis.PisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.PAYMENT_FAILED;
import static de.adorsys.aspsp.xs2a.exception.MessageCategory.ERROR;


@Service
@AllArgsConstructor
public class PaymentService {
    private final PaymentSpi paymentSpi;
    private final PaymentMapper paymentMapper;
    private final AccountService accountService;
    private final PisConsentService pisConsentService;
    private final AspspProfileService aspspProfileService;

    /**
     * Get information about the status of a payment
     *
     * @param paymentId      Id for a required payment
     * @param paymentProduct The addressed payment product
     * @return Information about the status of a payment
     */
    public ResponseObject<TransactionStatus> getPaymentStatusById(String paymentId, String paymentProduct) {
        TransactionStatus transactionStatus = paymentMapper.mapToTransactionStatus(paymentSpi.getPaymentStatusById(paymentId, paymentProduct));

        return ResponseObject.<TransactionStatus>builder()
                   .body(transactionStatus).build();
    }

    /**
     * Initialises a new periodic payment
     *
     * @param periodicPayment      Information in order to create new periodic payment
     * @param paymentProduct       The addressed payment product endpoint for periodic payments
     * @param tppRedirectPreferred If it equals “true”, the TPP prefers a redirect over an embedded SCA approach
     * @return Response included information about created periodic payment
     */
    public ResponseObject<PaymentInitialisationResponse> initiatePeriodicPayment(PeriodicPayment periodicPayment, String paymentProduct, boolean tppRedirectPreferred) {
        PaymentInitialisationResponse paymentInitiation = null;

        if (periodicPayment != null
                && areAccountsExist(periodicPayment.getDebtorAccount(), periodicPayment.getCreditorAccount())) {

            paymentInitiation = aspspProfileService.isRedirectMode()
                                    ? getPeriodicPaymentResponseWhenRedirectMode(periodicPayment, paymentProduct, tppRedirectPreferred)
                                    : getPeriodicPaymentResponseWhenOAuthMode(periodicPayment, paymentProduct, tppRedirectPreferred);
        }

        return Optional.ofNullable(paymentInitiation)
                   .map(resp -> ResponseObject.<PaymentInitialisationResponse>builder().body(resp).build())
                   .orElse(ResponseObject.<PaymentInitialisationResponse>builder()
                               .fail(new MessageError(new TppMessageInformation(ERROR, PAYMENT_FAILED)))
                               .build());
    }

    /**
     * Initialises a bulk payment
     *
     * @param payments             List of payments in order to create bulk payments
     * @param paymentProduct       The addressed payment product endpoint for bulk payments
     * @param tppRedirectPreferred If it equals “true”, the TPP prefers a redirect over an embedded SCA approach
     * @return List of responses which are included information about created payments
     */
    public ResponseObject<List<PaymentInitialisationResponse>> createBulkPayments(List<SinglePayments> payments, String paymentProduct, boolean tppRedirectPreferred) {
        List<PaymentInitialisationResponse> paymentResponses = aspspProfileService.isRedirectMode()
                                                                   ? getBulkPaymentResponseWhenRedirectMode(payments, paymentProduct, tppRedirectPreferred)
                                                                   : getBulkPaymentResponseWhenOAuthMode(payments, paymentProduct, tppRedirectPreferred);


        return CollectionUtils.isEmpty(paymentResponses)
                   ? ResponseObject.<List<PaymentInitialisationResponse>>builder()
                         .fail(new MessageError(new TppMessageInformation(ERROR, PAYMENT_FAILED))).build()
                   : ResponseObject.<List<PaymentInitialisationResponse>>builder()
                         .body(paymentResponses).build();
    }

    /**
     * Initialises a single payment
     *
     * @param singlePayment        Payment in order to create single payments
     * @param paymentProduct       The addressed payment product endpoint for single payments
     * @param tppRedirectPreferred If it equals “true”, the TPP prefers a redirect over an embedded SCA approach
     * @return Response included information about created single payment
     */
    public ResponseObject<PaymentInitialisationResponse> createPaymentInitiation(SinglePayments singlePayment, String paymentProduct, boolean tppRedirectPreferred) {
        PaymentInitialisationResponse paymentInitialisationResponse = null;

        if (singlePayment != null
                && areAccountsExist(singlePayment.getDebtorAccount(), singlePayment.getCreditorAccount())) {

            paymentInitialisationResponse = aspspProfileService.isRedirectMode()
                                                ? getSinglePaymentResponseWhenRedirectMode(singlePayment, paymentProduct, tppRedirectPreferred)
                                                : getSinglePaymentResponseWhenOAuthMode(singlePayment, paymentProduct, tppRedirectPreferred);
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

    private PaymentInitialisationResponse getPeriodicPaymentResponseWhenRedirectMode(PeriodicPayment periodicPayment, String paymentProduct, boolean tppRedirectPreferred) {
        return StringUtils.isBlank(pisConsentService.createPisConsentForPeriodicPaymentAndGetId(periodicPayment))
                   ? null
                   : createPeriodicPaymentAndGetResponse(periodicPayment, paymentProduct, tppRedirectPreferred);
    }

    private PaymentInitialisationResponse getPeriodicPaymentResponseWhenOAuthMode(PeriodicPayment periodicPayment, String paymentProduct, boolean tppRedirectPreferred) {
        return createPeriodicPaymentAndGetResponse(periodicPayment, paymentProduct, tppRedirectPreferred);
    }

    private PaymentInitialisationResponse createPeriodicPaymentAndGetResponse(PeriodicPayment periodicPayment, String paymentProduct, boolean tppRedirectPreferred) {
        SpiPeriodicPayment spiPeriodicPayment = paymentMapper.mapToSpiPeriodicPayment(periodicPayment);
        SpiPaymentInitialisationResponse spiPeriodicPaymentResp = paymentSpi.initiatePeriodicPayment(spiPeriodicPayment, paymentProduct, tppRedirectPreferred);
        return paymentMapper.mapToPaymentInitializationResponse(spiPeriodicPaymentResp);
    }

    private List<PaymentInitialisationResponse> getBulkPaymentResponseWhenRedirectMode(List<SinglePayments> payments, String paymentProduct, boolean tppRedirectPreferred) {
        return StringUtils.isBlank(pisConsentService.createPisConsentForBulkPaymentAndGetId(payments))
                   ? null
                   : createBulkPaymentAndGetResponse(payments, paymentProduct, tppRedirectPreferred);
    }

    private List<PaymentInitialisationResponse> getBulkPaymentResponseWhenOAuthMode(List<SinglePayments> payments, String paymentProduct, boolean tppRedirectPreferred) {
        return createBulkPaymentAndGetResponse(payments, paymentProduct, tppRedirectPreferred);
    }

    private List<PaymentInitialisationResponse> createBulkPaymentAndGetResponse(List<SinglePayments> payments, String paymentProduct, boolean tppRedirectPreferred) {
        List<SinglePayments> validatedPayments = payments.stream()
                                                     .filter(Objects::nonNull)
                                                     .filter(pmt -> areAccountsExist(pmt.getDebtorAccount(), pmt.getCreditorAccount()))
                                                     .collect(Collectors.toList());

        List<SpiSinglePayments> spiPayments = paymentMapper.mapToSpiSinglePaymentList(validatedPayments);
        List<SpiPaymentInitialisationResponse> spiPaymentInitiations = paymentSpi.createBulkPayments(spiPayments, paymentProduct, tppRedirectPreferred);

        return spiPaymentInitiations.stream()
                   .map(paymentMapper::mapToPaymentInitializationResponse)
                   .collect(Collectors.toList());
    }

    private PaymentInitialisationResponse getSinglePaymentResponseWhenRedirectMode(SinglePayments singlePayment, String paymentProduct, boolean tppRedirectPreferred) {
        return StringUtils.isBlank(pisConsentService.createPisConsentForSinglePaymentAndGetId(singlePayment))
                   ? null
                   : createSinglePaymentAndGetResponse(singlePayment, paymentProduct, tppRedirectPreferred);
    }

    private PaymentInitialisationResponse getSinglePaymentResponseWhenOAuthMode(SinglePayments singlePayment, String paymentProduct, boolean tppRedirectPreferred) {
        return createSinglePaymentAndGetResponse(singlePayment, paymentProduct, tppRedirectPreferred);
    }

    private PaymentInitialisationResponse createSinglePaymentAndGetResponse(SinglePayments singlePayment, String paymentProduct, boolean tppRedirectPreferred) {
        SpiSinglePayments spiSinglePayments = paymentMapper.mapToSpiSinglePayments(singlePayment);
        SpiPaymentInitialisationResponse spiPeriodicPaymentResp = paymentSpi.createPaymentInitiation(spiSinglePayments, paymentProduct, tppRedirectPreferred);
        return paymentMapper.mapToPaymentInitializationResponse(spiPeriodicPaymentResp);
    }
}
