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

package de.adorsys.psd2.xs2a.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.adorsys.psd2.model.PaymentInitiationCancelResponse200202;
import de.adorsys.psd2.model.PaymentInitiationStatusResponse200Json;
import de.adorsys.psd2.model.PaymentInitiationTarget2WithStatusResponse;
import de.adorsys.psd2.xs2a.component.JsonConverter;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.AccountReferenceValidationService;
import de.adorsys.psd2.xs2a.service.ConsentService;
import de.adorsys.psd2.xs2a.service.PaymentService;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.mapper.ConsentModelMapper;
import de.adorsys.psd2.xs2a.web.mapper.PaymentModelMapperPsd2;
import de.adorsys.psd2.xs2a.web.mapper.PaymentModelMapperXs2a;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.charset.Charset;
import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;
import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.RESOURCE_UNKNOWN_403;
import static de.adorsys.psd2.xs2a.exception.MessageCategory.ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

@RunWith(MockitoJUnitRunner.class)
public class PaymentControllerTest {
    private static final String CORRECT_PAYMENT_ID = "33333-444444-55555-55555";
    private static final String WRONG_PAYMENT_ID = "wrong_payment_id";
    private static final Charset UTF_8 = Charset.forName("utf-8");
    private static final String REDIRECT_LINK = "http://localhost:4200/consent/confirmation/pis";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(null, null, null, null);
    private static final UUID REQUEST_ID = UUID.fromString("ddd36e05-d67a-4830-93ad-9462f71ae1e6");
    private static final String BULK_PAYMENT_DATA = "/json/BulkPaymentTestData.json";
    private static final String BULK_PAYMENT_RESP_DATA = "/json/BulkPaymentResponseTestData.json";
    private static final String PAYMENT_CANCELLATION_ID = "42af2f4a-0d9f-4a7f-8677-8acda5e718f0";
    private static final String AUTHORISATION_ID = "3e96e9e0-9974-42aa-beb8-003e91416652";

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
    @Mock
    private ConsentService consentService;
    @Mock
    private ConsentModelMapper consentModelMapper;

    @Before
    public void setUp() {
        when(paymentService.getPaymentById(eq(SINGLE), eq(CORRECT_PAYMENT_ID)))
            .thenReturn(ResponseObject.builder().body(getXs2aPayment()).build());
        when(paymentService.getPaymentById(eq(SINGLE), eq(WRONG_PAYMENT_ID)))
            .thenReturn(ResponseObject.builder().fail(new MessageError(
                new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_403))).build());
        when(aspspProfileService.getPisRedirectUrlToAspsp())
            .thenReturn(REDIRECT_LINK);
        when(referenceValidationService.validateAccountReferences(any()))
            .thenReturn(ResponseObject.builder().build());
    }

    @Before
    public void setUpPaymentServiceMock() {
        when(paymentService.getPaymentStatusById(eq(PaymentType.SINGLE), eq(CORRECT_PAYMENT_ID)))
            .thenReturn(ResponseObject.<TransactionStatus>builder().body(TransactionStatus.ACCP).build());
        when(paymentService.getPaymentStatusById(eq(PaymentType.SINGLE), eq(WRONG_PAYMENT_ID)))
            .thenReturn(ResponseObject.<TransactionStatus>builder().fail(new MessageError(
                new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_403))).build());
    }

    @Test
    public void getPaymentById() {
        doReturn(new ResponseEntity<>(getPaymentInitiationResponse(de.adorsys.psd2.model.TransactionStatus.ACCP), OK))
            .when(responseMapper).ok(any());

        //Given:
        Object expectedBody = getPaymentInitiationResponse(de.adorsys.psd2.model.TransactionStatus.ACCP);

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

    private PaymentInitiationTarget2WithStatusResponse getPaymentInitiationResponse(de.adorsys.psd2.model.TransactionStatus transactionStatus) {
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
        doReturn(new ResponseEntity<>(getPaymentInitiationStatus(de.adorsys.psd2.model.TransactionStatus.ACCP), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());

        //Given:
        PaymentInitiationStatusResponse200Json expectedBody = getPaymentInitiationStatus(de.adorsys.psd2.model.TransactionStatus.ACCP);
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

    private PaymentInitiationStatusResponse200Json getPaymentInitiationStatus(de.adorsys.psd2.model.TransactionStatus transactionStatus) {
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

    @Test
    public void cancelPayment_WithoutAuthorisation_Success() {
        when(responseMapper.ok(any()))
            .thenReturn(new ResponseEntity<>(getPaymentInitiationCancelResponse200202(de.adorsys.psd2.model.TransactionStatus.CANC), HttpStatus.OK));
        when(paymentService.cancelPayment(any(), any())).thenReturn(getCancelPaymentResponseObject(false));

        // Given
        PaymentType paymentType = PaymentType.SINGLE;
        ResponseEntity<PaymentInitiationCancelResponse200202> expectedResult = new ResponseEntity<>(getPaymentInitiationCancelResponse200202(de.adorsys.psd2.model.TransactionStatus.CANC), HttpStatus.OK);

        // When
        ResponseEntity<PaymentInitiationCancelResponse200202> actualResult = (ResponseEntity<PaymentInitiationCancelResponse200202>) paymentController.cancelPayment(paymentType.getValue(),
                                                                                                                                                                     CORRECT_PAYMENT_ID, null, null, null,
                                                                                                                                                                     null, null, null, null, null,
                                                                                                                                                                     null, null,
                                                                                                                                                                     null, null, null, null);

        // Then:
        assertThat(actualResult.getStatusCode()).isEqualTo(expectedResult.getStatusCode());
        assertThat(actualResult.getBody()).isEqualTo(expectedResult.getBody());
    }

    @Test
    public void cancelPayment_WithAuthorisation_Success() {
        when(responseMapper.accepted(any()))
            .thenReturn(new ResponseEntity<>(getPaymentInitiationCancelResponse200202(de.adorsys.psd2.model.TransactionStatus.ACTC), HttpStatus.ACCEPTED));
        when(paymentService.cancelPayment(any(), any())).thenReturn(getCancelPaymentResponseObject(true));

        // Given
        PaymentType paymentType = PaymentType.SINGLE;
        ResponseEntity<PaymentInitiationCancelResponse200202> expectedResult = new ResponseEntity<>(getPaymentInitiationCancelResponse200202(de.adorsys.psd2.model.TransactionStatus.ACTC), HttpStatus.ACCEPTED);

        // When
        ResponseEntity<PaymentInitiationCancelResponse200202> actualResult = (ResponseEntity<PaymentInitiationCancelResponse200202>) paymentController.cancelPayment(paymentType.getValue(),
                                                                                                                                                                     CORRECT_PAYMENT_ID, null, null, null,
                                                                                                                                                                     null, null, null, null, null,
                                                                                                                                                                     null, null,
                                                                                                                                                                     null, null, null, null);

        // Then:
        assertThat(actualResult.getStatusCode()).isEqualTo(expectedResult.getStatusCode());
        assertThat(actualResult.getBody()).isEqualTo(expectedResult.getBody());
    }

    private ResponseObject<CancelPaymentResponse> getCancelPaymentResponseObject(boolean startAuthorisationRequired) {
        CancelPaymentResponse response = new CancelPaymentResponse();
        response.setStartAuthorisationRequired(startAuthorisationRequired);
        return ResponseObject.<CancelPaymentResponse>builder().body(response).build();
    }

    private PaymentInitiationCancelResponse200202 getPaymentInitiationCancelResponse200202(de.adorsys.psd2.model.TransactionStatus transactionStatus) {
        PaymentInitiationCancelResponse200202 response = new PaymentInitiationCancelResponse200202();
        response.setTransactionStatus(transactionStatus);
        return response;
    }
}
