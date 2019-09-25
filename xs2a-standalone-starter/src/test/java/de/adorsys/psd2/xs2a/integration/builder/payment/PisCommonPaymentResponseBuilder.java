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

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.integration.builder.PsuIdDataBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

public class PisCommonPaymentResponseBuilder {
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.RCVD;
    private static final PaymentType PAYMENT_TYPE = PaymentType.SINGLE;
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_CURRENCY_CODE = "EUR";
    private static final BigDecimal PAYMENT_AMOUNT = BigDecimal.TEN;

    public static PisCommonPaymentResponse buildPisCommonPaymentResponseWithAuthorisation(Authorisation authorisation) {
        PisCommonPaymentResponse response = buildPisCommonPaymentResponse();
        response.setAuthorisations(Collections.singletonList(authorisation));
        return response;
    }

    public static PisCommonPaymentResponse buildPisCommonPaymentResponse() {
        PisCommonPaymentResponse commonPaymentResponse = new PisCommonPaymentResponse();
        commonPaymentResponse.setTransactionStatus(TRANSACTION_STATUS);
        commonPaymentResponse.setPaymentType(PAYMENT_TYPE);
        commonPaymentResponse.setPaymentProduct(PAYMENT_PRODUCT);
        commonPaymentResponse.setTppInfo(TppInfoBuilder.buildTppInfo());
        commonPaymentResponse.setPsuData(Collections.singletonList(PsuIdDataBuilder.buildPsuIdData()));
        return commonPaymentResponse;
    }

    public static PisCommonPaymentResponse buildPisCommonPaymentResponse(List<Authorisation> authorisationList) {
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse();
        commonPaymentResponse.setAuthorisations(authorisationList);
        return commonPaymentResponse;
    }

    public static PisCommonPaymentResponse buildPisCommonPaymentResponseWithPayment() {
        PisPayment pisPayment = new PisPayment();
        pisPayment.setCurrency(Currency.getInstance(PAYMENT_CURRENCY_CODE));
        pisPayment.setAmount(PAYMENT_AMOUNT);
        pisPayment.setDebtorAccount(new AccountReference());
        pisPayment.setCreditorAccount(new AccountReference());

        PisCommonPaymentResponse pisCommonPaymentResponse = buildPisCommonPaymentResponse();
        pisCommonPaymentResponse.setPayments(Collections.singletonList(pisPayment));
        return pisCommonPaymentResponse;
    }
}
