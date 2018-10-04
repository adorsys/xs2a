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

package de.adorsys.aspsp.aspspmockserver.web;

import de.adorsys.aspsp.aspspmockserver.domain.spi.account.SpiAccountReference;
import de.adorsys.aspsp.aspspmockserver.domain.spi.common.SpiAmount;
import de.adorsys.aspsp.aspspmockserver.domain.spi.payment.SpiBulkPayment;
import de.adorsys.aspsp.aspspmockserver.domain.spi.payment.SpiSinglePayment;
import de.adorsys.aspsp.aspspmockserver.service.PaymentService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static de.adorsys.aspsp.aspspmockserver.domain.spi.common.SpiTransactionStatus.ACCP;
import static de.adorsys.aspsp.aspspmockserver.domain.spi.common.SpiTransactionStatus.RJCT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentControllerTest {
    private static final String PAYMENT_ID = "123456789";
    private static final String WRONG_PAYMENT_ID = "Wrong payment id";

    @InjectMocks
    private PaymentController paymentController;
    @Mock
    private PaymentService paymentService;

    @Before
    public void setUpPaymentServiceMock() {
        SpiSinglePayment response = getSpiSinglePayment();
        response.setPaymentId(PAYMENT_ID);
        List<SpiSinglePayment> responseList = new ArrayList<>();
        responseList.add(response);
        when(paymentService.addPayment(getSpiSinglePayment()))
            .thenReturn(Optional.of(response));
        when(paymentService.addBulkPayments(any()))
            .thenReturn(responseList);
        when(paymentService.isPaymentExist(PAYMENT_ID))
            .thenReturn(true);
        when(paymentService.isPaymentExist(WRONG_PAYMENT_ID))
            .thenReturn(false);
        when(paymentService.getPaymentStatusById(PAYMENT_ID))
            .thenReturn(Optional.of(ACCP));
        when(paymentService.getPaymentStatusById(WRONG_PAYMENT_ID))
            .thenReturn(Optional.of(RJCT));
    }

    @Test
    public void createPayment() {
        //Given
        HttpStatus expectedStatus = HttpStatus.CREATED;

        //When
        ResponseEntity<SpiSinglePayment> actualResponse = paymentController.createPayment(getSpiSinglePayment());

        //Then
        HttpStatus actualStatus = actualResponse.getStatusCode();
        assertThat(actualStatus).isEqualTo(expectedStatus);
        assertThat(actualResponse.getBody()).isNotNull();
        assertThat(actualResponse.getBody().getPaymentId()).isNotNull();
    }

    @Test
    public void createBulkPayments() {
        //Given
        HttpStatus expectedStatus = HttpStatus.CREATED;
        SpiBulkPayment expectedRequest = getSpiBulkPayment();
        //When
        ResponseEntity<List<SpiSinglePayment>> actualResponse = paymentController.createBulkPayments(expectedRequest);

        //Then
        HttpStatus actualStatus = actualResponse.getStatusCode();
        assertThat(actualStatus).isEqualTo(expectedStatus);
        assertThat(actualResponse.getBody()).isNotNull();
        assertThat(actualResponse.getBody().get(0).getPaymentId()).isEqualTo(PAYMENT_ID);
    }

    @Test
    public void getPaymentStatusById_Success() {
        //When
        ResponseEntity actualResponse = paymentController.getPaymentStatusById(PAYMENT_ID);

        //Then
        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualResponse.getBody()).isEqualTo(ACCP);
    }

    @Test
    public void getPaymentStatusById_WrongId() {
        //When
        ResponseEntity actualResponse = paymentController.getPaymentStatusById(WRONG_PAYMENT_ID);

        //Then
        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualResponse.getBody()).isEqualTo(RJCT);
    }

    private SpiSinglePayment getSpiSinglePayment() {
        SpiSinglePayment payment = new SpiSinglePayment();
        SpiAmount amount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(20));
        payment.setInstructedAmount(amount);
        payment.setDebtorAccount(getReference());
        payment.setCreditorName("Merchant123");
        payment.setPurposeCode("BEQNSD");
        payment.setCreditorAgent("sdasd");
        payment.setCreditorAccount(getReference());
        payment.setRemittanceInformationUnstructured("Ref Number Merchant");

        return payment;
    }

    private SpiBulkPayment getSpiBulkPayment() {
        SpiBulkPayment spiBulkPayment = new SpiBulkPayment();
        spiBulkPayment.setBatchBookingPreferred(false);
        spiBulkPayment.setRequestedExecutionDate(LocalDate.now());
        spiBulkPayment.setPayments(Collections.singletonList(getSpiSinglePayment()));
        spiBulkPayment.setDebtorAccount(getReference());

        return spiBulkPayment;
    }

    private SpiAccountReference getReference() {
        return new SpiAccountReference("DE23100120020123456789",
            null,
            null,
            null,
            null,
            Currency.getInstance("EUR"));
    }
}
