package de.adorsys.aspsp.aspspmockserver.web;

import de.adorsys.aspsp.aspspmockserver.service.PaymentService;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
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
import java.util.Optional;

import static de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus.ACCP;
import static de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus.RJCT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PaymentControllerTest {
    private static final String PAYMENT_ID = "123456789";
    private static final String WRONG_PAYMENT_ID = "0";

    @Autowired
    private PaymentController paymentController;
    @MockBean
    private PaymentService paymentService;

    @Before
    public void setUpPaymentServiceMock() throws IOException {
        SpiSinglePayments response = getExpectedRequest();
        response.setPaymentId("12345");
        when(paymentService.addPayment(getExpectedRequest()))
        .thenReturn(Optional.of(response));
        when(paymentService.isPaymentExist(PAYMENT_ID))
        .thenReturn(true);
        when(paymentService.isPaymentExist(WRONG_PAYMENT_ID))
        .thenReturn(false);
    }

    @Test
    public void createPayment() throws Exception {
        //Given
        HttpStatus expectedStatus = HttpStatus.CREATED;

        //When
        ResponseEntity<SpiSinglePayments> actualResponse = paymentController.createPayment(getExpectedRequest());
        HttpStatus actualStatus = actualResponse.getStatusCode();

        //Then
        assertThat(actualStatus).isEqualTo(expectedStatus);
        assertThat(actualResponse.getBody()).isNotNull();
        assertThat(actualResponse.getBody().getPaymentId()).isNotNull();
    }

    private SpiSinglePayments getExpectedRequest() {
        SpiSinglePayments spiSinglePayments = new SpiSinglePayments();
        return spiSinglePayments;
    }

    @Test
    public void getPaymentStatusById_Success() throws Exception {
        //Given
        HttpStatus expectedStatus = HttpStatus.OK;

        //When
        ResponseEntity<SpiSinglePayments> actualResponse = paymentController.getPaymentStatusById(PAYMENT_ID);
        HttpStatus actualStatus = actualResponse.getStatusCode();

        //Then
        assertThat(actualStatus).isEqualTo(expectedStatus);
        assertThat(actualResponse.getBody()).isNotNull();
        assertThat(actualResponse.getBody()).isEqualTo(ACCP);
    }

    @Test
    public void getPaymentStatusById_WrongId() throws Exception {
        //Given
        HttpStatus expectedStatus = HttpStatus.OK;

        //When
        ResponseEntity<SpiSinglePayments> actualResponse = paymentController.getPaymentStatusById(WRONG_PAYMENT_ID);
        HttpStatus actualStatus = actualResponse.getStatusCode();

        //Then
        assertThat(actualStatus).isEqualTo(expectedStatus);
        assertThat(actualResponse.getBody()).isNotNull();
        assertThat(actualResponse.getBody()).isEqualTo(RJCT);
    }
}
