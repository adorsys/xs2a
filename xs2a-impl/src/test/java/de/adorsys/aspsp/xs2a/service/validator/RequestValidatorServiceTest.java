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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.aspsp.xs2a.service.validator.parameter.ParametersFactory;
import de.adorsys.aspsp.xs2a.web.ConsentController;
import de.adorsys.aspsp.xs2a.web.PaymentController;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.*;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.PARAMETER_NOT_SUPPORTED;
import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.PRODUCT_UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RequestValidatorServiceTest {

    @InjectMocks
    private RequestValidatorService requestValidatorService;
    @Mock
    private ConsentController consentController;
    @Mock
    private PaymentController paymentController;
    @Mock
    private AspspProfileServiceWrapper aspspProfileService;
    @Spy
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    @Spy
    private ParametersFactory parametersFactory = new ParametersFactory(new ObjectMapper());

    @Before
    public void setUp() {
        when(aspspProfileService.getAvailablePaymentProducts())
            .thenReturn(Arrays.asList(PaymentProduct.ISCT, PaymentProduct.SCT));

        when(aspspProfileService.getAvailablePaymentTypes())
            .thenReturn(Arrays.asList(PaymentType.SINGLE, PaymentType.BULK));
    }

    @Test
    public void getRequestHeaderViolationMap() throws Exception {
        //Given:
        HttpServletRequest request = getCorrectRequest();
        HandlerMethod handler = getPaymentsControllerHandler();

        //When:
        Map<String, String> actualViolations = requestValidatorService.getRequestViolationMap(request, handler);

        //Then:
        assertThat(actualViolations.isEmpty()).isTrue();
    }

    @Test
    public void shouldFail_getRequestHeaderViolationMap_wrongRequest() throws Exception {
        //Given:
        HttpServletRequest request = getWrongRequestNoXRequestId();
        HandlerMethod handler = getPaymentsControllerHandler();

        //When:

        Map<String, String> actualViolations = requestValidatorService.getRequestViolationMapInitiatePayment(request);

        //Then:
        assertThat(actualViolations.size()).isEqualTo(1);
        assertThat(actualViolations.get("xRequestId")).isNotNull();
    }

    @Test
    public void shouldFail_getRequestHeaderViolationMap_wrongRequestHeaderFormat() throws Exception {
        //Given:
        HttpServletRequest request = getWrongRequestWrongTppRequestIdFormat();
        HandlerMethod handler = getPaymentsControllerHandler();

        //When:
        Map<String, String> actualViolations = requestValidatorService.getRequestViolationMapInitiatePayment(request);

        //Then:
        assertThat(actualViolations.size()).isEqualTo(1);
        assertThat(actualViolations.get("Wrong header arguments: ")).isNotNull();
    }

    @Test
    public void getRequestPathVariablesViolationMap_WrongProduct() throws Exception {
        //Given:
        HttpServletRequest request = getCorrectRequestForPayment();
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Collections.singletonMap("payment-product", PaymentProduct.CBCT.getCode()));
        HandlerMethod handler = getPaymentInitiationControllerHandler();

        //When:
        Map<String, String> actualViolations = requestValidatorService.getRequestViolationMap(request, handler);

        //Then:
        assertThat(actualViolations.size()).isEqualTo(1);
        assertThat(actualViolations.get(PRODUCT_UNKNOWN.getName())).contains("Wrong payment product: cross-border-credit-transfers");
    }

    @Test
    public void getRequestPathVariablesViolationMap() throws Exception {
        //Given:
        HttpServletRequest request = getCorrectRequestForPayment();
        Map<String, String> templates = new HashMap<>();
        templates.put("payment-product", PaymentProduct.SCT.getCode());
        templates.put("payment-service", PaymentType.SINGLE.getValue());
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, templates);
        HandlerMethod handler = getPaymentInitiationControllerHandler();

        //When:
        Map<String, String> actualViolations = requestValidatorService.getRequestViolationMap(request, handler);

        //Then:
        assertThat(actualViolations.isEmpty()).isTrue();
    }

    @Test
    public void getRequestPathVariablesViolationMap_wrongPaymentType() throws Exception {
        //Given:
        HttpServletRequest request = getCorrectRequestForPayment();
        Map<String, String> templates = new HashMap<>();
        templates.put("payment-product", PaymentProduct.SCT.getCode());
        templates.put("payment-service", PaymentType.PERIODIC.getValue());
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, templates);
        HandlerMethod handler = getPaymentsControllerHandler();

        //When:
        Map<String, String> actualViolations = requestValidatorService.getRequestViolationMap(request, handler);

        //Then:
        assertThat(actualViolations.size()).isEqualTo(1);
        assertThat(actualViolations.get(PARAMETER_NOT_SUPPORTED.getName())).contains("Wrong payment type: periodic");
    }

    @Test
    public void getRequestHeaderViolationMap_wrongIpAddressV4() throws Exception {
        //Given:
        HttpServletRequest request = getRequestWithIpAddress("wrong ip");

        //When:
        Map<String, String> actualViolations = requestValidatorService.getRequestViolationMapInitiatePayment(request);

        //Then:
        assertThat(actualViolations.size()).isEqualTo(1);
        assertThat(actualViolations.get("psuIpAddress")).isNotNull();
    }

    @Test
    public void getRequestHeaderViolationMap_wrongIpAddressV6() throws Exception {
        //Given:
        HttpServletRequest request = getRequestWithIpAddress("1200::AB00:1234::2552:7777:1313");

        //When:
        Map<String, String> actualViolations = requestValidatorService.getRequestViolationMapInitiatePayment(request);

        //Then:
        assertThat(actualViolations.size()).isEqualTo(1);
        assertThat(actualViolations.get("psuIpAddress")).isNotNull();
    }

    @Test
    public void getRequestHeaderViolationMap_correctIpAddressV6() throws Exception {
        //Given:
        HttpServletRequest request = getRequestWithIpAddress("1200:0000:AB00:1234:0000:2552:7777:1313");

        //When:
        Map<String, String> actualViolations = requestValidatorService.getRequestViolationMapInitiatePayment(request);

        //Then:
        assertThat(actualViolations.isEmpty()).isTrue();
    }

    private HttpServletRequest getWrongRequestNoXRequestId() {
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
        request.addHeader("x-request-id", "wrong_format");
        request.addHeader("consent-id", "21d40f65-a150-8343-b539-b9a822ae98c0");

        return request;
    }

    private HttpServletRequest getCorrectRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Content-Type", "application/json");
        request.addHeader("tpp-transaction-id", "16d40f49-a110-4344-a949-f99828ae13c9");
        request.addHeader("x-request-id", "21d40f65-a150-8343-b539-b9a822ae98c0");
        request.addHeader("consent-id", "21d40f65-a150-8343-b539-b9a822ae98c0");

        return request;
    }

    private HttpServletRequest getCorrectRequestForPayment() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Content-Type", "application/json");
        request.addHeader("tpp-transaction-id", "16d40f49-a110-4344-a949-f99828ae13c9");
        request.addHeader("x-request-id", "21d40f65-a150-8343-b539-b9a822ae98c0");
        request.addHeader("psu-ip-address", "192.168.8.78");

        return request;
    }

    private HttpServletRequest getRequestWithIpAddress(String ip) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Content-Type", "application/json");
        request.addHeader("tpp-transaction-id", "16d40f49-a110-4344-a949-f99828ae13c9");
        request.addHeader("x-request-id", "21d40f65-a150-8343-b539-b9a822ae98c0");
        request.addHeader("consent-id", "21d40f65-a150-8343-b539-b9a822ae98c0");
        request.addHeader("psu-ip-address", ip);

        return request;
    }

    private HandlerMethod getHandler() throws NoSuchMethodException {
        Class<?>[] params = new Class<?>[]{String.class, UUID.class, String.class, String.class, byte[].class,
            String.class, Object.class, String.class, String.class, String.class, String.class, String.class,
            String.class, UUID.class, String.class};
        return new HandlerMethod(consentController, "getConsentInformation", params);
    }

    private HandlerMethod getPaymentInitiationControllerHandler() throws NoSuchMethodException {
        Class<?>[] params = new Class<?>[]{String.class, String.class, UUID.class, String.class, String.class,
            byte[].class, String.class, Object.class, String.class, String.class, String.class, String.class,
            String.class, String.class, UUID.class, String.class};
        return new HandlerMethod(paymentController, "getPaymentInitiationStatus", params);
    }

    private HandlerMethod getPaymentsControllerHandler() throws NoSuchMethodException {
        Class<?>[] params = new Class<?>[]{Object.class, String.class, String.class, UUID.class, String.class,
            String.class, String.class, byte[].class, String.class, String.class, String.class, String.class,
            String.class, Boolean.class, String.class, String.class, Boolean.class, Object.class, String.class,
            String.class, String.class, String.class, String.class, String.class, UUID.class, String.class};
        return new HandlerMethod(paymentController, "initiatePayment", params);
    }
}
