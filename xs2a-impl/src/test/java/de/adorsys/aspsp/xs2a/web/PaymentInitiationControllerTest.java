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

package de.adorsys.aspsp.xs2a.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.adorsys.aspsp.xs2a.component.JsonConverter;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.service.AspspProfileService;
import de.adorsys.aspsp.xs2a.service.PaymentService;
import de.adorsys.aspsp.xs2a.service.mapper.ResponseMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

@RunWith(MockitoJUnitRunner.class)
public class PaymentInitiationControllerTest {

    private static final String CREATE_PAYMENT_INITIATION_REQUEST_JSON_PATH = "/json/CreatePaymentInitiationRequestTest.json";
    private static final String CREATE_PAYMENT_INITIATION_RESPONSE_JSON_PATH = "/json/CreatePaymentInitiationResponseTest.json";
    private static final Charset UTF_8 = Charset.forName("utf-8");
    private static final String PAYMENT_ID = "12345";
    private static final String WRONG_PAYMENT_ID = "Really wrong id";
    private static final String REDIRECT_LINK = "http://localhost:28080/view/payment/confirmation/";

    @InjectMocks
    private PaymentInitiationController paymentInitiationController;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private JsonConverter jsonConverter = new JsonConverter(objectMapper);

    @Mock
    private PaymentService paymentService;
    @Mock
    private AspspProfileService aspspProfileService;
    @Mock
    ResponseMapper responseMapper;

    @Before
    public void setUpPaymentServiceMock() throws IOException {
        when(paymentService.getPaymentStatusById(PAYMENT_ID, PaymentProduct.SCT.getCode()))
            .thenReturn(ResponseObject.<TransactionStatus>builder().body(TransactionStatus.ACCP).build());
        Map<String, TransactionStatus> paymentStatusResponseWrongId = new HashMap<>();
        paymentStatusResponseWrongId.put("transactionStatus", TransactionStatus.RJCT);
        when(paymentService.getPaymentStatusById(WRONG_PAYMENT_ID, PaymentProduct.SCT.getCode()))
            .thenReturn(ResponseObject.<TransactionStatus>builder().body(TransactionStatus.RJCT).build());
        when(paymentService.createPaymentInitiation(any(), any(), anyBoolean())).thenReturn(readResponseObject());
        when(aspspProfileService.getPisRedirectUrlToAspsp()).thenReturn(REDIRECT_LINK);
    }

    @Test
    public void getTransactionStatusById_Success() {
        when(responseMapper.ok(any())).thenReturn(new ResponseEntity<>(TransactionStatus.ACCP, HttpStatus.OK));
        //Given:
        HttpStatus expectedHttpStatus = OK;
        TransactionStatus expectedTransactionStatus = TransactionStatus.ACCP;

        //When:
        ResponseEntity<TransactionStatus> actualResponse = paymentInitiationController.getPaymentInitiationStatusById(PaymentProduct.SCT.getCode(), PAYMENT_ID);

        //Then:
        HttpStatus actualHttpStatus = actualResponse.getStatusCode();
        assertThat(actualHttpStatus).isEqualTo(expectedHttpStatus);
        assertThat(actualResponse.getBody()).isEqualTo(expectedTransactionStatus);
    }

    @Test
    public void getTransactionStatusById_WrongId() {
        when(responseMapper.ok(any())).thenReturn(new ResponseEntity<>(TransactionStatus.RJCT, HttpStatus.OK));
        //Given:
        HttpStatus expectedHttpStatus = OK;
        TransactionStatus expectedTransactionStatus = TransactionStatus.RJCT;

        //When:
        ResponseEntity<TransactionStatus> actualResponse = paymentInitiationController.getPaymentInitiationStatusById(PaymentProduct.SCT.getCode(), WRONG_PAYMENT_ID);

        //Then:
        HttpStatus actualHttpStatus = actualResponse.getStatusCode();
        assertThat(actualHttpStatus).isEqualTo(expectedHttpStatus);
        assertThat(actualResponse.getBody()).isEqualTo(expectedTransactionStatus);
    }

    @Test
    public void createPaymentInitiation() throws IOException {
        when(responseMapper.created(any())).thenReturn(new ResponseEntity<>(readPaymentInitialisationResponse(), HttpStatus.CREATED));
        //Given
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        boolean tppRedirectPreferred = false;
        SinglePayments payment = readSinglePayments();
        ResponseEntity<PaymentInitialisationResponse> expectedResult = new ResponseEntity<>(readPaymentInitialisationResponse(), HttpStatus.CREATED);

        //When:
        ResponseEntity<PaymentInitialisationResponse> actualResult = paymentInitiationController
                                                                         .createPaymentInitiation(paymentProduct.getCode(), tppRedirectPreferred, payment);

        //Then:
        assertThat(actualResult.getStatusCode()).isEqualTo(expectedResult.getStatusCode());
        assertThat(actualResult.getBody()).isEqualTo(expectedResult.getBody());
    }

    private ResponseObject readResponseObject() throws IOException {
        PaymentInitialisationResponse resp = readPaymentInitialisationResponse();
        resp.setIban("DE371234599999");
        resp.setPisConsentId("932f8184-59dc-4fdb-848e-58b887b3ba02");

        return ResponseObject.builder().body(resp).build();
    }

    private PaymentInitialisationResponse readPaymentInitialisationResponse() throws IOException {
        PaymentInitialisationResponse resp = jsonConverter.toObject(IOUtils.resourceToString(CREATE_PAYMENT_INITIATION_RESPONSE_JSON_PATH, UTF_8), PaymentInitialisationResponse.class).get();
        resp.setIban("DE371234599999");
        resp.setPisConsentId("932f8184-59dc-4fdb-848e-58b887b3ba02");

        return resp;
    }

    private SinglePayments readSinglePayments() throws IOException {
        return jsonConverter.toObject(IOUtils.resourceToString(CREATE_PAYMENT_INITIATION_REQUEST_JSON_PATH, UTF_8), SinglePayments.class).get();
    }

}
