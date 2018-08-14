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
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayment;
import de.adorsys.aspsp.xs2a.domain.pis.TppInfo;
import de.adorsys.aspsp.xs2a.service.AspspProfileService;
import de.adorsys.aspsp.xs2a.service.PaymentService;
import de.adorsys.aspsp.xs2a.service.mapper.ResponseMapper;
import de.adorsys.aspsp.xs2a.service.validator.AccountReferenceValidationService;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BulkPaymentInitiationControllerTest {
    private final String BULK_PAYMENT_DATA = "/json/BulkPaymentTestData.json";
    private final String BULK_PAYMENT_RESP_DATA = "/json/BulkPaymentResponseTestData.json";
    private final Charset UTF_8 = Charset.forName("utf-8");
    private final PaymentProduct PAYMENT_PRODUCT = PaymentProduct.SCT;
    private static final String REDIRECT_LINK = "http://localhost:28080/payment/confirmation/";
    private static final String TPP_INFO = "";

    @InjectMocks
    private BulkPaymentInitiationController bulkPaymentInitiationController;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private JsonConverter jsonConverter = new JsonConverter(objectMapper);

    @Mock
    private PaymentService paymentService;
    @Mock
    private AspspProfileService aspspProfileService;
    @Mock
    private ResponseMapper responseMapper;
    @Mock
    private AccountReferenceValidationService referenceValidationService;

    @Before
    public void setUp() throws IOException {
        when(paymentService.createBulkPayments(any(), anyString(), any())).thenReturn(readResponseObject());
        when(aspspProfileService.getPisRedirectUrlToAspsp()).thenReturn(REDIRECT_LINK);
        when(responseMapper.created(any())).thenReturn(new ResponseEntity<>(readPaymentInitialisationResponse(), HttpStatus.CREATED));
        when(referenceValidationService.validateAccountReferences(any())).thenReturn(Optional.empty());
    }

    @Test
    public void createBulkPaymentInitiation() throws IOException {
        //Given
        List<SinglePayment> payments = readBulkPayments();
        ResponseEntity<List<PaymentInitialisationResponse>> expectedResult = new ResponseEntity<>(readPaymentInitialisationResponse(), HttpStatus.CREATED);

        //When:
        ResponseEntity<List<PaymentInitialisationResponse>> actualResult = bulkPaymentInitiationController
                                                                               .createBulkPaymentInitiation(PAYMENT_PRODUCT.getCode(), TPP_INFO, payments);

        //Then:
        assertThat(actualResult.getStatusCode()).isEqualTo(expectedResult.getStatusCode());
        assertThat(actualResult.getBody()).isEqualTo(expectedResult.getBody());
    }

    private ResponseObject<List<PaymentInitialisationResponse>> readResponseObject() throws IOException {
        return ResponseObject.<List<PaymentInitialisationResponse>>builder()
                   .body(readPaymentInitialisationResponse()).build();
    }

    private List<PaymentInitialisationResponse> readPaymentInitialisationResponse() throws IOException {
        PaymentInitialisationResponse response = jsonConverter.toObject(IOUtils.resourceToString(BULK_PAYMENT_RESP_DATA, UTF_8), PaymentInitialisationResponse.class).get();
        List<PaymentInitialisationResponse> responseList = new ArrayList<>();
        responseList.add(response);

        return responseList;
    }

    private List<SinglePayment> readBulkPayments() throws IOException {
        SinglePayment[] payments = jsonConverter.toObject(IOUtils.resourceToString(BULK_PAYMENT_DATA, UTF_8), SinglePayment[].class).get();
        return Arrays.asList(payments);
    }
}
