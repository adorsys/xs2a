package de.adorsys.aspsp.xs2a.web;

import com.google.gson.Gson;
import de.adorsys.aspsp.xs2a.domain.MessageCode;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PaymentInitiationControllerTest {

    private static final String CREATE_PAYMENT_INITIATION_REQUEST_JSON_PATH = "/json/CreatePaymentInitiationRequestTest.json";
    private static final String CREATE_PAYMENT_INITIATION_RESPONSE_JSON_PATH = "/json/CreatePaymentInitiationResponseTest.json";
    private static final Charset UTF_8 = Charset.forName("utf-8");
    private static final String PAYMENT_ID = "12345";
    private static final String WRONG_PAYMENT_ID = "Really wrong id";

    @Autowired
    private PaymentInitiationController paymentInitiationController;
    @MockBean
    private PaymentService paymentService;

    @Before
    public void setUp() throws IOException {
        when(paymentService.createPaymentInitiation(any(), any(), anyBoolean())).thenReturn(readResponseObject());
    }

    @Before
    public void setUpPaymentServiceMock() throws IOException {
        Map<String, TransactionStatus> paymentStatusResponse = new HashMap<>();
        paymentStatusResponse.put("transactionStatus", TransactionStatus.ACCP);
        when(paymentService.getPaymentStatusById(PAYMENT_ID, PaymentProduct.SCT))
        .thenReturn(new ResponseObject<>(paymentStatusResponse));
        when(paymentService.getPaymentStatusById(WRONG_PAYMENT_ID, PaymentProduct.SCT))
        .thenReturn(new ResponseObject<>(new MessageError(new TppMessageInformation(ERROR, MessageCode.PRODUCT_UNKNOWN))));
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
    public void createPaymentInitiation() throws IOException {
        //Given
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        boolean tppRedirectPreferred = false;
        SinglePayments payment = readSinglePayments();
        ResponseEntity<PaymentInitialisationResponse> expectedResult = new ResponseEntity<>(readPaymentInitialisationResponse(), HttpStatus.CREATED);

        //When:
        ResponseEntity<PaymentInitialisationResponse> actualResult = paymentInitiationController
                                                                     .createPaymentInitiation(paymentProduct.getCode(), tppRedirectPreferred, payment);

        //Then:
        assertThat(actualResult.getStatusCode()).isEqualTo(expectedResult.getStatusCode());
        assertThat(actualResult.getBody()).isEqualTo(expectedResult.getBody());
    }

    private ResponseObject<PaymentInitialisationResponse> readResponseObject() throws IOException {
        return new ResponseObject<>(readPaymentInitialisationResponse());
    }

    private PaymentInitialisationResponse readPaymentInitialisationResponse() throws IOException {
        return new Gson().fromJson(IOUtils.resourceToString(CREATE_PAYMENT_INITIATION_RESPONSE_JSON_PATH, UTF_8), PaymentInitialisationResponse.class);
    }

    private SinglePayments readSinglePayments() throws IOException {
        return new Gson().fromJson(IOUtils.resourceToString(CREATE_PAYMENT_INITIATION_REQUEST_JSON_PATH, UTF_8), SinglePayments.class);
    }

}
