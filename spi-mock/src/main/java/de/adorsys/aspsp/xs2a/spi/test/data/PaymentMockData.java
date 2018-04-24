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

package de.adorsys.aspsp.xs2a.spi.test.data;

import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;

import java.util.*;

public class PaymentMockData {

    private static Map<String, SpiPaymentInitialisationResponse> paymentMap = new HashMap<>();

    public static SpiTransactionStatus getPaymentStatusById(String paymentId, String paymentProduct) {
        return Optional.ofNullable(paymentMap.get(paymentId))
                       .map(SpiPaymentInitialisationResponse::getTransactionStatus)
                       .orElse(null);
    }

    public static SpiPaymentInitialisationResponse createMultiplePayments(List<SpiSinglePayments> payments, String paymentProduct, boolean tppRedirectPreferred) {
        return paymentMap.get(createPaymentInitiation(null, tppRedirectPreferred));
    }

    public static String createPaymentInitiation(SpiSinglePayments spiSinglePayments, boolean tppRedirectPreferred) {
        String paymentId = generatePaymentId();
        SpiPaymentInitialisationResponse response = new SpiPaymentInitialisationResponse();
        response.setTransactionStatus(SpiTransactionStatus.ACCP);
        response.setPaymentId(paymentId);
        response.setScaMethods(null);
        response.setTppRedirectPreferred(tppRedirectPreferred);
        response.setSpiTransactionFees(null);
        response.setPsuMessage(null);
        response.setTppMessages(new String[0]);
        response.setSpiTransactionFeeIndicator(false);
        paymentMap.put(paymentId, response);
        return paymentId;
    }

    private static String generatePaymentId() {
        return UUID.randomUUID().toString();
    }
}

