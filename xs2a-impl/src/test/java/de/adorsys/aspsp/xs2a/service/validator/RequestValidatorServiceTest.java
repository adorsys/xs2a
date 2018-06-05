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

package de.adorsys.aspsp.xs2a.service.validator;


import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.service.AspspProfileService;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.aspsp.xs2a.spi.impl.ConsentSpiImpl;
import de.adorsys.aspsp.xs2a.web.ConsentInformationController;
import de.adorsys.aspsp.xs2a.web.PaymentInitiationController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static de.adorsys.aspsp.xs2a.domain.MessageCode.PRODUCT_UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RequestValidatorServiceTest {

    @Autowired
    private RequestValidatorService requestValidatorService;
    @Autowired
    private ConsentInformationController consentInformationController;

    @Autowired
    private PaymentInitiationController paymentInitiationController;

    @MockBean(name = "aspspProfileService")
    AspspProfileService aspspProfileService;

    @Before
    public void setUp() {
        when(aspspProfileService.getAvailablePaymentProducts())
            .thenReturn(Arrays.asList(PaymentProduct.ISCT, PaymentProduct.SCT));

    }
    @Test
    public void getRequestHeaderViolationMap() throws Exception {
        //Given:
        HttpServletRequest request = getCorrectRequest();
        Object handler = getHandler();

        //When:
        Map<String, String> actualViolations = requestValidatorService.getRequestHeaderViolationMap(request, handler);

        //Then:
        assertThat(actualViolations.isEmpty()).isTrue();
    }

    @Test
    public void shouldFail_getRequestHeaderViolationMap_wrongRequest() throws Exception {
        //Given:
        HttpServletRequest request = getWrongRequestNoTppRequestId();
        Object handler = getHandler();

        //When:
        Map<String, String> actualViolations = requestValidatorService.getRequestHeaderViolationMap(request, handler);

        //Then:
        assertThat(actualViolations.size()).isEqualTo(1);
    }

    @Test
    public void shouldFail_getRequestHeaderViolationMap_wrongRequestHeaderFormat() throws Exception {
        //Given:
        HttpServletRequest request = getWrongRequestWrongTppRequestIdFormat();
        Object handler = getHandler();

        //When:
        Map<String, String> actualViolations = requestValidatorService.getRequestHeaderViolationMap(request, handler);

        //Then:
        assertThat(actualViolations.size()).isEqualTo(1);
        assertThat(actualViolations.get("Wrong header arguments: ")).contains("Can not deserialize value");
    }

    @Test
    public void getRequestPathVariablesViolationMap_WrongProduct() throws Exception {
        //Given:
        HttpServletRequest request = getCorrectRequestForPayment();
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Collections.singletonMap("payment-product", PaymentProduct.CBCT.getCode()));

        Object handler = getPaymentInitiationControllerHandler();

        //When:
        Map<String, String> actualViolations = requestValidatorService.getRequestPathVariablesViolationMap(request, handler);

        //Then:
        assertThat(actualViolations.size()).isEqualTo(1);
        assertThat(actualViolations.get(PRODUCT_UNKNOWN.getName())).contains("Wrong payment product: cross-border-credit-transfers");
    }

    @Test
    public void getRequestPathVariablesViolationMap() throws Exception {
        //Given:
        HttpServletRequest request = getCorrectRequestForPayment();
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Collections.singletonMap("payment-product", PaymentProduct.SCT.getCode()));
        Object handler = getPaymentInitiationControllerHandler();

        //When:
        Map<String, String> actualViolations = requestValidatorService.getRequestPathVariablesViolationMap(request, handler);

        //Then:
        assertThat(actualViolations.isEmpty()).isTrue();
    }

    private HttpServletRequest getWrongRequestNoTppRequestId() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Content-Type", "application/json");
        request.addHeader("tpp-transaction-id", "16d40f49-a110-4344-a949-f99828ae13c9");
        request.addHeader("consent-id", "21d40f65-a150-8343-b539-b9a822ae98c0");

        return request;
    }

    private HttpServletRequest getWrongRequestWrongTppRequestIdFormat() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Content-Type", "application/json");
        request.addHeader("tpp-transaction-id", "16d40f49-a110-4344-a949-f99828ae13c9");
        request.addHeader("tpp-request-id", "wrong_format");
        request.addHeader("consent-id", "21d40f65-a150-8343-b539-b9a822ae98c0");

        return request;
    }

    private HttpServletRequest getCorrectRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Content-Type", "application/json");
        request.addHeader("tpp-transaction-id", "16d40f49-a110-4344-a949-f99828ae13c9");
        request.addHeader("tpp-request-id", "21d40f65-a150-8343-b539-b9a822ae98c0");
        request.addHeader("consent-id", "21d40f65-a150-8343-b539-b9a822ae98c0");

        return request;
    }

    private HttpServletRequest getCorrectRequestForPayment() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Content-Type", "application/json");
        request.addHeader("tpp-transaction-id", "16d40f49-a110-4344-a949-f99828ae13c9");
        request.addHeader("tpp-request-id", "21d40f65-a150-8343-b539-b9a822ae98c0");
        request.addHeader("psu-ip-address", "192.168.8.78");

        return request;
    }

    private Object getHandler() throws NoSuchMethodException {
        return new HandlerMethod(consentInformationController, "getAccountConsentsInformationById", String.class);
    }

    private Object getPaymentInitiationControllerHandler() throws NoSuchMethodException {
        return new HandlerMethod(paymentInitiationController, "getPaymentInitiationStatusById", String.class, String.class);
    }
}
