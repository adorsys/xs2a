package de.adorsys.aspsp.xs2a.web;

import com.google.gson.Gson;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.service.PaymentService;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PaymentInitiationControllerTest {

    private final String CREATE_PAYMENT_INITIATION_REQUEST_JSON_PATH = "/json/CreatePaymentInitiationRequestTest.json";
    private final Charset UTF_8 = Charset.forName("utf-8");
    private final String PAYMENT_ID = "12345";

    @Autowired
    PaymentInitiationController paymentInitiationController;
    @MockBean
    PaymentService paymentService;

    @Before
    public void setUpPaymentServiceMock() throws IOException {
        when(paymentService.createPaymentInitiationAndReturnId(getExpectedRequest(), false))
        .thenReturn(PAYMENT_ID);
        when(paymentService.getPaymentStatusById(PAYMENT_ID))
        .thenReturn(TransactionStatus.ACCP);
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
        ResponseEntity<Map<String, TransactionStatus>> actualResponse = paymentInitiationController.getPaymentInitiationStatusById(paymentId);

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
        Map<String, TransactionStatus> expectedResult = new HashMap<>();
        expectedResult.put("transactionStatus", null);
        String wrongId = "0";

        //When:
        ResponseEntity<Map<String, TransactionStatus>> actualResponse = paymentInitiationController.getPaymentInitiationStatusById(wrongId);

        //Then:
        Map<String, TransactionStatus> actualResult = actualResponse.getBody();
        assertThat(actualResult).isEqualTo(expectedResult);
    }

}
