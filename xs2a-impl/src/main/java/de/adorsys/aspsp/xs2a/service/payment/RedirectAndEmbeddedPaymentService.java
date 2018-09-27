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

import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.SpiResponse;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedirectAndEmbeddedPaymentService implements ScaPaymentService {
    private final PaymentSpi paymentSpi;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentInitialisationResponse createSinglePayment(SinglePayment payment, TppInfo tppInfo, String paymentProduct) {
        AspspConsentData aspspConsentData = new AspspConsentData();
        SpiResponse<SpiPaymentInitialisationResponse> response = paymentSpi.createPaymentInitiation(paymentMapper.mapToSpiSinglePayment(payment), aspspConsentData);
        return paymentMapper.mapToPaymentInitializationResponse(response.getPayload(), response.getAspspConsentData());
    }

    @Override
    public PaymentInitialisationResponse createPeriodicPayment(PeriodicPayment payment, TppInfo tppInfo, String paymentProduct) {
        AspspConsentData aspspConsentData = new AspspConsentData();
        SpiResponse<SpiPaymentInitialisationResponse> response = paymentSpi.initiatePeriodicPayment(paymentMapper.mapToSpiPeriodicPayment(payment), aspspConsentData);
        return paymentMapper.mapToPaymentInitializationResponse(response.getPayload(), response.getAspspConsentData());
    }

    @Override
    public List<PaymentInitialisationResponse> createBulkPayment(BulkPayment bulkPayment, TppInfo tppInfo, String paymentProduct) {
        SpiResponse<List<SpiPaymentInitialisationResponse>> response = paymentSpi.createBulkPayments(paymentMapper.mapToSpiBulkPayment(bulkPayment), new AspspConsentData());
        final AspspConsentData aspspConsentData = response.getAspspConsentData();
        return response.getPayload()
                   .stream()
                   .map((SpiPaymentInitialisationResponse resp) ->
                            paymentMapper.mapToPaymentInitializationResponse(resp, aspspConsentData))
                   .collect(Collectors.toList());
    }
}
