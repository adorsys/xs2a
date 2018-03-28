package de.adorsys.aspsp.xs2a.service;


import de.adorsys.aspsp.xs2a.web.ConsentInformationController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RequestValidatorServiceTest {

    @Autowired
    private RequestValidatorService requestValidatorService;
    @Autowired
    private ConsentInformationController consentInformationController;

    @Test
    public void preHandle() throws Exception {
        //Given:
        HttpServletRequest request = getCorrectRequest();
        Object handler = getHandler();

        //When:
        Map<String, String> actualViolations = requestValidatorService.getRequestHeaderViolationMap(request, handler);

        //Then:
        assertThat(actualViolations.isEmpty()).isTrue();
    }

    @Test
    public void shouldFail_preHandle_wrongRequest() throws Exception {
        //Given:
        HttpServletRequest request = getWrongRequestNoTppRequestId();
        Object handler = getHandler();

        //When:
        Map<String, String> actualViolations = requestValidatorService.getRequestHeaderViolationMap(request, handler);

        //Then:
        assertThat(actualViolations.size()).isEqualTo(1);
        assertThat(actualViolations.get("tppRequestId")).isEqualTo("must not be null");
    }


    @Test
    public void shouldFail_preHandle_wrongRequestHeaderFormat() throws Exception {
        //Given:
        HttpServletRequest request = getWrongRequestWrongTppRequestIdFormat();
        Object handler = getHandler();

        //When:
        Map<String, String> actualViolations = requestValidatorService.getRequestHeaderViolationMap(request, handler);

        //Then:
        assertThat(actualViolations.size()).isEqualTo(1);
        assertThat(actualViolations.get("Wrong header arguments: ")).contains("Can not deserialize value");
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

    private Object getHandler() throws NoSuchMethodException {
        return new HandlerMethod(consentInformationController, "getAccountConsentsInformationById", String.class);
    }
}
