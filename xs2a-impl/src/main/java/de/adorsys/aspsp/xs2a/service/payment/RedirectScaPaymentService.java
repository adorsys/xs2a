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
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayment;
import de.adorsys.aspsp.xs2a.domain.pis.TppInfo;
import de.adorsys.aspsp.xs2a.domain.consent.CreatePisConsentData;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.PisConsentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
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
    private final ConsentSpi consentSpi;
    private final PaymentMapper paymentMapper;
    private final PaymentSpi paymentSpi;
    private final PisConsentMapper pisConsentMapper;

    @Override
    public Optional<PaymentInitialisationResponse> createPeriodicPayment(PeriodicPayment periodicPayment, TppInfo tppInfo, String paymentProduct) {
        AspspConsentData aspspConsentData = new AspspConsentData("zzzzzzzzzzzzzz".getBytes()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here

        return createPeriodicPaymentAndGetResponse(periodicPayment, aspspConsentData)
                   .filter(pmt -> pmt.getTransactionStatus() != TransactionStatus.RJCT)
                   .map(resp -> createConsentForPeriodicPaymentAndExtendPaymentResponse(new CreatePisConsentData(periodicPayment, tppInfo, paymentProduct, aspspConsentData), resp));
    }

    private Optional<PaymentInitialisationResponse> createPeriodicPaymentAndGetResponse(PeriodicPayment periodicPayment, AspspConsentData aspspConsentData) {
        SpiPeriodicPayment spiPeriodicPayment = paymentMapper.mapToSpiPeriodicPayment(periodicPayment);
        return paymentMapper.mapToPaymentInitializationResponse(paymentSpi.initiatePeriodicPayment(spiPeriodicPayment, aspspConsentData).getPayload());
    }

    private PaymentInitialisationResponse createConsentForPeriodicPaymentAndExtendPaymentResponse(CreatePisConsentData createPisConsentData, PaymentInitialisationResponse response) {
        PisConsentRequest request = pisConsentMapper.mapToPisConsentRequestForPeriodicPayment(createPisConsentData, response.getPaymentId());
        String pisConsentId = consentSpi.createPisConsentForPeriodicPaymentAndGetId(request);
        String iban = createPisConsentData.getPeriodicPayment().getDebtorAccount().getIban();

        return StringUtils.isBlank(pisConsentId)
                   ? null
                   : extendPaymentResponseFields(response, iban, pisConsentId);
    }

    @Override
    public List<PaymentInitialisationResponse> createBulkPayment(List<SinglePayment> payments, TppInfo tppInfo, String paymentProduct) {
        AspspConsentData aspspConsentData = new AspspConsentData("zzzzzzzzzzzzzz".getBytes()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
        Map<SinglePayment, PaymentInitialisationResponse> paymentIdentifierMap = createBulkPaymentAndGetResponseMap(payments, aspspConsentData);

        return MapUtils.isNotEmpty(paymentIdentifierMap)
                   ? createConsentForBulkPaymentAndExtendPaymentResponses(new CreatePisConsentData(paymentIdentifierMap, tppInfo, paymentProduct, aspspConsentData))
                   : Collections.emptyList();
    }

    private Map<SinglePayment, PaymentInitialisationResponse> createBulkPaymentAndGetResponseMap(List<SinglePayment> payments, AspspConsentData aspspConsentData) {
        HashMap<SinglePayment, PaymentInitialisationResponse> paymentIdentifierMap = new HashMap<>();

        for (SinglePayment payment : payments) {
            Optional<PaymentInitialisationResponse> paymentInitialisationResponse = createSinglePaymentAndGetResponse(payment, aspspConsentData);
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

    private List<PaymentInitialisationResponse> createConsentForBulkPaymentAndExtendPaymentResponses(CreatePisConsentData createPisConsentData) {
        PisConsentRequest request = pisConsentMapper.mapToPisConsentRequestForBulkPayment(createPisConsentData);
        String pisConsentId = consentSpi.createPisConsentForBulkPaymentAndGetId(request);

        List<SinglePayment> singlePayments = Lists.newArrayList(createPisConsentData.getPaymentIdentifierMap().keySet());
        List<PaymentInitialisationResponse> responses = Lists.newArrayList(createPisConsentData.getPaymentIdentifierMap().values());

        return getDebtorIbanFromPayments(singlePayments)
                   .map(iban -> responses.stream()
                                    .map(resp -> extendPaymentResponseFields(resp, iban, pisConsentId))
                                    .collect(Collectors.toList()))
                   .orElseGet(Collections::emptyList);
    }

    @Override
    public Optional<PaymentInitialisationResponse> createSinglePayment(SinglePayment singlePayment, TppInfo tppInfo, String paymentProduct) {
        AspspConsentData aspspConsentData = new AspspConsentData("zzzzzzzzzzzzzz".getBytes()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here

        return createSinglePaymentAndGetResponse(singlePayment, aspspConsentData)
                   .filter(resp -> resp.getTransactionStatus() != TransactionStatus.RJCT)
                   .map(resp -> createConsentForSinglePaymentAndExtendPaymentResponse(new CreatePisConsentData(singlePayment, tppInfo, paymentProduct, aspspConsentData), resp));
    }

    private Optional<PaymentInitialisationResponse> createSinglePaymentAndGetResponse(SinglePayment singlePayment, AspspConsentData aspspConsentData) {
        SpiSinglePayment spiSinglePayment = paymentMapper.mapToSpiSinglePayment(singlePayment);
        SpiPaymentInitialisationResponse spiPeriodicPaymentResp = paymentSpi.createPaymentInitiation(spiSinglePayment, aspspConsentData).getPayload();
        return paymentMapper.mapToPaymentInitializationResponse(spiPeriodicPaymentResp);
    }

    private PaymentInitialisationResponse createConsentForSinglePaymentAndExtendPaymentResponse(CreatePisConsentData createPisConsentData, PaymentInitialisationResponse response) {

        PisConsentRequest request = pisConsentMapper.mapToPisConsentRequestForSinglePayment(createPisConsentData, response.getPaymentId());



        String pisConsentId = consentSpi.createPisConsentForSinglePaymentAndGetId(request);
        String iban = createPisConsentData.getSinglePayment().getDebtorAccount().getIban();

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
