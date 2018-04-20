package de.adorsys.aspsp.xs2a.web;

import com.google.gson.Gson;
import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PeriodicPaymentsControllerTest {
    private final String PERIODIC_PAYMENT_DATA = "/json/PeriodicPaymentTestData.json";
    private final Charset UTF_8 = Charset.forName("utf-8");

    @Autowired
    private PeriodicPaymentsController periodicPaymentsController;

    @MockBean(name = "paymentService")
    private PaymentService paymentService;

    @Before
    public void setUp() {
        when(paymentService.initiatePeriodicPayment(any(), anyBoolean(), any())).thenReturn(readResponseObject());
    }

    @Test
    public void initiationForStandingOrdersForRecurringOrPeriodicPayments() throws IOException {
        //Given
        String paymentProduct = "123123";
        boolean tppRedirectPreferred = false;
        PeriodicPayment periodicPayment = readPeriodicPayment();
        ResponseEntity<PaymentInitialisationResponse> expectedResult = new ResponseEntity<>(getPaymentInitializationResponse(), HttpStatus.OK);

        //When:
        ResponseEntity<PaymentInitialisationResponse> result = periodicPaymentsController
                                                               .initiationForStandingOrdersForRecurringOrPeriodicPayments(paymentProduct, tppRedirectPreferred, periodicPayment);

        //Then:
        assertThat(result.getStatusCode()).isEqualTo(expectedResult.getStatusCode());
        assertThat(result.getBody().getTransactionStatus().getName()).isEqualTo(expectedResult.getBody().getTransactionStatus().getName());
        assertThat(result.getBody().get_links()).isEqualTo(expectedResult.getBody().get_links());
    }

    private ResponseObject<PaymentInitialisationResponse> readResponseObject() {

        return ResponseObject.builder()
               .body(getPaymentInitializationResponse()).build();
    }

    private PeriodicPayment readPeriodicPayment() throws IOException {
        return new Gson().fromJson(IOUtils.resourceToString(PERIODIC_PAYMENT_DATA, UTF_8), PeriodicPayment.class);
    }

    private PaymentInitialisationResponse getPaymentInitializationResponse() {
        PaymentInitialisationResponse resp = new PaymentInitialisationResponse();
        resp.setTransactionStatus(TransactionStatus.ACCP);
        resp.set_links(new Links());
        return resp;
    }
}
