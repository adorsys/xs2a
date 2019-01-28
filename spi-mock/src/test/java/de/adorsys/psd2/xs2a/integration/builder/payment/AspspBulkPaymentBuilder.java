/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.integration.builder.payment;

import de.adorsys.psd2.aspsp.mock.api.common.AspspTransactionStatus;
import de.adorsys.psd2.aspsp.mock.api.payment.AspspBulkPayment;
import de.adorsys.psd2.aspsp.mock.api.payment.AspspSinglePayment;
import de.adorsys.psd2.xs2a.integration.builder.AspspAccountReferenceBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;

public class AspspBulkPaymentBuilder {
    private static final LocalDate REQUESTED_EXECUTION_DATE = LocalDate.of(2019, 10, 10);

    public static AspspBulkPayment buildAspspBulkPayment(String paymentId, String debIban, Currency currency, HashMap<String, BigDecimal> amountMap) {
        AspspBulkPayment payment = new AspspBulkPayment();

        payment.setPaymentId(paymentId);
        payment.setBatchBookingPreferred(false);
        payment.setDebtorAccount(AspspAccountReferenceBuilder.getReference(debIban, currency));
        payment.setRequestedExecutionDate(REQUESTED_EXECUTION_DATE);
        payment.setPaymentStatus(AspspTransactionStatus.RCVD);
        payment.setPayments(getSinglePaymentList(paymentId, debIban, currency, amountMap));

        return payment;
    }

    private static List<AspspSinglePayment> getSinglePaymentList(String paymentId, String debIban, Currency currency, HashMap<String, BigDecimal> amountMap) {
        List<AspspSinglePayment> payments = new ArrayList<>();
        amountMap.forEach((credIban, amount) -> payments
                                                    .add(new AspspSinglePaymentBuilder().buildAspspSinglePayment(paymentId, debIban, credIban, currency, amount)));
        return payments;
    }
}
