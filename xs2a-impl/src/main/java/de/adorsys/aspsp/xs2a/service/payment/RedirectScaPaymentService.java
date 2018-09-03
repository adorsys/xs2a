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
import de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayment;
import de.adorsys.aspsp.xs2a.domain.pis.TppInfo;
import de.adorsys.aspsp.xs2a.service.consent.pis.CreateConsentRequest;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.PAYMENT_FAILED;

@Service
@RequiredArgsConstructor
public class RedirectScaPaymentService implements ScaPaymentService {
    private final PisConsentService pisConsentService;
    private final PaymentMapper paymentMapper;
    private final PaymentSpi paymentSpi;

    @Override
    public PaymentInitialisationResponse createPeriodicPayment(PeriodicPayment periodicPayment, TppInfo tppInfo, String paymentProduct) {
        AspspConsentData aspspConsentData = new AspspConsentData(); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here

        PaymentInitialisationResponse response = createPeriodicPaymentAndGetResponse(periodicPayment, aspspConsentData);
        return response.getTransactionStatus() != Xs2aTransactionStatus.RJCT
                   ? createConsentForPeriodicPaymentAndExtendPaymentResponse(new CreateConsentRequest(periodicPayment, tppInfo, paymentProduct, aspspConsentData), response)
                   : response;
    }

    private PaymentInitialisationResponse createPeriodicPaymentAndGetResponse(PeriodicPayment periodicPayment, AspspConsentData aspspConsentData) {
        SpiPeriodicPayment spiPeriodicPayment = paymentMapper.mapToSpiPeriodicPayment(periodicPayment);
        return paymentMapper.mapToPaymentInitializationResponse(paymentSpi.initiatePeriodicPayment(spiPeriodicPayment, aspspConsentData).getPayload());
    }

    private PaymentInitialisationResponse createConsentForPeriodicPaymentAndExtendPaymentResponse(CreateConsentRequest createConsentRequest, PaymentInitialisationResponse response) {
        String pisConsentId = pisConsentService.createPisConsentForPeriodicPaymentAndGetId(createConsentRequest, response.getPaymentId());
        return StringUtils.isBlank(pisConsentId)
                   ? null
                   : extendPaymentResponseFields(response, pisConsentId);
    }

    @Override
    public List<PaymentInitialisationResponse> createBulkPayment(List<SinglePayment> payments, TppInfo tppInfo, String paymentProduct) {
        AspspConsentData aspspConsentData = new AspspConsentData(); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
        Map<SinglePayment, PaymentInitialisationResponse> paymentIdentifierMap = createBulkPaymentAndGetResponseMap(payments, aspspConsentData);

        return MapUtils.isNotEmpty(paymentIdentifierMap)
                   ? createConsentForBulkPaymentAndExtendPaymentResponses(new CreateConsentRequest(paymentIdentifierMap, tppInfo, paymentProduct, aspspConsentData))
                   : Collections.emptyList();
    }

    private Map<SinglePayment, PaymentInitialisationResponse> createBulkPaymentAndGetResponseMap(List<SinglePayment> payments, AspspConsentData aspspConsentData) {
        HashMap<SinglePayment, PaymentInitialisationResponse> paymentIdentifierMap = new HashMap<>();

        for (SinglePayment payment : payments) {
            PaymentInitialisationResponse paymentInitialisationResponse = createSinglePaymentAndGetResponse(payment, aspspConsentData);
            paymentIdentifierMap.put(payment, paymentInitialisationResponse);
        }

        paymentIdentifierMap.forEach((sp, resp) -> {
            if (StringUtils.isBlank(resp.getPaymentId())
                    || resp.getTransactionStatus() == Xs2aTransactionStatus.RJCT) {
                resp.setTppMessages(new MessageErrorCode[]{PAYMENT_FAILED});
                resp.setTransactionStatus(Xs2aTransactionStatus.RJCT);
            }
        });
        return paymentIdentifierMap;
    }

    private List<PaymentInitialisationResponse> createBulkPaymentAndGetResponse(List<SinglePayment> payments) {  // NOPMD return when we make storing payment info with payment ID
        List<SpiSinglePayment> spiPayments = paymentMapper.mapToSpiSinglePaymentList(payments);
        List<SpiPaymentInitialisationResponse> spiPaymentInitiations = paymentSpi.createBulkPayments(spiPayments, new AspspConsentData()).getPayload(); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here

        List<PaymentInitialisationResponse> paymentResponses = spiPaymentInitiations.stream()
                                                                   .map(paymentMapper::mapToPaymentInitializationResponse)
                                                                   .collect(Collectors.toList());

        for (PaymentInitialisationResponse resp : paymentResponses) {
            if (StringUtils.isBlank(resp.getPaymentId())
                    || resp.getTransactionStatus() == Xs2aTransactionStatus.RJCT) {
                resp.setTppMessages(new MessageErrorCode[]{PAYMENT_FAILED});
                resp.setTransactionStatus(Xs2aTransactionStatus.RJCT);
            }
        }

        return paymentResponses;
    }

    private List<PaymentInitialisationResponse> createConsentForBulkPaymentAndExtendPaymentResponses(CreateConsentRequest createConsentRequest) {
        String pisConsentId = pisConsentService.createPisConsentForBulkPaymentAndGetId(createConsentRequest);
        List<PaymentInitialisationResponse> responses = Lists.newArrayList(createConsentRequest.getPaymentIdentifierMap().values());
        return responses.stream()
                   .map(resp -> extendPaymentResponseFields(resp, pisConsentId))
                   .collect(Collectors.toList());
    }

    @Override
    public PaymentInitialisationResponse createSinglePayment(SinglePayment singlePayment, TppInfo tppInfo, String paymentProduct) {
        AspspConsentData aspspConsentData = new AspspConsentData(); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here

        PaymentInitialisationResponse response = createSinglePaymentAndGetResponse(singlePayment, aspspConsentData);
        return response.getTransactionStatus() != Xs2aTransactionStatus.RJCT
                   ? createConsentForSinglePaymentAndExtendPaymentResponse(new CreateConsentRequest(singlePayment, tppInfo, paymentProduct, aspspConsentData), response)
                   : response;
    }

    private PaymentInitialisationResponse createSinglePaymentAndGetResponse(SinglePayment singlePayment, AspspConsentData aspspConsentData) {
        SpiSinglePayment spiSinglePayment = paymentMapper.mapToSpiSinglePayment(singlePayment);
        SpiPaymentInitialisationResponse spiPeriodicPaymentResp = paymentSpi.createPaymentInitiation(spiSinglePayment, aspspConsentData).getPayload();
        return paymentMapper.mapToPaymentInitializationResponse(spiPeriodicPaymentResp);
    }

    private PaymentInitialisationResponse createConsentForSinglePaymentAndExtendPaymentResponse(CreateConsentRequest createConsentRequest, PaymentInitialisationResponse response) {
        String pisConsentId = pisConsentService.createPisConsentForSinglePaymentAndGetId(createConsentRequest, response.getPaymentId());
        return StringUtils.isBlank(pisConsentId)
                   ? null
                   : extendPaymentResponseFields(response, pisConsentId);
    }

    private PaymentInitialisationResponse extendPaymentResponseFields(PaymentInitialisationResponse response, String pisConsentId) {
        response.setPisConsentId(pisConsentId);
        return response;
    }
}
