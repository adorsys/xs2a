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
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus;
import de.adorsys.aspsp.xs2a.domain.consent.CreatePisConsentData;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayment;
import de.adorsys.aspsp.xs2a.domain.pis.TppInfo;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aPisConsentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiPisConsentRequest;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmbeddedScaPaymentService implements ScaPaymentService {
    private final ConsentSpi consentSpi;
    private final Xs2aPisConsentMapper pisConsentMapper;

    @Override
    public PaymentInitialisationResponse createPeriodicPayment(PeriodicPayment payment, TppInfo tppInfo, String paymentProduct) {
        AspspConsentData aspspConsentData = new AspspConsentData(); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
        CreatePisConsentData pisConsentData = new CreatePisConsentData(payment, tppInfo, paymentProduct, aspspConsentData);
        return createConsentForPeriodicPaymentAndExtendPaymentResponse(pisConsentData, new PaymentInitialisationResponse());
    }

    @Override
    public List<PaymentInitialisationResponse> createBulkPayment(List<SinglePayment> payments, TppInfo tppInfo, String paymentProduct) {
        AspspConsentData aspspConsentData = new AspspConsentData(); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
        Map<SinglePayment, PaymentInitialisationResponse> paymentMap = new HashMap<>();
        for (SinglePayment payment : payments) {
            paymentMap.put(payment, new PaymentInitialisationResponse());
        }
        CreatePisConsentData pisConsentData = new CreatePisConsentData(paymentMap, tppInfo, paymentProduct, aspspConsentData);
        return createConsentForBulkPaymentAndExtendPaymentResponses(pisConsentData);
    }

    @Override
    public PaymentInitialisationResponse createSinglePayment(SinglePayment payment, TppInfo tppInfo, String paymentProduct) {
        AspspConsentData aspspConsentData = new AspspConsentData(); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
        CreatePisConsentData pisConsentData = new CreatePisConsentData(payment, tppInfo, paymentProduct, aspspConsentData);
        return createConsentForSinglePaymentAndExtendPaymentResponse(pisConsentData, new PaymentInitialisationResponse());
    }

    private PaymentInitialisationResponse createConsentForSinglePaymentAndExtendPaymentResponse(CreatePisConsentData createPisConsentData, PaymentInitialisationResponse response) {
        SpiPisConsentRequest request = pisConsentMapper.mapToSpiPisConsentRequestForSinglePayment(createPisConsentData, response.getPaymentId());
        CreatePisConsentResponse cmsResponse = consentSpi.createPisConsentForSinglePaymentAndGetId(request);

        return extendPaymentResponseFields(response, cmsResponse);
    }

    private PaymentInitialisationResponse createConsentForPeriodicPaymentAndExtendPaymentResponse(CreatePisConsentData createPisConsentData, PaymentInitialisationResponse response) {
        SpiPisConsentRequest request = pisConsentMapper.mapToSpiPisConsentRequestForPeriodicPayment(createPisConsentData, response.getPaymentId());
        CreatePisConsentResponse cmsResponse = consentSpi.createPisConsentForPeriodicPaymentAndGetId(request);
        return extendPaymentResponseFields(response, cmsResponse);
    }

    private List<PaymentInitialisationResponse> createConsentForBulkPaymentAndExtendPaymentResponses(CreatePisConsentData createPisConsentData) {
        SpiPisConsentRequest request = pisConsentMapper.mapToSpiPisConsentRequestForBulkPayment(createPisConsentData);
        CreatePisConsentResponse cmsResponse = consentSpi.createPisConsentForBulkPaymentAndGetId(request);

        List<PaymentInitialisationResponse> responses = Lists.newArrayList(createPisConsentData.getPaymentIdentifierMap().values());
        return responses.stream()
                   .map(resp -> extendPaymentResponseFields(resp, cmsResponse))
                   .collect(Collectors.toList());
    }

    private PaymentInitialisationResponse extendPaymentResponseFields(PaymentInitialisationResponse response, CreatePisConsentResponse cmsResponse) {
        return Optional.ofNullable(cmsResponse)
                   .filter(c -> StringUtils.isNotBlank(c.getConsentId()) && StringUtils.isNotBlank(c.getPaymentId()))
                   .map(c -> {
                       response.setPaymentId(c.getPaymentId());
                       response.setTransactionStatus(Xs2aTransactionStatus.RCVD);
                       response.setPisConsentId(c.getConsentId());
                       return response;
                   })
                   .orElse(response);
    }
}
