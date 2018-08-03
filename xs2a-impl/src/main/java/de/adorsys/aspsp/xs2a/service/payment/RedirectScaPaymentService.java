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

package de.adorsys.aspsp.xs2a.service.payment;

import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.service.consent.pis.PisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.PAYMENT_FAILED;

@Service
@RequiredArgsConstructor
public class RedirectScaPaymentService implements ScaPaymentService {
    private final PisConsentService pisConsentService;
    private final PaymentMapper paymentMapper;
    private final PaymentSpi paymentSpi;

    @Override
    public Optional<PaymentInitialisationResponse> createPeriodicPayment(PeriodicPayment periodicPayment) {
        return createPeriodicPaymentAndGetResponse(periodicPayment)
                   .filter(pmt -> pmt.getTransactionStatus() != TransactionStatus.RJCT)
                   .map(resp -> createConsentForPeriodicPaymentAndExtendPaymentResponse(periodicPayment, resp));
    }

    private Optional<PaymentInitialisationResponse> createPeriodicPaymentAndGetResponse(PeriodicPayment periodicPayment) {
        SpiPeriodicPayment spiPeriodicPayment = paymentMapper.mapToSpiPeriodicPayment(periodicPayment);
        return paymentMapper.mapToPaymentInitializationResponse(paymentSpi.initiatePeriodicPayment(spiPeriodicPayment, new AspspConsentData("zzzzzzzzzzzzzz".getBytes())).getPayload()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
    }

    private PaymentInitialisationResponse createConsentForPeriodicPaymentAndExtendPaymentResponse(PeriodicPayment periodicPayment, PaymentInitialisationResponse response) {
        String pisConsentId = pisConsentService.createPisConsentForPeriodicPaymentAndGetId(response.getPaymentId());
        String iban = periodicPayment.getDebtorAccount().getIban();

        return StringUtils.isBlank(pisConsentId)
                   ? null
                   : extendPaymentResponseFields(response, iban, pisConsentId);
    }

    @Override
    public List<PaymentInitialisationResponse> createBulkPayment(List<SinglePayments> payments) {
        List<PaymentInitialisationResponse> responseList = createBulkPaymentAndGetResponse(payments);

        return CollectionUtils.isNotEmpty(responseList)
                   ? createConsentForBulkPaymentAndExtendPaymentResponses(payments, responseList)
                   : Collections.emptyList();
    }

    private List<PaymentInitialisationResponse> createBulkPaymentAndGetResponse(List<SinglePayments> payments) {
        List<SpiSinglePayments> spiPayments = paymentMapper.mapToSpiSinglePaymentList(payments);
        List<SpiPaymentInitialisationResponse> spiPaymentInitiations = paymentSpi.createBulkPayments(spiPayments,  new AspspConsentData("zzzzzzzzzzzzzz".getBytes())).getPayload(); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here

        List<PaymentInitialisationResponse> paymentResponses = spiPaymentInitiations.stream()
                                                                   .map(paymentMapper::mapToPaymentInitializationResponse)
                                                                   .filter(Optional::isPresent)
                                                                   .map(Optional::get)
                                                                   .collect(Collectors.toList());

        for (PaymentInitialisationResponse resp : paymentResponses) {
            if (StringUtils.isBlank(resp.getPaymentId())
                    || resp.getTransactionStatus() == TransactionStatus.RJCT) {
                resp.setTppMessages(new MessageErrorCode[]{PAYMENT_FAILED});
                resp.setTransactionStatus(TransactionStatus.RJCT);
            }
        }

        return paymentResponses;
    }

    private List<PaymentInitialisationResponse> createConsentForBulkPaymentAndExtendPaymentResponses(List<SinglePayments> payments, List<PaymentInitialisationResponse> responseList) {
        List<String> validPaymentIds = responseList.stream()
                                           .filter(pmt -> pmt.getTransactionStatus() != TransactionStatus.RJCT)
                                           .map(PaymentInitialisationResponse::getPaymentId)
                                           .collect(Collectors.toList());

        String pisConsentId = pisConsentService.createPisConsentForBulkPaymentAndGetId(validPaymentIds);

        return getDebtorIbanFromPayments(payments)
                   .map(iban -> responseList.stream()
                                    .map(resp -> extendPaymentResponseFields(resp, iban, pisConsentId))
                                    .collect(Collectors.toList()))
                   .orElse(Collections.emptyList());
    }

    @Override
    public Optional<PaymentInitialisationResponse> createSinglePayment(SinglePayments singlePayment) {
        return createSinglePaymentAndGetResponse(singlePayment)
                   .filter(resp -> resp.getTransactionStatus() != TransactionStatus.RJCT)
                   .map(resp -> createConsentForSinglePaymentAndExtendPaymentResponse(singlePayment, resp));
    }

    private Optional<PaymentInitialisationResponse> createSinglePaymentAndGetResponse(SinglePayments singlePayment) {
        SpiSinglePayments spiSinglePayments = paymentMapper.mapToSpiSinglePayments(singlePayment);
        SpiPaymentInitialisationResponse spiPeriodicPaymentResp = paymentSpi.createPaymentInitiation(spiSinglePayments,  new AspspConsentData("zzzzzzzzzzzzzz".getBytes())).getPayload(); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
        return paymentMapper.mapToPaymentInitializationResponse(spiPeriodicPaymentResp);
    }

    private PaymentInitialisationResponse createConsentForSinglePaymentAndExtendPaymentResponse(SinglePayments singlePayment, PaymentInitialisationResponse response) {
        String pisConsentId = pisConsentService.createPisConsentForSinglePaymentAndGetId(response.getPaymentId());
        String iban = singlePayment.getDebtorAccount().getIban();

        return StringUtils.isBlank(pisConsentId)
                   ? null
                   : extendPaymentResponseFields(response, iban, pisConsentId);
    }

    private PaymentInitialisationResponse extendPaymentResponseFields(PaymentInitialisationResponse response, String iban, String pisConsentId) {
        response.setPisConsentId(pisConsentId);
        response.setIban(iban);
        return response;
    }

    private Optional<String> getDebtorIbanFromPayments(List<SinglePayments> payments) {
        return Optional.ofNullable(payments.get(0).getDebtorAccount())
                   .map(AccountReference::getIban);
    }
}
