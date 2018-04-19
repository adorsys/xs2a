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

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import de.adorsys.aspsp.xs2a.spi.test.data.AccountMockData;
import de.adorsys.aspsp.xs2a.spi.test.data.PaymentMockData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PaymentSpiImpl implements PaymentSpi {

    @Override
    public SpiTransactionStatus getPaymentStatusById(String paymentId, String  paymentProduct) {
        return PaymentMockData.getPaymentStatusById(paymentId, paymentProduct);
    }

    @Override
    public String createPaymentInitiation(SpiSinglePayments spiSinglePayments, boolean tppRedirectPreferred) {
        return PaymentMockData.createPaymentInitiation(spiSinglePayments, tppRedirectPreferred);
    }

    @Override
    public SpiPaymentInitialisationResponse initiatePeriodicPayment(String paymentProduct, boolean tppRedirectPreferred, SpiPeriodicPayment periodicPayment) {
        SpiPaymentInitialisationResponse response = new SpiPaymentInitialisationResponse();
        response.setTransactionStatus(SpiTransactionStatus.valueOf(resolveTransactionStatus(periodicPayment)));

        return response;
    }

    private String resolveTransactionStatus(SpiPeriodicPayment payment) {
        Map<String, SpiAccountDetails> map = AccountMockData.getAccountsHashMap();
        boolean isPresent = map.entrySet().stream()
                                    .anyMatch(a -> a.getValue().getIban()
                                                           .equals(payment.getCreditorAccount().getIban()));
        return isPresent ? "ACCP" : "RJCT";
    }

    public SpiPaymentInitialisationResponse createBulkPayments(List<SpiSinglePayments> payments, String paymentProduct, boolean tppRedirectPreferred) {
        return PaymentMockData.createMultiplePayments(payments, paymentProduct, tppRedirectPreferred);
    }

}
