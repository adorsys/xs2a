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
import de.adorsys.psd2.aspsp.mock.api.common.AspspTransactionStatus;
import de.adorsys.psd2.aspsp.mock.api.payment.AspspPaymentInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static de.adorsys.psd2.aspsp.mock.api.common.AspspTransactionStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommonPaymentControllerTest {
    private static final String PAYMENT_ID = "123456789";
    private static final String WRONG_PAYMENT_ID = "Wrong payment id";
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";

    @InjectMocks
    private CommonPaymentController commonPaymentController;
    @Mock
    private PaymentService paymentService;

    @Before
    public void setUpPaymentServiceMock() {
        AspspPaymentInfo aspspPaymentInfo = getAspspPaymentInfo(RCVD);
        when(paymentService.addPaymentInfo(aspspPaymentInfo))
            .thenReturn(Optional.of(aspspPaymentInfo));
        when(paymentService.getCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(aspspPaymentInfo));
        when(paymentService.getCommonPaymentById(WRONG_PAYMENT_ID))
            .thenReturn(Optional.empty());
        when(paymentService.getPaymentStatusById(PAYMENT_ID))
            .thenReturn(Optional.of(ACCP));
        when(paymentService.getPaymentStatusById(WRONG_PAYMENT_ID))
            .thenReturn(Optional.of(RJCT));
    }

    @Test
    public void createPayment() {
        //Given
        HttpStatus expectedStatus = HttpStatus.CREATED;
        AspspPaymentInfo aspspPaymentInfo = getAspspPaymentInfo(RCVD);

        //When
        ResponseEntity<AspspPaymentInfo> actualResponse =
            commonPaymentController.createPayment(aspspPaymentInfo);

        //Then
        HttpStatus actualStatus = actualResponse.getStatusCode();
        assertThat(actualStatus).isEqualTo(expectedStatus);
        assertThat(actualResponse.getBody()).isNotNull();
        assertThat(actualResponse.getBody().getPaymentId()).isNotNull();
    }

    @Test
    public void getPaymentStatusById_Success() {
        //When
        ResponseEntity actualResponse = commonPaymentController.getPaymentStatusById(PAYMENT_ID);

        //Then
        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualResponse.getBody()).isEqualTo(ACCP);
    }

    @Test
    public void getPaymentStatusById_WrongId() {
        //When
        ResponseEntity actualResponse = commonPaymentController.getPaymentStatusById(WRONG_PAYMENT_ID);

        //Then
        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualResponse.getBody()).isEqualTo(RJCT);
    }

    @Test
    public void getPaymentById_Success() {
        //When
        ResponseEntity actualResponse = commonPaymentController.getPaymentById(PAYMENT_ID);

        //Then
        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualResponse.getBody()).isEqualTo(getAspspPaymentInfo(AspspTransactionStatus.RCVD));
    }

    @Test
    public void getPaymentById_WrongId() {
        //When
        ResponseEntity actualResponse = commonPaymentController.getPaymentById(WRONG_PAYMENT_ID);

        //Then
        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(actualResponse.getBody()).isNull();
    }

    private AspspPaymentInfo getAspspPaymentInfo(AspspTransactionStatus transactionStatus) {
        return new AspspPaymentInfo(
            PAYMENT_ID,
            transactionStatus,
            "sepa-credit-transfers",
            "SINGLE",
            new byte[16],
            ASPSP_ACCOUNT_ID
        );
    }
}
