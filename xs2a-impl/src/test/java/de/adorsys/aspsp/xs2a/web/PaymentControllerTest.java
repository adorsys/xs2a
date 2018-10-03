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
import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.AccountReferenceValidationService;
import de.adorsys.aspsp.xs2a.service.PaymentService;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentModelMapperPsd2;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentModelMapperXs2a;
import de.adorsys.aspsp.xs2a.service.mapper.ResponseMapper;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.model.PaymentInitationRequestResponse201;
import de.adorsys.psd2.model.PaymentInitiationStatusResponse200Json;
import de.adorsys.psd2.model.PaymentInitiationTarget2WithStatusResponse;
import de.adorsys.psd2.model.TransactionStatus;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.RESOURCE_UNKNOWN_403;
import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.PERIODIC;
import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.SINGLE;
import static de.adorsys.aspsp.xs2a.exception.MessageCategory.ERROR;
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

    private static final String CREATE_PAYMENT_INITIATION_REQUEST_JSON_PATH = "/json/CreatePaymentInitiationRequestTest.json";
    private static final String CREATE_PAYMENT_INITIATION_RESPONSE_JSON_PATH = "/json/CreatePaymentInitiationResponseTest.json";

    private final String PERIODIC_PAYMENT_DATA = "/json/PeriodicPaymentTestData.json";
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
    public void setUp() throws IOException {
        when(paymentService.getPaymentById(SINGLE, CORRECT_PAYMENT_ID))
            .thenReturn(ResponseObject.builder().body(getXs2aPayment()).build());
        when(paymentService.getPaymentById(SINGLE, WRONG_PAYMENT_ID))
            .thenReturn(ResponseObject.builder().fail(new MessageError(
                new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_403))).build());
        when(paymentService.createPayment(any(), any()))
            .thenReturn(readResponseObject());

        when(paymentService.createBulkPayments(any(), any(), any()))
            .thenReturn(readListOfXs2aPaymentInitialisationResponses());
        when(aspspProfileService.getPisRedirectUrlToAspsp())
            .thenReturn(REDIRECT_LINK);
        when(referenceValidationService.validateAccountReferences(any()))
            .thenReturn(ResponseObject.builder().build());
    }

    @Before
    public void setUpPaymentServiceMock() throws IOException {
        when(paymentService.getPaymentStatusById(CORRECT_PAYMENT_ID, PaymentType.SINGLE))
            .thenReturn(ResponseObject.<Xs2aTransactionStatus>builder().body(Xs2aTransactionStatus.ACCP).build());
        when(paymentService.getPaymentStatusById(WRONG_PAYMENT_ID, PaymentType.SINGLE))
            .thenReturn(ResponseObject.<Xs2aTransactionStatus>builder().fail(new MessageError(
                new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_403))).build());
        when(paymentService.createPaymentInitiation(any(), any(), any())).thenReturn(readResponseObject());
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
            null );

        //Then:
        assertThat(actualResponse.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test
    public void createPaymentInitiation() throws IOException {
        doReturn(new ResponseEntity<>(readPaymentInitialisationResponse(), HttpStatus.CREATED))
            .when(responseMapper)
            .created(any());
        when(referenceValidationService.validateAccountReferences(readSinglePayment().getAccountReferences()))
            .thenReturn(ResponseObject.builder().build());
        //Given
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        PaymentType paymentType = PaymentType.SINGLE;
        SinglePayment payment = readSinglePayment();
        ResponseEntity<PaymentInitialisationResponse> expectedResult = new ResponseEntity<>(readPaymentInitialisationResponse(), HttpStatus.CREATED);

        //When:
        ResponseEntity<PaymentInitialisationResponse> actualResult =
            (ResponseEntity<PaymentInitialisationResponse>) paymentController.initiatePayment(payment,
            paymentType.getValue(), paymentProduct.getCode(), null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
            null, null, null, null, null,
            null ,null, null, null, null);

        //Then:
        assertThat(actualResult.getStatusCode()).isEqualTo(expectedResult.getStatusCode());
        assertThat(actualResult.getBody()).isEqualTo(expectedResult.getBody());
    }

    private ResponseObject<PaymentInitialisationResponse> readResponseObject() throws IOException {
        PaymentInitialisationResponse resp = readPaymentInitialisationResponse();
        return ResponseObject.<PaymentInitialisationResponse>builder().body(resp).build();
    }

    private PaymentInitialisationResponse readPaymentInitialisationResponse() throws IOException {
        PaymentInitialisationResponse resp = jsonConverter.toObject(IOUtils.resourceToString(
            CREATE_PAYMENT_INITIATION_RESPONSE_JSON_PATH, UTF_8), PaymentInitialisationResponse.class).get();
        resp.setPisConsentId("932f8184-59dc-4fdb-848e-58b887b3ba02");

        return resp;
    }

    private SinglePayment readSinglePayment() throws IOException {
        return jsonConverter.toObject(IOUtils.resourceToString(CREATE_PAYMENT_INITIATION_REQUEST_JSON_PATH, UTF_8),
            SinglePayment.class).get();
    }

    @Test
    public void initiationForStandingOrdersForRecurringOrPeriodicPayments() throws IOException {
        doReturn(new ResponseEntity<>(getPaymentInitializationResponse(), HttpStatus.CREATED))
            .when(responseMapper).created(any());
        when(referenceValidationService.validateAccountReferences(readSinglePayment().getAccountReferences()))
            .thenReturn(ResponseObject.builder().build());
        //Given
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        PeriodicPayment periodicPayment = readPeriodicPayment();
        ResponseEntity<PaymentInitialisationResponse> expectedResult = new ResponseEntity<>(
            getPaymentInitializationResponse(), HttpStatus.CREATED);

        //When:
        ResponseEntity<PaymentInitialisationResponse> result =
            (ResponseEntity<PaymentInitialisationResponse>) paymentController.initiatePayment(periodicPayment,
            PERIODIC.getValue(), paymentProduct.getCode(), null,null, null, null,
            null, null, null, null, null,
                null ,null, null, null,
                null, null, null, null,
                null, null, null, null, null,
                null);

        //Then:
        assertThat(result.getStatusCode()).isEqualTo(expectedResult.getStatusCode());
        assertThat(result.getBody().getTransactionStatus().name()).isEqualTo(expectedResult.getBody().getTransactionStatus().name());
    }

    private PeriodicPayment readPeriodicPayment() throws IOException {
        return jsonConverter.toObject(IOUtils.resourceToString(PERIODIC_PAYMENT_DATA, UTF_8),
            PeriodicPayment.class).get();
    }

    private PaymentInitialisationResponse getPaymentInitializationResponse() {
        PaymentInitialisationResponse resp = new PaymentInitialisationResponse();
        resp.setTransactionStatus(Xs2aTransactionStatus.ACCP);
        resp.setPaymentId("352397d6-a9f2-4914-8549-d127c02660ba");
        resp.setPisConsentId("f33e9b14-56b8-4f3b-b2fd-87884a4a24b9");
        resp.setLinks(new Links());
        return resp;
    }

    @Test
    public void createBulkPaymentInitiation() throws IOException {
        when(responseMapper.created(any()))
            .thenReturn(new ResponseEntity<>(readListOfPaymentInitialisationResponses(), HttpStatus.CREATED));
        //Given
        List<SinglePayment> payments = readBulkPayments();
        ResponseEntity<List<PaymentInitationRequestResponse201>> expectedResult = new ResponseEntity<>(
            readListOfPaymentInitialisationResponses(), HttpStatus.CREATED);

        //When:
        ResponseEntity<List<PaymentInitationRequestResponse201>> actualResult =
            (ResponseEntity<List<PaymentInitationRequestResponse201>>) paymentController.initiatePayment(payments,
                PaymentType.BULK.getValue(), PaymentProduct.SCT.getCode(), null,null, null,
                null, null, null, null, null,
                null, null ,null, null, null,
            null, null, null, null, null,
            null, null, null, null, null);

        //Then:
        assertThat(actualResult.getStatusCode()).isEqualTo(expectedResult.getStatusCode());
        assertThat(actualResult.getBody()).isEqualTo(expectedResult.getBody());
    }

    private ResponseObject<List<PaymentInitialisationResponse>> readListOfXs2aPaymentInitialisationResponses() throws IOException {
        return ResponseObject.<List<PaymentInitialisationResponse>>builder()
                   .body(readPaymentInitialisationXs2aResponse()).build();
    }

    private List<PaymentInitialisationResponse> readPaymentInitialisationXs2aResponse() throws IOException {
        PaymentInitialisationResponse response = jsonConverter.toObject(IOUtils.resourceToString(
            BULK_PAYMENT_RESP_DATA, UTF_8), PaymentInitialisationResponse.class).get();
        List<PaymentInitialisationResponse> responseList = new ArrayList<>();
        responseList.add(response);

        return responseList;
    }

    private List<PaymentInitationRequestResponse201> readListOfPaymentInitialisationResponses() throws IOException {
        PaymentInitationRequestResponse201 response = jsonConverter.toObject(IOUtils.resourceToString(
            BULK_PAYMENT_RESP_DATA, UTF_8), PaymentInitationRequestResponse201.class).get();
        List<PaymentInitationRequestResponse201> responseList = new ArrayList<>();
        responseList.add(response);

        return responseList;
    }

    private List<SinglePayment> readBulkPayments() throws IOException {
        SinglePayment[] payments = jsonConverter.toObject(IOUtils.resourceToString(BULK_PAYMENT_DATA, UTF_8),
            SinglePayment[].class).get();
        return Arrays.asList(payments);
    }
}
