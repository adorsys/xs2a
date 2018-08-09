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
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayment;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.PAYMENT_FAILED;

@Service
@RequiredArgsConstructor
public class OauthScaPaymentService implements ScaPaymentService {
    private final PaymentMapper paymentMapper;
    private final PaymentSpi paymentSpi;

    @Override
    public Optional<PaymentInitialisationResponse> createPeriodicPayment(PeriodicPayment periodicPayment, String paymentProduct) {
        SpiPeriodicPayment spiPeriodicPayment = paymentMapper.mapToSpiPeriodicPayment(periodicPayment);
        return paymentMapper.mapToPaymentInitializationResponse(paymentSpi.initiatePeriodicPayment(spiPeriodicPayment,  new AspspConsentData("zzzzzzzzzzzzzz".getBytes())).getPayload()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
    }

    @Override
    public List<PaymentInitialisationResponse> createBulkPayment(List<SinglePayment> payments, String paymentProduct) {
        List<SpiSinglePayment> spiPayments = paymentMapper.mapToSpiSinglePaymentList(payments);
        List<SpiPaymentInitialisationResponse> spiPaymentInitiations = paymentSpi.createBulkPayments(spiPayments,  new AspspConsentData("zzzzzzzzzzzzzz".getBytes())).getPayload(); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here

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

    @Override
    public Optional<PaymentInitialisationResponse> createSinglePayment(SinglePayment singlePayment, String paymentProduct) {
        SpiSinglePayment spiSinglePayment = paymentMapper.mapToSpiSinglePayment(singlePayment);
        SpiPaymentInitialisationResponse spiPeriodicPaymentResp = paymentSpi.createPaymentInitiation(spiSinglePayment,  new AspspConsentData("zzzzzzzzzzzzzz".getBytes())).getPayload(); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
        return paymentMapper.mapToPaymentInitializationResponse(spiPeriodicPaymentResp);
    }
}
