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
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class PeriodicPaymentsControllerTest {
    private final String PERIODIC_PAYMENT_DATA = "/json/PeriodicPaymentTestData.json";
    private final Charset UTF_8 = Charset.forName("utf-8");
    private static final String REDIRECT_LINK = "http://localhost:28080/payment/confirmation/";


    @InjectMocks
    private PeriodicPaymentsController periodicPaymentsController;
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
    public void setUp() {
        when(paymentService.initiatePeriodicPayment(any(), any())).thenReturn(readResponseObject());
        when(aspspProfileService.getPisRedirectUrlToAspsp()).thenReturn(REDIRECT_LINK);
        when(responseMapper.created(any())).thenReturn(new ResponseEntity<>(getPaymentInitializationResponse(), HttpStatus.CREATED));
        when(referenceValidationService.validateAccountReferences(any())).thenReturn(Optional.empty());

    }

    @Test
    public void initiationForStandingOrdersForRecurringOrPeriodicPayments() throws IOException {
        //Given
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        PeriodicPayment periodicPayment = readPeriodicPayment();
        ResponseEntity<PaymentInitialisationResponse> expectedResult = new ResponseEntity<>(getPaymentInitializationResponse(), HttpStatus.CREATED);

        //When:
        ResponseEntity<PaymentInitialisationResponse> result = periodicPaymentsController.createPeriodicPayment(paymentProduct.getCode(), periodicPayment);

        //Then:
        assertThat(result.getStatusCode()).isEqualTo(expectedResult.getStatusCode());
        assertThat(result.getBody().getTransactionStatus().name()).isEqualTo(expectedResult.getBody().getTransactionStatus().name());
    }

    private ResponseObject<PaymentInitialisationResponse> readResponseObject() {

        return ResponseObject.<PaymentInitialisationResponse>builder()
                   .body(getPaymentInitializationResponse()).build();
    }

    private PeriodicPayment readPeriodicPayment() throws IOException {
        return jsonConverter.toObject(IOUtils.resourceToString(PERIODIC_PAYMENT_DATA, UTF_8), PeriodicPayment.class).get();
    }

    private PaymentInitialisationResponse getPaymentInitializationResponse() {
        PaymentInitialisationResponse resp = new PaymentInitialisationResponse();
        resp.setTransactionStatus(TransactionStatus.ACCP);
        resp.setPaymentId("352397d6-a9f2-4914-8549-d127c02660ba");
        resp.setPisConsentId("f33e9b14-56b8-4f3b-b2fd-87884a4a24b9");
        resp.setIban("DE89370400440532013000");
        resp.setLinks(new Links());
        return resp;
    }
}
