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

package de.adorsys.aspsp.xs2a.web.interceptor;


import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.aspsp.xs2a.service.mapper.MessageErrorMapper;
import de.adorsys.aspsp.xs2a.service.validator.RequestValidatorService;
import de.adorsys.aspsp.xs2a.web.ConsentInformationController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HandlerInterceptorTest {

    @InjectMocks
    private HandlerInterceptor handlerInterceptor;
    @Mock
    RequestValidatorService requestValidatorService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private MessageErrorMapper messageErrorMapper;

    @InjectMocks
    private ConsentInformationController consentInformationController;

    @Test
    public void preHandle() throws Exception {
        when(requestValidatorService.getRequestViolationMap(any(), any())).thenReturn(new HashMap<>());
        //Given:
        HttpServletRequest request = getCorrectRequest();
        HttpServletResponse response = getResponse();
        Object handler = getHandler();

        //When:
        boolean actualResponse = handlerInterceptor.preHandle(request, response, handler);

        //Then:
        assertThat(actualResponse).isTrue();
    }

    @Test
    public void shouldFail_preHandle_wrongRequest() throws Exception {
        when(requestValidatorService.getRequestViolationMap(any(), any())).thenReturn(getErrorMap());
        when(objectMapper.writeValueAsString(any())).thenReturn("400");
        //Given:
        HttpServletRequest wrongRequest = getWrongRequest();
        HttpServletResponse response = getResponse();
        Object handler = getHandler();
        int expectedResponseHttpStatusCode = 400;

        //When:
        boolean actualResponse = handlerInterceptor.preHandle(wrongRequest, response, handler);

        //Then:
        assertThat(actualResponse).isFalse();
        assertThat(response.getStatus()).isEqualTo(expectedResponseHttpStatusCode);
    }

    @Test
    public void shouldFail_preHandle_wrongRequestHeaderFormat() throws Exception {
        when(requestValidatorService.getRequestViolationMap(any(), any())).thenReturn(getErrorMap());
        when(objectMapper.writeValueAsString(any())).thenReturn("400");
        //Given:
        HttpServletRequest wrongRequest = getWrongRequestWrongTppRequestIdFormat();
        HttpServletResponse response = getResponse();
        Object handler = getHandler();
        int expectedResponseHttpStatusCode = 400;

        //When:
        boolean actualResponse = handlerInterceptor.preHandle(wrongRequest, response, handler);

        //Then:
        assertThat(actualResponse).isFalse();
        assertThat(response.getStatus()).isEqualTo(expectedResponseHttpStatusCode);
    }

    @Test(expected = NullPointerException.class)
    public void shouldFail_preHandle_NPE() throws Exception {
        when(requestValidatorService.getRequestViolationMap(any(), any())).thenReturn(null);
        //Given:
        HttpServletRequest request = getCorrectRequest();
        HttpServletResponse response = getResponse();
        Object handler = null;

        //When:
        handlerInterceptor.preHandle(request, response, handler);
    }

    private HttpServletRequest getWrongRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Content-Type", "application/json");
        request.addHeader("consent-id", "21d40f65-a150-8343-b539-b9a822ae98c0");

        return request;
    }

    private HttpServletRequest getWrongRequestWrongTppRequestIdFormat() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Content-Type", "application/json");
        request.addHeader("x-request-id", "wrong_format");
        request.addHeader("consent-id", "21d40f65-a150-8343-b539-b9a822ae98c0");

        return request;
    }

    private HttpServletRequest getCorrectRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Content-Type", "application/json");
        request.addHeader("x-request-id", "21d40f65-a150-8343-b539-b9a822ae98c0");
        request.addHeader("consent-id", "21d40f65-a150-8343-b539-b9a822ae98c0");

        return request;
    }

    private HttpServletResponse getResponse() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        return response;
    }

    private Object getHandler() throws NoSuchMethodException {
        return new HandlerMethod(consentInformationController, "getAccountConsentsInformationById", String.class);
    }

    private Map<String, String> getErrorMap() {
        Map<String, String> errors = new HashMap();
        errors.put("error1", "errorMgs1");
        return errors;
    }
}
