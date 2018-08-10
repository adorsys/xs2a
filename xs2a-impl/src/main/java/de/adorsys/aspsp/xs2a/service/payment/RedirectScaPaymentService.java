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

import com.google.common.collect.Lists;
import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayment;
import de.adorsys.aspsp.xs2a.domain.pis.TppInfo;
import de.adorsys.aspsp.xs2a.service.consent.pis.PisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.PAYMENT_FAILED;

@Service
@RequiredArgsConstructor
public class RedirectScaPaymentService implements ScaPaymentService {
    private final PisConsentService pisConsentService;
    private final PaymentMapper paymentMapper;
    private final PaymentSpi paymentSpi;

    @Override
    public Optional<PaymentInitialisationResponse> createPeriodicPayment(PeriodicPayment periodicPayment, TppInfo tppInfo, String paymentProduct) {
        return createPeriodicPaymentAndGetResponse(periodicPayment)
                   .filter(pmt -> pmt.getTransactionStatus() != TransactionStatus.RJCT)
                   .map(resp -> createConsentForPeriodicPaymentAndExtendPaymentResponse(periodicPayment, resp, tppInfo, paymentProduct));
    }

    private Optional<PaymentInitialisationResponse> createPeriodicPaymentAndGetResponse(PeriodicPayment periodicPayment) {
        SpiPeriodicPayment spiPeriodicPayment = paymentMapper.mapToSpiPeriodicPayment(periodicPayment);
        return paymentMapper.mapToPaymentInitializationResponse(paymentSpi.initiatePeriodicPayment(spiPeriodicPayment, new AspspConsentData("zzzzzzzzzzzzzz".getBytes())).getPayload()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
    }

    private PaymentInitialisationResponse createConsentForPeriodicPaymentAndExtendPaymentResponse(PeriodicPayment periodicPayment, PaymentInitialisationResponse response, TppInfo tppInfo, String paymentProduct) {
        String pisConsentId = pisConsentService.createPisConsentForPeriodicPaymentAndGetId(periodicPayment, response.getPaymentId(), tppInfo, paymentProduct);
        String iban = periodicPayment.getDebtorAccount().getIban();

        return StringUtils.isBlank(pisConsentId)
                   ? null
                   : extendPaymentResponseFields(response, iban, pisConsentId);
    }

    @Override
    public List<PaymentInitialisationResponse> createBulkPayment(List<SinglePayment> payments, TppInfo tppInfo, String paymentProduct) {
        Map<SinglePayment, PaymentInitialisationResponse> paymentIdentifierMap = createBulkPaymentAndGetResponseMap(payments);

        return MapUtils.isNotEmpty(paymentIdentifierMap)
                   ? createConsentForBulkPaymentAndExtendPaymentResponses(paymentIdentifierMap, tppInfo, paymentProduct)
                   : Collections.emptyList();
    }

    private Map<SinglePayment, PaymentInitialisationResponse> createBulkPaymentAndGetResponseMap(List<SinglePayment> payments) {
        HashMap<SinglePayment, PaymentInitialisationResponse> paymentIdentifierMap = new HashMap<>();

        for (SinglePayment payment : payments) {
            Optional<PaymentInitialisationResponse> paymentInitialisationResponse = createSinglePaymentAndGetResponse(payment);
            paymentInitialisationResponse.ifPresent(resp -> paymentIdentifierMap.put(payment, resp));
        }

        paymentIdentifierMap.forEach((sp, resp) -> {
            if (StringUtils.isBlank(resp.getPaymentId())
                    || resp.getTransactionStatus() == TransactionStatus.RJCT) {
                resp.setTppMessages(new MessageErrorCode[]{PAYMENT_FAILED});
                resp.setTransactionStatus(TransactionStatus.RJCT);
            }
        });
        return paymentIdentifierMap;
    }

    private List<PaymentInitialisationResponse> createBulkPaymentAndGetResponse(List<SinglePayment> payments) {  // NOPMD return when we make storing payment info with payment ID
        List<SpiSinglePayment> spiPayments = paymentMapper.mapToSpiSinglePaymentList(payments);
        List<SpiPaymentInitialisationResponse> spiPaymentInitiations = paymentSpi.createBulkPayments(spiPayments, new AspspConsentData("zzzzzzzzzzzzzz".getBytes())).getPayload(); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here

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

    private List<PaymentInitialisationResponse> createConsentForBulkPaymentAndExtendPaymentResponses(Map<SinglePayment, PaymentInitialisationResponse> paymentIdentifierMap, TppInfo tppInfo, String paymentProduct) {
        String pisConsentId = pisConsentService.createPisConsentForBulkPaymentAndGetId(paymentIdentifierMap, tppInfo, paymentProduct);

        List<SinglePayment> singlePayments = Lists.newArrayList(paymentIdentifierMap.keySet());
        List<PaymentInitialisationResponse> responses = Lists.newArrayList(paymentIdentifierMap.values());

        return getDebtorIbanFromPayments(singlePayments)
                   .map(iban -> responses.stream()
                                    .map(resp -> extendPaymentResponseFields(resp, iban, pisConsentId))
                                    .collect(Collectors.toList()))
                   .orElse(Collections.emptyList());
    }

    @Override
    public Optional<PaymentInitialisationResponse> createSinglePayment(SinglePayment singlePayment, TppInfo tppInfo, String paymentProduct) {
        return createSinglePaymentAndGetResponse(singlePayment)
                   .filter(resp -> resp.getTransactionStatus() != TransactionStatus.RJCT)
                   .map(resp -> createConsentForSinglePaymentAndExtendPaymentResponse(singlePayment, resp, tppInfo, paymentProduct));
    }

    private Optional<PaymentInitialisationResponse> createSinglePaymentAndGetResponse(SinglePayment singlePayment) {
        SpiSinglePayment spiSinglePayment = paymentMapper.mapToSpiSinglePayment(singlePayment);
        SpiPaymentInitialisationResponse spiPeriodicPaymentResp = paymentSpi.createPaymentInitiation(spiSinglePayment, new AspspConsentData("zzzzzzzzzzzzzz".getBytes())).getPayload(); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
        return paymentMapper.mapToPaymentInitializationResponse(spiPeriodicPaymentResp);
    }

    private PaymentInitialisationResponse createConsentForSinglePaymentAndExtendPaymentResponse(SinglePayment singlePayment, PaymentInitialisationResponse response, TppInfo tppInfo, String paymentProduct) {

        String pisConsentId = pisConsentService.createPisConsentForSinglePaymentAndGetId(singlePayment, response.getPaymentId(), tppInfo, paymentProduct);
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

    private Optional<String> getDebtorIbanFromPayments(List<SinglePayment> payments) {
        return Optional.ofNullable(payments.get(0).getDebtorAccount())
                   .map(AccountReference::getIban);
    }
}
