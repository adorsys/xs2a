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

import de.adorsys.aspsp.xs2a.component.JsonConverter;
import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.service.AspspProfileService;
import de.adorsys.aspsp.xs2a.service.CommonPaymentService;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PeriodicPaymentsControllerTest {
    private final String PERIODIC_PAYMENT_DATA = "/json/PeriodicPaymentTestData.json";
    private final Charset UTF_8 = Charset.forName("utf-8");
    private static final String REDIRECT_LINK = "http://localhost:28080/view/payment/confirmation/";


    @Autowired
    private PeriodicPaymentsController periodicPaymentsController;
    @Autowired
    private JsonConverter jsonConverter;

    @MockBean(name = "paymentService")
    private CommonPaymentService paymentService;
    @MockBean
    private AspspProfileService aspspProfileService;

    @Before
    public void setUp() {
        when(paymentService.initiatePeriodicPayment(any(), any(), anyBoolean())).thenReturn(readResponseObject());
        when(aspspProfileService.getPisRedirectUrlToAspsp()).thenReturn(REDIRECT_LINK);
    }

    @Test
    public void initiationForStandingOrdersForRecurringOrPeriodicPayments() throws IOException {
        //Given
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        boolean tppRedirectPreferred = false;
        PeriodicPayment periodicPayment = readPeriodicPayment();
        ResponseEntity<PaymentInitialisationResponse> expectedResult = new ResponseEntity<>(getPaymentInitializationResponse(), HttpStatus.CREATED);

        //When:
        ResponseEntity<PaymentInitialisationResponse> result = periodicPaymentsController.createPeriodicPayment(paymentProduct.getCode(), tppRedirectPreferred, periodicPayment);

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
        resp.setLinks(new Links());
        return resp;
    }
}
