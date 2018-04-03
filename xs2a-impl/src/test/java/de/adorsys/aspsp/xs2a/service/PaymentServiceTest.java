package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.CreatePaymentInitiationRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PaymentServiceTest {
    @Autowired
    PaymentService paymentService;

    @Test
    public void getPaymentStatusById() {

    }

    @Test
    public void getAccountConsentsStatusById_successesResult() {
        //Given:
        boolean withBalance = true;
        boolean tppRedirectPreferred = false;
        CreatePaymentInitiationRequest expectedRequest = getCreatePaymentInitiationRequestTest();
        String validAccountConsentsId = paymentService.createPaymentInitiationAndReturnId(expectedRequest,tppRedirectPreferred);
        TransactionStatus expectedStatus = TransactionStatus.ACCP;

        //When:
        TransactionStatus actualStatus = paymentService.getPaymentStatusById(validAccountConsentsId);

        //Then:
        assertThat(actualStatus).isEqualTo(expectedStatus);
    }

    private CreatePaymentInitiationRequest getCreatePaymentInitiationRequestTest() {
        return new CreatePaymentInitiationRequest(
            null,
            false,
            null,
            null,
            null
        );
    }

    @Test
    public void getAccountConsentsStatusById_wrongId() {
        //Given:
        String wrongId = "111111";

        //When:
        TransactionStatus actualStatus = paymentService.getPaymentStatusById(wrongId);

        //Then:
        assertThat(actualStatus).isNull();
    }
}
