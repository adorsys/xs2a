package de.adorsys.aspsp.xs2a.web;

import com.google.gson.Gson;
import de.adorsys.aspsp.xs2a.domain.MessageCode;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
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
        when(paymentService.getPaymentStatusById(PAYMENT_ID))
        .thenReturn(new ResponseObject<>(paymentStatusResponse));
        when(paymentService.getPaymentStatusById(WRONG_PAYMENT_ID))
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
        //Given:короч
        HttpStatus expectedStatusCode = HttpStatus.NOT_FOUND;

        //When:
        ResponseEntity<Map<String, TransactionStatus>> actualResponse = paymentInitiationController.getPaymentInitiationStatusById(WRONG_PAYMENT_ID);
        HttpStatus actualStatusCode = actualResponse.getStatusCode();

        //Then:
        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
    }

    @Test
    public void createPaymentInitiation() {
        // TODO according task PIS_01_01. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/9
    }
}
