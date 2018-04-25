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

package de.adorsys.aspsp.xs2a.spi.impl;

import de.adorsys.aspsp.xs2a.spi.config.RemoteSpiUrls;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import de.adorsys.aspsp.xs2a.spi.test.data.AccountMockData;
import de.adorsys.aspsp.xs2a.spi.test.data.PaymentMockData;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus.ACCP;
import static de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus.RJCT;
import static org.springframework.http.HttpStatus.CREATED;

@Component
@AllArgsConstructor
public class PaymentSpiImpl implements PaymentSpi {
    private final RestTemplate restTemplate;
    private final RemoteSpiUrls remoteSpiUrls;


    @Override
    public SpiTransactionStatus getPaymentStatusById(String paymentId, String paymentProduct) {
        return restTemplate.getForEntity(remoteSpiUrls.getUrl("getPaymentStatus"), SpiTransactionStatus.class, paymentId).getBody();
    }

    @Override
    public SpiPaymentInitialisationResponse initiatePeriodicPayment(String paymentProduct, boolean tppRedirectPreferred, SpiPeriodicPayment periodicPayment) {
        SpiPaymentInitialisationResponse response = new SpiPaymentInitialisationResponse();
        response.setTransactionStatus(resolveTransactionStatus(periodicPayment));

        return response;
    }

    private SpiTransactionStatus resolveTransactionStatus(SpiPeriodicPayment payment) {
        Map<String, SpiAccountDetails> map = AccountMockData.getAccountsHashMap();
        return Optional.of(map.values().stream()
                           .anyMatch(a -> a.getIban()
                                          .equals(payment.getCreditorAccount().getIban())))
               .map(present -> ACCP)
               .orElse(RJCT);
    }

    public SpiPaymentInitialisationResponse createBulkPayments(List<SpiSinglePayments> payments, String paymentProduct, boolean tppRedirectPreferred) {
        return PaymentMockData.createMultiplePayments(payments, paymentProduct, tppRedirectPreferred);
    }

    @Override
    public SpiPaymentInitialisationResponse createPaymentInitiation(SpiSinglePayments spiSinglePayments, String paymentProduct, boolean tppRedirectPreferred) {
        ResponseEntity<SpiSinglePayments> responseEntity = restTemplate.postForEntity(remoteSpiUrls.getUrl("createPayment"), spiSinglePayments, SpiSinglePayments.class);
        if (responseEntity.getStatusCode() == CREATED) {
            SpiPaymentInitialisationResponse paymentResponse = new SpiPaymentInitialisationResponse();
            paymentResponse.setTransactionStatus(SpiTransactionStatus.RCVD);
            paymentResponse.setPaymentId(responseEntity.getBody().getPaymentId());
            paymentResponse.setTppRedirectPreferred(tppRedirectPreferred);
            return paymentResponse;
        }
        return null;
    }
}
