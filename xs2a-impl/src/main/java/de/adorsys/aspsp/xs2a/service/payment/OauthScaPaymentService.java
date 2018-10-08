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
import de.adorsys.aspsp.xs2a.domain.TppInfo;
import de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.BulkPayment;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayment;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.PAYMENT_FAILED;

@Service
@RequiredArgsConstructor
public class OauthScaPaymentService implements ScaPaymentService {
    private final PaymentMapper paymentMapper;
    private final PaymentSpi paymentSpi;

    @Override
    public PaymentInitialisationResponse createPeriodicPayment(PeriodicPayment periodicPayment, TppInfo tppInfo, String paymentProduct) {
        SpiPeriodicPayment spiPeriodicPayment = paymentMapper.mapToSpiPeriodicPayment(periodicPayment);
        //TODO put get consent call here https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332
        return paymentMapper.mapToPaymentInitializationResponse(paymentSpi.initiatePeriodicPayment(spiPeriodicPayment, new AspspConsentData()).getPayload(), new AspspConsentData());//TODO put real data call here https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332
        //TODO put update consent call here https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332
    }

    @Override
    public List<PaymentInitialisationResponse> createBulkPayment(BulkPayment bulkPayment, TppInfo tppInfo, String paymentProduct) {
        SpiBulkPayment spiBulkPayment = paymentMapper.mapToSpiBulkPayment(bulkPayment);
        //TODO put get consent call here https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332
        List<SpiPaymentInitialisationResponse> spiPaymentInitiations = paymentSpi.createBulkPayments(spiBulkPayment, new AspspConsentData()).getPayload();
        //TODO put update consent call here https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332
        return spiPaymentInitiations.stream()
                   .map((SpiPaymentInitialisationResponse response) -> paymentMapper.mapToPaymentInitializationResponse(
                       response, new AspspConsentData()))//TODO put real data call here https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332
                   .peek(resp -> {
                       if (StringUtils.isBlank(resp.getPaymentId()) || resp.getTransactionStatus() == Xs2aTransactionStatus.RJCT) {
                           resp.setTppMessages(new MessageErrorCode[]{PAYMENT_FAILED});
                           resp.setTransactionStatus(Xs2aTransactionStatus.RJCT);
                       }
                   })
                   .collect(Collectors.toList());
    }

    @Override
    public PaymentInitialisationResponse createSinglePayment(SinglePayment singlePayment, TppInfo tppInfo, String paymentProduct) {
        SpiSinglePayment spiSinglePayment = paymentMapper.mapToSpiSinglePayment(singlePayment);
        //TODO put get consent call here https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332
        SpiPaymentInitialisationResponse spiPeriodicPaymentResp = paymentSpi.createPaymentInitiation(spiSinglePayment, new AspspConsentData()).getPayload();
        //TODO put update consent call here https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332
        return paymentMapper.mapToPaymentInitializationResponse(spiPeriodicPaymentResp, new AspspConsentData());//TODO put real data call here https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332
    }
}
