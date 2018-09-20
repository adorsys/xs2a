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
import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentType;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayment;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.AccountReferenceValidationService;
import de.adorsys.aspsp.xs2a.service.PaymentService;
import de.adorsys.aspsp.xs2a.service.mapper.ResponseMapper;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileServiceWrapper;
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
import java.util.Base64;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.RESOURCE_UNKNOWN_403;
import static de.adorsys.aspsp.xs2a.exception.MessageCategory.ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

@RunWith(MockitoJUnitRunner.class)
public class PaymentInitiationControllerTest {
    private static final String CREATE_PAYMENT_INITIATION_REQUEST_JSON_PATH = "/json/CreatePaymentInitiationRequestTest.json";
    private static final String CREATE_PAYMENT_INITIATION_RESPONSE_JSON_PATH = "/json/CreatePaymentInitiationResponseTest.json";
    private static final Charset UTF_8 = Charset.forName("utf-8");
    private static final String PAYMENT_ID = "12345";
    private static final String WRONG_PAYMENT_ID = "Really wrong id";
    private static final String REDIRECT_LINK = "http://localhost:4200/consent/confirmation/pis";

    @InjectMocks
    private PaymentInitiationController paymentInitiationController;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private JsonConverter jsonConverter = new JsonConverter(objectMapper);

    @Mock
    private PaymentService paymentService;
    @Mock
    private AspspProfileServiceWrapper aspspProfileService;
    @Mock
    private ResponseMapper responseMapper;
    @Mock
    private AccountReferenceValidationService referenceValidationService;

    @Before
    public void setUpPaymentServiceMock() throws IOException {
        when(paymentService.getPaymentStatusById(PAYMENT_ID, PaymentType.SINGLE))
            .thenReturn(ResponseObject.<Xs2aTransactionStatus>builder().body(Xs2aTransactionStatus.ACCP).build());
        when(paymentService.getPaymentStatusById(WRONG_PAYMENT_ID, PaymentType.SINGLE))
            .thenReturn(ResponseObject.<Xs2aTransactionStatus>builder().fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_403))).build());
        when(paymentService.createPaymentInitiation(any(), any(), any())).thenReturn(readResponseObject());
    }

    @Test
    public void getTransactionStatusById_Success() {
        when(responseMapper.ok(any())).thenReturn(new ResponseEntity<>(Xs2aTransactionStatus.ACCP, HttpStatus.OK));
        //Given:
        HttpStatus expectedHttpStatus = OK;
        Xs2aTransactionStatus expectedTransactionStatus = Xs2aTransactionStatus.ACCP;

        //When:
        ResponseEntity<TransactionStatusResponse> actualResponse = paymentInitiationController.getPaymentInitiationStatusById(PaymentProduct.SCT.getCode(), PAYMENT_ID);

        //Then:
        HttpStatus actualHttpStatus = actualResponse.getStatusCode();
        assertThat(actualHttpStatus).isEqualTo(expectedHttpStatus);
        assertThat(actualResponse.getBody()).isEqualTo(expectedTransactionStatus);
    }

    @Test
    public void getTransactionStatusById_WrongId() {
        when(responseMapper.ok(any()))
            .thenReturn(new ResponseEntity<>(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_403)), HttpStatus.FORBIDDEN));
        //Given:
        HttpStatus expectedHttpStatus = FORBIDDEN;

        //When:
        ResponseEntity<TransactionStatusResponse> actualResponse = paymentInitiationController.getPaymentInitiationStatusById(PaymentProduct.SCT.getCode(), WRONG_PAYMENT_ID);

        //Then:
        assertThat(actualResponse.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test
    public void createPaymentInitiation() throws IOException {
        when(responseMapper.created(any())).thenReturn(new ResponseEntity<>(readPaymentInitialisationResponse(), HttpStatus.CREATED));
        when(referenceValidationService.validateAccountReferences(readSinglePayment().getAccountReferences())).thenReturn(ResponseObject.builder().build());
        //Given
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        SinglePayment payment = readSinglePayment();
        ResponseEntity<PaymentInitialisationResponse> expectedResult = new ResponseEntity<>(readPaymentInitialisationResponse(), HttpStatus.CREATED);

        //When:
        ResponseEntity<PaymentInitialisationResponse> actualResult = paymentInitiationController
                                                                         .createPaymentInitiation(paymentProduct.getCode(), "", payment);

        //Then:
        assertThat(actualResult.getStatusCode()).isEqualTo(expectedResult.getStatusCode());
        assertThat(actualResult.getBody()).isEqualTo(expectedResult.getBody());
    }

    private ResponseObject<PaymentInitialisationResponse> readResponseObject() throws IOException {
        PaymentInitialisationResponse resp = readPaymentInitialisationResponse();
        return ResponseObject.<PaymentInitialisationResponse>builder().body(resp).build();
    }

    private PaymentInitialisationResponse readPaymentInitialisationResponse() throws IOException {
        PaymentInitialisationResponse resp = jsonConverter.toObject(IOUtils.resourceToString(CREATE_PAYMENT_INITIATION_RESPONSE_JSON_PATH, UTF_8), PaymentInitialisationResponse.class).get();
        resp.setPisConsentId("932f8184-59dc-4fdb-848e-58b887b3ba02");
        Links links = new Links();
        String encodedPaymentId = Base64.getEncoder().encodeToString(resp.getPaymentId().getBytes());
        links.setScaRedirect(REDIRECT_LINK + "/" + resp.getPisConsentId() + "/" + encodedPaymentId);

        return resp;
    }

    private SinglePayment readSinglePayment() throws IOException {
        return jsonConverter.toObject(IOUtils.resourceToString(CREATE_PAYMENT_INITIATION_REQUEST_JSON_PATH, UTF_8), SinglePayment.class).get();
    }

}
