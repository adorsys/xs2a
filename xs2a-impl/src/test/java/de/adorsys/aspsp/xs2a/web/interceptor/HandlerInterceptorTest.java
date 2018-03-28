package de.adorsys.aspsp.xs2a.web.interceptor;


import de.adorsys.aspsp.xs2a.web.ConsentInformationController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HandlerInterceptorTest {

    @Autowired
    private HandlerInterceptor handlerInterceptor;

    @Autowired
    private ConsentInformationController consentInformationController;

    @Test
    public void preHandle() throws Exception {
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
        //Given:
        HttpServletRequest wrongRequest = getWrongRequestWrongTppRequestIdFormat();
        HttpServletResponse response = getResponse();
        Object handler = getHandler();
        int expectedResponseHttpStatusCode = 400;

        //When:
        boolean actualResponse = handlerInterceptor.preHandle(wrongRequest, response ,handler);

        //Then:
        assertThat(actualResponse).isFalse();
        assertThat(response.getStatus()).isEqualTo(expectedResponseHttpStatusCode);
    }

    @Test(expected = NullPointerException.class)
    public void shouldFail_preHandle_NPE() throws Exception {
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

    private HttpServletResponse getResponse() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        return response;
    }

    private Object getHandler() throws NoSuchMethodException {
        return new HandlerMethod(consentInformationController, "getAccountConsentsInformationById", String.class);
    }
}
