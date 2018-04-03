package de.adorsys.aspsp.xs2a.web;

import com.google.gson.Gson;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.CreatePaymentInitiationRequest;
import de.adorsys.aspsp.xs2a.service.PaymentService;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PaymentInitiationControllerTest {

    private final String CREATE_PAYMENT_INITIATION_REQUEST_JSON_PATH = "/json/CreatePaymentInitiationRequestTest.json";
    private final Charset UTF_8 = Charset.forName("utf-8");

    @Autowired
    PaymentInitiationController paymentInitiationController;
    @Autowired
    PaymentService paymentService;

    @Test
    public void createPaymentInitiation() {
    }

    @Test
    public void getPaymentInitiationStatusById_successesResult() throws IOException {
        //Given:
        boolean tppRedirectPreferred = false;
        HttpStatus expectedStatusCode = HttpStatus.OK;
        String pisRequestJson = IOUtils.resourceToString(CREATE_PAYMENT_INITIATION_REQUEST_JSON_PATH, UTF_8);
        CreatePaymentInitiationRequest expectedRequest = new Gson().fromJson(pisRequestJson, CreatePaymentInitiationRequest.class);
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

    @Test
    public void shouldFail_getAccountConsentsStatusById_wrongId() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;
        Map<String, TransactionStatus> expectedResult = new HashMap<>();
        expectedResult.put("transactionStatus", null);
        String wrongId = "111111";

        //When:
        ResponseEntity<Map<String, TransactionStatus>> actualResponse = paymentInitiationController.getPaymentInitiationStatusById(wrongId);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        Map<String, TransactionStatus> actualResult = actualResponse.getBody();
        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    public void getPaymentInitiation() {
    }

}
