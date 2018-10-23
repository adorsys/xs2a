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
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayment;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.AccountReferenceValidationService;
import de.adorsys.aspsp.xs2a.service.PaymentService;
import de.adorsys.aspsp.xs2a.service.mapper.ResponseMapper;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.aspsp.xs2a.web.mapper.PaymentModelMapperPsd2;
import de.adorsys.aspsp.xs2a.web.mapper.PaymentModelMapperXs2a;
import de.adorsys.psd2.model.PaymentInitiationStatusResponse200Json;
import de.adorsys.psd2.model.PaymentInitiationTarget2WithStatusResponse;
import de.adorsys.psd2.model.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.charset.Charset;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.RESOURCE_UNKNOWN_403;
import static de.adorsys.aspsp.xs2a.exception.MessageCategory.ERROR;
import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

@RunWith(MockitoJUnitRunner.class)
public class PaymentControllerTest {
    private static final String CORRECT_PAYMENT_ID = "33333-444444-55555-55555";
    private static final String WRONG_PAYMENT_ID = "wrong_payment_id";
    private final Charset UTF_8 = Charset.forName("utf-8");
    private static final String REDIRECT_LINK = "http://localhost:4200/consent/confirmation/pis";

    private final String BULK_PAYMENT_DATA = "/json/BulkPaymentTestData.json";
    private final String BULK_PAYMENT_RESP_DATA = "/json/BulkPaymentResponseTestData.json";

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private JsonConverter jsonConverter = new JsonConverter(objectMapper);

    @InjectMocks
    private PaymentController paymentController;

    @Mock
    private PaymentService paymentService;
    @Mock
    private ResponseMapper responseMapper;
    @Mock
    private PaymentModelMapperPsd2 paymentModelMapperPsd2;
    @Mock
    private PaymentModelMapperXs2a paymentModelMapperXs2a;

    @Mock
    private AspspProfileServiceWrapper aspspProfileService;
    @Mock
    private AccountReferenceValidationService referenceValidationService;

    @Before
    public void setUp() {
        when(paymentService.getPaymentById(SINGLE, CORRECT_PAYMENT_ID))
            .thenReturn(ResponseObject.builder().body(getXs2aPayment()).build());
        when(paymentService.getPaymentById(SINGLE, WRONG_PAYMENT_ID))
            .thenReturn(ResponseObject.builder().fail(new MessageError(
                new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_403))).build());
        when(aspspProfileService.getPisRedirectUrlToAspsp())
            .thenReturn(REDIRECT_LINK);
        when(referenceValidationService.validateAccountReferences(any()))
            .thenReturn(ResponseObject.builder().build());
    }

    @Before
    public void setUpPaymentServiceMock() {
        when(paymentService.getPaymentStatusById(PaymentType.SINGLE, CORRECT_PAYMENT_ID))
            .thenReturn(ResponseObject.<Xs2aTransactionStatus>builder().body(Xs2aTransactionStatus.ACCP).build());
        when(paymentService.getPaymentStatusById(PaymentType.SINGLE, WRONG_PAYMENT_ID))
            .thenReturn(ResponseObject.<Xs2aTransactionStatus>builder().fail(new MessageError(
                new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_403))).build());
    }

    @Test
    public void getPaymentById() {
        doReturn(new ResponseEntity<>(getPaymentInitiationResponse(TransactionStatus.ACCP), OK))
            .when(responseMapper).ok(any());

        //Given:
        Object expectedBody = getPaymentInitiationResponse(TransactionStatus.ACCP);

        //When
        ResponseEntity response = paymentController.getPaymentInformation(SINGLE.getValue(), CORRECT_PAYMENT_ID,
            null, null, null, null, null, null,
            null, null, null, null, null,
            null, null, null);

        //Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isEqualToComparingFieldByField(expectedBody);
    }

    @Test
    public void getPaymentById_Failure() {
        when(responseMapper.ok(any()))
            .thenReturn(new ResponseEntity<>(new MessageError(
                new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_403)), FORBIDDEN));

        //When
        ResponseEntity response = paymentController.getPaymentInformation(SINGLE.getValue(), WRONG_PAYMENT_ID,
            null, null, null, null, null, null,
            null, null, null, null, null,
            null, null, null);

        //Then
        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    private PaymentInitiationTarget2WithStatusResponse getPaymentInitiationResponse(TransactionStatus transactionStatus) {
        PaymentInitiationTarget2WithStatusResponse response = new PaymentInitiationTarget2WithStatusResponse();
        response.setTransactionStatus(transactionStatus);
        return response;
    }

    private Object getXs2aPayment() {
        SinglePayment payment = new SinglePayment();
        payment.setEndToEndIdentification(CORRECT_PAYMENT_ID);
        return payment;
    }

    @Test
    public void getTransactionStatusById_Success() {
        doReturn(new ResponseEntity<>(getPaymentInitiationStatus(TransactionStatus.ACCP), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());

        //Given:
        PaymentInitiationStatusResponse200Json expectedBody = getPaymentInitiationStatus(TransactionStatus.ACCP);
        HttpStatus expectedHttpStatus = OK;

        //When:
        ResponseEntity<PaymentInitiationStatusResponse200Json> actualResponse =
            (ResponseEntity<PaymentInitiationStatusResponse200Json>) paymentController.getPaymentInitiationStatus(
                PaymentType.SINGLE.getValue(), CORRECT_PAYMENT_ID, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null);

        //Then:
        HttpStatus actualHttpStatus = actualResponse.getStatusCode();
        assertThat(actualHttpStatus).isEqualTo(expectedHttpStatus);
        assertThat(actualResponse.getBody()).isEqualTo(expectedBody);
    }

    private PaymentInitiationStatusResponse200Json getPaymentInitiationStatus(TransactionStatus transactionStatus) {
        PaymentInitiationStatusResponse200Json response = new PaymentInitiationStatusResponse200Json();
        response.setTransactionStatus(transactionStatus);
        return response;
    }

    @Test
    public void getTransactionStatusById_WrongId() {
        doReturn(new ResponseEntity<>(new MessageError(
            new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_403)), FORBIDDEN)).when(responseMapper).ok(any(), any());

        //Given:
        HttpStatus expectedHttpStatus = FORBIDDEN;

        //When:
        ResponseEntity<PaymentInitiationStatusResponse200Json> actualResponse =
            (ResponseEntity<PaymentInitiationStatusResponse200Json>) paymentController.getPaymentInitiationStatus(
                PaymentType.SINGLE.getValue(), WRONG_PAYMENT_ID, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null);

        //Then:
        assertThat(actualResponse.getStatusCode()).isEqualTo(expectedHttpStatus);
    }
}
