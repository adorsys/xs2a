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

import com.google.gson.Gson;
import de.adorsys.aspsp.xs2a.domain.MessageCode;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.PaymentService;
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
import java.util.HashMap;
import java.util.Map;

import static de.adorsys.aspsp.xs2a.exception.MessageCategory.ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PaymentInitiationControllerTest {

    private static final String CREATE_PAYMENT_INITIATION_REQUEST_JSON_PATH = "/json/CreatePaymentInitiationRequestTest.json";
    private static final Charset UTF_8 = Charset.forName("utf-8");
    private static final String PAYMENT_ID = "12345";
    private static final String WRONG_PAYMENT_ID = "Really wrong id";

    @Autowired
    private PaymentInitiationController paymentInitiationController;
    @MockBean
    private PaymentService paymentService;

    @Before
    public void setUpPaymentServiceMock() throws IOException {
        Map<String, TransactionStatus> paymentStatusResponse = new HashMap<>();
        paymentStatusResponse.put("transactionStatus", TransactionStatus.ACCP);
        when(paymentService.createPaymentInitiationAndReturnId(getExpectedRequest(), false))
        .thenReturn(PAYMENT_ID);
        when(paymentService.getPaymentStatusById(PAYMENT_ID, PaymentProduct.SCT))
        .thenReturn(new ResponseObject<>(paymentStatusResponse));
        when(paymentService.getPaymentStatusById(WRONG_PAYMENT_ID, PaymentProduct.SCT))
        .thenReturn(new ResponseObject<>(new MessageError(new TppMessageInformation(ERROR, MessageCode.PRODUCT_UNKNOWN))));
    }

    @Test
    public void getPaymentInitiationStatusById_successesResult() throws IOException {
        //Given:
        boolean tppRedirectPreferred = false;
        HttpStatus expectedStatusCode = HttpStatus.OK;
        String pisRequestJson = IOUtils.resourceToString(CREATE_PAYMENT_INITIATION_REQUEST_JSON_PATH, UTF_8);
        SinglePayments expectedRequest = new Gson().fromJson(pisRequestJson, SinglePayments.class);
        String paymentId = paymentService.createPaymentInitiationAndReturnId(expectedRequest, tppRedirectPreferred);
        Map<String, TransactionStatus> expectedResult = new HashMap<>();
        expectedResult.put("transactionStatus", TransactionStatus.ACCP);

        //When:
        ResponseEntity<Map<String, TransactionStatus>> actualResponse = paymentInitiationController.getPaymentInitiationStatusById(PaymentProduct.SCT.getCode(), paymentId);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        Map<String, TransactionStatus> actualResult = actualResponse.getBody();
        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    private SinglePayments getExpectedRequest() throws IOException {
        String pisRequestJson = IOUtils.resourceToString(CREATE_PAYMENT_INITIATION_REQUEST_JSON_PATH, UTF_8);
        return new Gson().fromJson(pisRequestJson, SinglePayments.class);
    }

    @Test
    public void getAccountConsentsStatusById_wrongId() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.NOT_FOUND;

        //When:
        ResponseEntity<Map<String, TransactionStatus>> actualResponse = paymentInitiationController.getPaymentInitiationStatusById(PaymentProduct.SCT.getCode(), WRONG_PAYMENT_ID);
        HttpStatus actualStatusCode = actualResponse.getStatusCode();

        //Then:
        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
    }

    @Test
    public void createPaymentInitiation() {
        // TODO according task PIS_01_01. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/9
    }
}
