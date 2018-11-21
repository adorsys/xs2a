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

import de.adorsys.aspsp.aspspmockserver.service.PaymentService;
import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountReference;
import de.adorsys.psd2.aspsp.mock.api.common.AspspAmount;
import de.adorsys.psd2.aspsp.mock.api.common.AspspTransactionStatus;
import de.adorsys.psd2.aspsp.mock.api.payment.AspspBulkPayment;
import de.adorsys.psd2.aspsp.mock.api.payment.AspspPaymentCancellationResponse;
import de.adorsys.psd2.aspsp.mock.api.payment.AspspSinglePayment;
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
import java.util.Collections;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static de.adorsys.psd2.aspsp.mock.api.common.AspspTransactionStatus.*;
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
        AspspSinglePayment response = getAspspSinglePayment();
        response.setPaymentId(PAYMENT_ID);
        AspspBulkPayment bulkResponse = new AspspBulkPayment();
        bulkResponse.setPaymentId(PAYMENT_ID);
        when(paymentService.addPayment(getAspspSinglePayment()))
            .thenReturn(Optional.of(response));
        when(paymentService.addBulkPayments(any()))
            .thenReturn(Optional.of(bulkResponse));
        when(paymentService.isPaymentExist(PAYMENT_ID))
            .thenReturn(true);
        when(paymentService.isPaymentExist(WRONG_PAYMENT_ID))
            .thenReturn(false);
        when(paymentService.getPaymentStatusById(PAYMENT_ID))
            .thenReturn(Optional.of(ACCP));
        when(paymentService.getPaymentStatusById(WRONG_PAYMENT_ID))
            .thenReturn(Optional.of(RJCT));
        when(paymentService.cancelPayment(PAYMENT_ID))
            .thenReturn(Optional.of(getAspspPaymentCancellationResponse(false, CANC)));
        when(paymentService.cancelPayment(WRONG_PAYMENT_ID))
            .thenReturn(Optional.empty());
        when(paymentService.initiatePaymentCancellation(PAYMENT_ID))
            .thenReturn(Optional.of(getAspspPaymentCancellationResponse(true, ACTC)));
        when(paymentService.initiatePaymentCancellation(WRONG_PAYMENT_ID))
            .thenReturn(Optional.empty());
    }

    @Test
    public void createPayment() {
        //Given
        HttpStatus expectedStatus = HttpStatus.CREATED;

        //When
        ResponseEntity<AspspSinglePayment> actualResponse =
            paymentController.createSinglePayment(getAspspSinglePayment());

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
        AspspBulkPayment expectedRequest = getAspspBulkPayment();
        //When
        ResponseEntity<AspspBulkPayment> actualResponse = paymentController.createBulkPayments(expectedRequest);

        //Then
        HttpStatus actualStatus = actualResponse.getStatusCode();
        assertThat(actualStatus).isEqualTo(expectedStatus);
        assertThat(actualResponse.getBody()).isNotNull();
        assertThat(actualResponse.getBody().getPaymentId()).isEqualTo(PAYMENT_ID);
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

    @Test
    public void cancelPayment_Success() {
        //When
        ResponseEntity actualResponse = paymentController.cancelPayment(PAYMENT_ID);

        //Then
        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    public void cancelPayment_Failure_WrongId() {
        //When
        ResponseEntity actualResponse = paymentController.cancelPayment(WRONG_PAYMENT_ID);

        //Then
        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void initiatePaymentCancellation_Success() {
        //When
        ResponseEntity actualResponse = paymentController.initiatePaymentCancellation(PAYMENT_ID);

        //Then
        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(actualResponse.getBody()).isEqualTo(getAspspPaymentCancellationResponse(true, ACTC));
    }

    @Test
    public void initiatePaymentCancellation_Failure_WrongId() {
        //When
        ResponseEntity actualResponse = paymentController.initiatePaymentCancellation(WRONG_PAYMENT_ID);

        //Then
        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(actualResponse.hasBody()).isFalse();
    }

    private AspspSinglePayment getAspspSinglePayment() {
        AspspSinglePayment payment = new AspspSinglePayment();
        AspspAmount amount = new AspspAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(20));
        payment.setInstructedAmount(amount);
        payment.setDebtorAccount(getReference());
        payment.setCreditorName("Merchant123");
        payment.setPurposeCode("BEQNSD");
        payment.setCreditorAgent("sdasd");
        payment.setCreditorAccount(getReference());
        payment.setRemittanceInformationUnstructured("Ref Number Merchant");

        return payment;
    }

    private AspspBulkPayment getAspspBulkPayment() {
        AspspBulkPayment aspspBulkPayment = new AspspBulkPayment();
        aspspBulkPayment.setBatchBookingPreferred(false);
        aspspBulkPayment.setRequestedExecutionDate(LocalDate.now());
        aspspBulkPayment.setPayments(Collections.singletonList(getAspspSinglePayment()));
        aspspBulkPayment.setDebtorAccount(getReference());

        return aspspBulkPayment;
    }

    private AspspAccountReference getReference() {
        return new AspspAccountReference(
            null,
            "DE23100120020123456789",
            null,
            null,
            null,
            null,
            Currency.getInstance("EUR"));
    }

    private AspspPaymentCancellationResponse getAspspPaymentCancellationResponse(boolean authorisationMandated,
                                                                                 AspspTransactionStatus transactionStatus) {
        AspspPaymentCancellationResponse response = new AspspPaymentCancellationResponse();
        response.setCancellationAuthorisationMandated(authorisationMandated);
        response.setTransactionStatus(transactionStatus);
        return response;
    }
}
