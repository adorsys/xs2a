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

import de.adorsys.psd2.aspsp.mock.api.common.AspspAmount;
import de.adorsys.psd2.aspsp.mock.api.common.AspspTransactionStatus;
import de.adorsys.psd2.aspsp.mock.api.payment.AspspSinglePayment;
import de.adorsys.psd2.xs2a.integration.builder.AspspAccountReferenceBuilder;
import de.adorsys.psd2.xs2a.integration.builder.AspspAddressBuilder;

import java.math.BigDecimal;
import java.util.Currency;

public class AspspSinglePaymentBuilder {
    private static final String CREDITOR_AGENT = "AAAADEBBXXX";
    private static final String CREDITOR_NAME = "WBG";
    private static final String END_TO_END_ID = "WBG-123456789";
    private static final String REMITTANCE_INFO_UNSTRUCTURED = "Ref. Number WBG-1222";

    public static AspspSinglePayment buildAspspSinglePayment(String paymentId, String debIban, String credIban, Currency currency, BigDecimal amount) {
        AspspSinglePayment payment = new AspspSinglePayment();

        AspspAmount aspspAmount = new AspspAmount(currency, amount);
        payment.setEndToEndIdentification(END_TO_END_ID);
        payment.setPaymentStatus(AspspTransactionStatus.RCVD);
        payment.setInstructedAmount(aspspAmount);
        payment.setDebtorAccount(AspspAccountReferenceBuilder.getReference(debIban, currency));
        payment.setCreditorName(CREDITOR_NAME);
        payment.setCreditorAgent(CREDITOR_AGENT);
        payment.setCreditorAddress(AspspAddressBuilder.buildAddress());
        payment.setCreditorAccount(AspspAccountReferenceBuilder.getReference(credIban, currency));
        payment.setRemittanceInformationUnstructured(REMITTANCE_INFO_UNSTRUCTURED);
        payment.setPaymentId(paymentId);

        return payment;
    }
}
