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

package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.code.BICFI;
import de.adorsys.aspsp.xs2a.domain.code.PurposeCode;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PaymentServiceTest {
    @Autowired
    private PaymentService paymentService;

    @Test
    public void getPaymentStatusById_successesResult() {
        //Given:
        boolean tppRedirectPreferred = false;
        SinglePayments expectedRequest = getCreatePaymentInitiationRequestTest();
        String validAccountConsentsId = paymentService.createPaymentInitiationAndReturnId(expectedRequest, tppRedirectPreferred);
        TransactionStatus expectedStatus = TransactionStatus.ACCP;

        //When:
        ResponseObject<Map<String, TransactionStatus>> actualStatus = paymentService.getPaymentStatusById(validAccountConsentsId, PaymentProduct.SCT);

        //Then:
        assertThat(actualStatus.getBody()).isNotNull();
        assertThat(actualStatus.getBody().get("transactionStatus")).isEqualTo(expectedStatus);
    }

    @Test
    public void getPaymentStatusById_wrongId() {
        //Given:
        String wrongId = "111111";

        //When:
        ResponseObject<Map<String, TransactionStatus>> actualStatus = paymentService.getPaymentStatusById(wrongId, PaymentProduct.SCT);

        //Then:
        assertThat(actualStatus.getBody()).isNull();
        assertThat(actualStatus.getError().getTppMessage().getCode()).isEqualTo(MessageCode.PRODUCT_UNKNOWN);
    }

    @Test
    public void createBulkPayments() {
        // Given
        List<SinglePayments> payments = Collections.singletonList(getCreatePaymentInitiationRequestTest());
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        boolean tppRedirectPreferred = false;

        //When:
        ResponseObject<PaymentInitialisationResponse> actualResponse = paymentService.createBulkPayments(payments, paymentProduct, tppRedirectPreferred);

        //Then:
        assertThat(actualResponse.getBody()).isNotNull();
        assertThat(actualResponse.getBody().getTransactionStatus()).isEqualTo(TransactionStatus.ACCP);
    }

    private SinglePayments getCreatePaymentInitiationRequestTest() {
        Amount amount = new Amount();
        amount.setCurrency(Currency.getInstance("EUR"));
        AccountReference accountReference = new AccountReference();
        accountReference.setIban("DE23100120020123456789");
        amount.setContent("123.40");
        BICFI bicfi = new BICFI();
        bicfi.setCode("vnldkvn");
        SinglePayments singlePayments = new SinglePayments();
        singlePayments.setInstructedAmount(amount);
        singlePayments.setDebtorAccount(accountReference);
        singlePayments.setCreditorName("Merchant123");
        singlePayments.setPurposeCode(new PurposeCode("BEQNSD"));
        singlePayments.setCreditorAgent(bicfi);
        singlePayments.setCreditorAccount(accountReference);
        singlePayments.setPurposeCode(new PurposeCode("BCENECEQ"));
        singlePayments.setRemittanceInformationUnstructured("Ref Number Merchant");

        return singlePayments;
    }
}
