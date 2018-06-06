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

import com.google.gson.Gson;
import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.code.BICFI;
import de.adorsys.aspsp.xs2a.domain.code.PurposeCode;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import static de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus.ACCP;
import static de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus.RCVD;
import static de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus.RJCT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PaymentServiceTest {

    private final String PERIODIC_PAYMENT_DATA = "/json/PeriodicPaymentTestData.json";
    private final Charset UTF_8 = Charset.forName("utf-8");
    private static final String PAYMENT_ID = "12345";
    private static final String WRONG_PAYMENT_ID = "0";

    @Autowired
    private PaymentService paymentService;

    @MockBean(name = "paymentSpi")
    private PaymentSpi paymentSpi;
    @MockBean(name = "accountService")
    private AccountService accountService;

    @Before
    public void setUp() throws IOException {
        List<SpiPaymentInitialisationResponse> responseList = new ArrayList<>();
        responseList.add(getSpiPaymentResponse(ACCP));
        when(paymentSpi.initiatePeriodicPayment(any(), any(), anyBoolean()))
            .thenReturn(getSpiPaymentResponse(ACCP));
        when(paymentSpi.createPaymentInitiation(any(), any(), anyBoolean()))
            .thenReturn(getSpiPaymentResponse(RCVD));
        when(paymentSpi.createBulkPayments(any(), any(), anyBoolean()))
            .thenReturn(responseList);
        when(paymentSpi.getPaymentStatusById(PAYMENT_ID, PaymentProduct.SCT.getCode()))
            .thenReturn(ACCP);
        when(paymentSpi.getPaymentStatusById(WRONG_PAYMENT_ID, PaymentProduct.SCT.getCode()))
            .thenReturn(RJCT);
        when(accountService.isAccountExists(any(AccountReference.class)))
            .thenReturn(true);
    }

    @Test
    public void getPaymentStatusById_successesResult() {
        //Given
        TransactionStatus expectedTransactionStatus = TransactionStatus.ACCP;
        PaymentProduct paymentProduct = PaymentProduct.SCT;

        //When:
        ResponseObject<TransactionStatus> actualResponse = paymentService.getPaymentStatusById(PAYMENT_ID, paymentProduct.getCode());

        //Then:
        assertThat(actualResponse.getBody()).isEqualTo(expectedTransactionStatus);
    }

    @Test
    public void getPaymentStatusById_wrongId() {
        //Given
        TransactionStatus expectedTransactionStatus = TransactionStatus.RJCT;
        PaymentProduct paymentProduct = PaymentProduct.SCT;

        //When:
        ResponseObject<TransactionStatus> actualResponse = paymentService.getPaymentStatusById(WRONG_PAYMENT_ID, paymentProduct.getCode());

        //Then:
        assertThat(actualResponse.getBody()).isEqualTo(expectedTransactionStatus);
    }

    @Test
    public void createBulkPayments() {
        // Given
        List<SinglePayments> payments = Collections.singletonList(getPaymentInitiationRequest());
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        boolean tppRedirectPreferred = false;

        //When:
        ResponseObject<List<PaymentInitialisationResponse>> actualResponse = paymentService.createBulkPayments(payments, paymentProduct.getCode(), tppRedirectPreferred);

        //Then:
        assertThat(actualResponse.getBody()).isNotNull();
        assertThat(actualResponse.getBody().get(0).getTransactionStatus()).isEqualTo(TransactionStatus.ACCP);
    }

    @Test
    public void initiatePeriodicPayment() throws IOException {
        //Given:
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        boolean tppRedirectPreferred = false;
        PeriodicPayment periodicPayment = readPeriodicPayment();
        ResponseObject<PaymentInitialisationResponse> expectedResult = readResponseObject();

        //When:
        ResponseObject<PaymentInitialisationResponse> result = paymentService.initiatePeriodicPayment(periodicPayment, paymentProduct.getCode(), tppRedirectPreferred);

        //Than:
        assertThat(result.getError()).isEqualTo(expectedResult.getError());
        assertThat(result.getBody().getTransactionStatus()).isEqualTo(expectedResult.getBody().getTransactionStatus());
    }

    @Test
    public void createPaymentInitiation() {
        // Given
        SinglePayments payment = getPaymentInitiationRequest();
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        boolean tppRedirectPreferred = false;

        //When:
        ResponseObject<PaymentInitialisationResponse> actualResponse = paymentService.createPaymentInitiation(payment, paymentProduct.getCode(), tppRedirectPreferred);

        //Then:
        assertThat(actualResponse.getBody()).isNotNull();
        assertThat(actualResponse.getBody().getTransactionStatus()).isEqualTo(TransactionStatus.RCVD);
    }

    private SpiPaymentInitialisationResponse getSpiPaymentResponse(SpiTransactionStatus status) {
        SpiPaymentInitialisationResponse spiPaymentInitialisationResponse = new SpiPaymentInitialisationResponse();
        spiPaymentInitialisationResponse.setTransactionStatus(status);
        spiPaymentInitialisationResponse.setPaymentId(PAYMENT_ID);
        return spiPaymentInitialisationResponse;
    }

    private SinglePayments getPaymentInitiationRequest() {
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


    private ResponseObject<PaymentInitialisationResponse> readResponseObject() {

        return ResponseObject.<PaymentInitialisationResponse>builder()
            .body(getPaymentInitializationResponse()).build();
    }

    private PeriodicPayment readPeriodicPayment() throws IOException {
        return new Gson().fromJson(IOUtils.resourceToString(PERIODIC_PAYMENT_DATA, UTF_8), PeriodicPayment.class);
    }

    private PaymentInitialisationResponse getPaymentInitializationResponse() {
        PaymentInitialisationResponse resp = new PaymentInitialisationResponse();
        resp.setTransactionStatus(TransactionStatus.ACCP);
        resp.setLinks(new Links());
        return resp;
    }

}
