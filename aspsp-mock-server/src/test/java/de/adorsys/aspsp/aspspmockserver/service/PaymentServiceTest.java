package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.aspspmockserver.repository.PaymentRepository;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PaymentServiceTest {
    private static final String PAYMENT_ID = "123456789";
    private static final String WRONG_PAYMENT_ID = "0";

    @Autowired
    private PaymentService paymentService;
    @MockBean
    private PaymentRepository paymentRepository;

    @Before
    public void setUp() {
        when(paymentRepository.save(any(SpiSinglePayments.class)))
        .thenReturn(new SpiSinglePayments());
        when(paymentRepository.exists(PAYMENT_ID))
        .thenReturn(true);
        when(paymentRepository.exists(WRONG_PAYMENT_ID))
        .thenReturn(false);
    }

    @Test
    public void addPayment() {
        //Given
        SpiSinglePayments expectedPayment = new SpiSinglePayments();

        //When
        SpiSinglePayments actualPayment = paymentService.addPayment(expectedPayment).get();

        //Then
        assertThat(actualPayment).isNotNull();
    }

    @Test
    public void getPaymentStatusById() {
        //Then
        assertThat(paymentService.isPaymentExist(PAYMENT_ID)).isTrue();
        assertThat(paymentService.isPaymentExist(WRONG_PAYMENT_ID)).isFalse();
    }
}
