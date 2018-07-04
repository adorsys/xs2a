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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.*;
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
     * Retrieves payment status from ASPSP
     *
     * @param paymentId      String representation of payment primary ASPSP identifier
     * @param paymentProduct The addressed payment product
     * @return Information about the status of a payment
     */
    public ResponseObject<TransactionStatus> getPaymentStatusById(String paymentId, String paymentProduct) {
        TransactionStatus transactionStatus = paymentMapper.mapToTransactionStatus(paymentSpi.getPaymentStatusById(paymentId, paymentProduct));

        return ResponseObject.<TransactionStatus>builder()
                   .body(transactionStatus).build();
    }

    /**
     * Initiates periodic payment
     *
     * @param periodicPayment      Periodic payment information
     * @param paymentProduct       The addressed payment product
     * @param tppRedirectPreferred boolean representation of TPP's desire to use redirect approach
     * @return Response containing information about created periodic payment or corresponding error
     */
    public ResponseObject<PaymentInitialisationResponse> initiatePeriodicPayment(PeriodicPayment periodicPayment, String paymentProduct, boolean tppRedirectPreferred) {
        ResponseObject paymentRelatedErrors = containsPeriodicPaymentRelatedErrors(periodicPayment, paymentProduct);
        if (paymentRelatedErrors.hasError()) {
            return ResponseObject.<PaymentInitialisationResponse>builder().fail(paymentRelatedErrors.getError()).build();
        }

        Optional<PaymentInitialisationResponse> paymentInitiation = aspspProfileService.isRedirectMode()
                                                                        ? getPeriodicPaymentResponseWhenRedirectMode(periodicPayment)
                                                                        : getPeriodicPaymentResponseWhenOAuthMode(periodicPayment, paymentProduct, tppRedirectPreferred);

        return paymentInitiation
                   .map(resp -> ResponseObject.<PaymentInitialisationResponse>builder().body(resp).build())
                   .orElse(ResponseObject.<PaymentInitialisationResponse>builder()
                               .fail(new MessageError(new TppMessageInformation(ERROR, PAYMENT_FAILED)))
                               .build());
    }

    /**
     * Initiates a bulk payment
     *
     * @param payments             List of single payments forming bulk payment
     * @param paymentProduct       The addressed payment product
     * @param tppRedirectPreferred boolean representation of TPP's desire to use redirect approach
     * @return List of payment initiation responses containing inforamtion about created payments or an error if non of the payments could pass the validation
     */
    public ResponseObject<List<PaymentInitialisationResponse>> createBulkPayments(List<SinglePayments> payments, String paymentProduct, boolean tppRedirectPreferred) {
        if (CollectionUtils.isEmpty(payments)) {
            return ResponseObject.<List<PaymentInitialisationResponse>>builder()
                       .fail(new MessageError(new TppMessageInformation(ERROR, FORMAT_ERROR)))
                       .build();
        }
        List<SinglePayments> validPayments = new ArrayList<>();
        List<PaymentInitialisationResponse> invalidPayments = new ArrayList<>();
        for (SinglePayments s : payments) {
            ResponseObject paymentRelatedErrors = containsSinglePaymentRelatedErrors(s, paymentProduct);
            if (paymentRelatedErrors.hasError()) {
                paymentMapper.mapToPaymentInitResponseFailedPayment(s == null ? new SinglePayments() : s, paymentRelatedErrors.getError().getTppMessage().getCode(), tppRedirectPreferred)
                    .map(invalidPayments::add);
            } else {
                validPayments.add(s);
            }
        }
        if (CollectionUtils.isNotEmpty(validPayments)) {
            List<PaymentInitialisationResponse> paymentResponses = getBulkPaymentResponses(paymentProduct, tppRedirectPreferred, validPayments);
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
     * @param singlePayment        Single payment information
     * @param paymentProduct       The addressed payment product
     * @param tppRedirectPreferred boolean representation of TPP's desire to use redirect approach
     * @return Response containing information about created single payment or corresponding error
     */
    public ResponseObject<PaymentInitialisationResponse> createPaymentInitiation(SinglePayments singlePayment, String paymentProduct, boolean tppRedirectPreferred) {
        ResponseObject paymentRelatedErrors = containsSinglePaymentRelatedErrors(singlePayment, paymentProduct);
        if (paymentRelatedErrors.hasError()) {
            return ResponseObject.<PaymentInitialisationResponse>builder().fail(paymentRelatedErrors.getError()).build();
        }

        Optional<PaymentInitialisationResponse> paymentInitResp = aspspProfileService.isRedirectMode()
                                                                      ? getSinglePaymentResponseWhenRedirectMode(singlePayment)
                                                                      : getSinglePaymentResponseWhenOAuthMode(singlePayment, paymentProduct, tppRedirectPreferred);

        return paymentInitResp
                   .map(resp -> ResponseObject.<PaymentInitialisationResponse>builder().body(resp).build())
                   .orElse(ResponseObject.<PaymentInitialisationResponse>builder()
                               .fail(new MessageError(new TppMessageInformation(ERROR, PAYMENT_FAILED)))
                               .build());
    }

    private List<PaymentInitialisationResponse> getBulkPaymentResponses(String paymentProduct, boolean tppRedirectPreferred, List<SinglePayments> validPayments) {
        return aspspProfileService.isRedirectMode()
                   ? getBulkPaymentResponseWhenRedirectMode(validPayments)
                   : getBulkPaymentResponseWhenOAuthMode(validPayments, paymentProduct, tppRedirectPreferred);
    }

    private ResponseObject containsSinglePaymentRelatedErrors(SinglePayments payment, String paymentProduct) {
        return payment != null && payment.isValidDated()
                   ? containsPaymentRelatedErrors(payment, paymentProduct)
                   : ResponseObject.builder()
                         .fail(new MessageError(new TppMessageInformation(ERROR, FORMAT_ERROR)))
                         .build();
    }

    private ResponseObject containsPeriodicPaymentRelatedErrors(PeriodicPayment payment, String paymentProduct) {
        return payment != null && payment.isValidDate()
                   ? containsPaymentRelatedErrors(payment, paymentProduct)
                   : ResponseObject.builder()
                         .fail(new MessageError(new TppMessageInformation(ERROR, FORMAT_ERROR)))
                         .build();
    }

    private ResponseObject containsPaymentRelatedErrors(SinglePayments payment, String paymentProduct) {
        if (!accountService.getAccountDetailsByAccountReference(payment.getDebtorAccount()).isPresent()
                && !accountService.getAccountDetailsByAccountReference(payment.getCreditorAccount()).isPresent()) {
            return ResponseObject.builder()
                       .fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_400)))
                       .build();
        }
        if (accountService.isInvalidPaymentProductForPsu(payment.getDebtorAccount(), paymentProduct)) {
            return ResponseObject.<PaymentInitialisationResponse>builder()
                       .fail(new MessageError(new TppMessageInformation(ERROR, PRODUCT_INVALID)))
                       .build();
        }
        return ResponseObject.builder().build();
    }

    private Optional<PaymentInitialisationResponse> getPeriodicPaymentResponseWhenRedirectMode(PeriodicPayment periodicPayment) {
        String pisConsentId = pisConsentService.createPisConsentForPeriodicPaymentAndGetId(periodicPayment);

        if (StringUtils.isNotBlank(pisConsentId)) {
            PaymentInitialisationResponse response = new PaymentInitialisationResponse();
            response.setTransactionStatus(TransactionStatus.ACCP);
            response.setPisConsentId(pisConsentId);
            response.setIban(periodicPayment.getDebtorAccount().getIban());

            return Optional.of(response);
        }

        return Optional.empty();
    }

    private Optional<PaymentInitialisationResponse> getPeriodicPaymentResponseWhenOAuthMode(PeriodicPayment periodicPayment, String paymentProduct, boolean tppRedirectPreferred) {
        return createPeriodicPaymentAndGetResponse(periodicPayment, paymentProduct, tppRedirectPreferred);
    }

    private Optional<PaymentInitialisationResponse> createPeriodicPaymentAndGetResponse(PeriodicPayment periodicPayment, String paymentProduct, boolean tppRedirectPreferred) {
        SpiPeriodicPayment spiPeriodicPayment = paymentMapper.mapToSpiPeriodicPayment(periodicPayment);
        return paymentMapper.mapToPaymentInitializationResponse(paymentSpi.initiatePeriodicPayment(spiPeriodicPayment, paymentProduct, tppRedirectPreferred));
    }

    private List<PaymentInitialisationResponse> getBulkPaymentResponseWhenRedirectMode(List<SinglePayments> payments) {
        String pisConsentId = pisConsentService.createPisConsentForBulkPaymentAndGetId(payments);

        if (!StringUtils.isBlank(pisConsentId)) {
            PaymentInitialisationResponse response = new PaymentInitialisationResponse();
            response.setTransactionStatus(TransactionStatus.ACCP);
            response.setPisConsentId(pisConsentId);
            response.setIban(payments.get(0).getDebtorAccount().getIban()); // TODO Establish order of payment creation with pis consent https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/159

            return new ArrayList<>(Collections.singletonList(response));
        }

        return Collections.emptyList();
    }

    private List<PaymentInitialisationResponse> getBulkPaymentResponseWhenOAuthMode(List<SinglePayments> payments, String paymentProduct, boolean tppRedirectPreferred) {
        return createBulkPaymentAndGetResponse(payments, paymentProduct, tppRedirectPreferred);
    }

    private List<PaymentInitialisationResponse> createBulkPaymentAndGetResponse(List<SinglePayments> payments, String paymentProduct, boolean tppRedirectPreferred) {
        List<SpiSinglePayments> spiPayments = paymentMapper.mapToSpiSinglePaymentList(payments);
        List<SpiPaymentInitialisationResponse> spiPaymentInitiations = paymentSpi.createBulkPayments(spiPayments, paymentProduct, tppRedirectPreferred);

        return spiPaymentInitiations.stream()
                   .map(paymentMapper::mapToPaymentInitializationResponse)
                   .filter(Optional::isPresent)
                   .map(Optional::get)
                   .peek(resp -> {
                       if (StringUtils.isBlank(resp.getPaymentId()) || resp.getTransactionStatus() == TransactionStatus.RJCT) {
                           resp.setTppMessages(new MessageErrorCode[]{PAYMENT_FAILED});
                           resp.setTransactionStatus(TransactionStatus.RJCT);
                       }
                   })
                   .collect(Collectors.toList());
    }

    private Optional<PaymentInitialisationResponse> getSinglePaymentResponseWhenRedirectMode(SinglePayments singlePayment) {
        String pisConsentId = pisConsentService.createPisConsentForSinglePaymentAndGetId(singlePayment);

        if (!StringUtils.isBlank(pisConsentId)) {
            PaymentInitialisationResponse response = new PaymentInitialisationResponse();
            response.setTransactionStatus(TransactionStatus.RCVD);
            response.setPisConsentId(pisConsentId);
            response.setIban(singlePayment.getDebtorAccount().getIban());

            return Optional.of(response);
        }
        return Optional.empty();
    }

    private Optional<PaymentInitialisationResponse> getSinglePaymentResponseWhenOAuthMode(SinglePayments singlePayment, String paymentProduct, boolean tppRedirectPreferred) {
        return createSinglePaymentAndGetResponse(singlePayment, paymentProduct, tppRedirectPreferred);
    }

    private Optional<PaymentInitialisationResponse> createSinglePaymentAndGetResponse(SinglePayments singlePayment, String paymentProduct, boolean tppRedirectPreferred) {
        SpiSinglePayments spiSinglePayments = paymentMapper.mapToSpiSinglePayments(singlePayment);
        SpiPaymentInitialisationResponse spiPeriodicPaymentResp = paymentSpi.createPaymentInitiation(spiSinglePayments, paymentProduct, tppRedirectPreferred);
        return paymentMapper.mapToPaymentInitializationResponse(spiPeriodicPaymentResp);
    }
}
