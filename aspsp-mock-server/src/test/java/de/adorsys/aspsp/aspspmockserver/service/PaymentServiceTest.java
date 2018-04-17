package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentServiceTest {
    @Autowired
    private PaymentService paymentService;

    @Test
    public void addPayment() {
        //Given
        //SpiSinglePayments expectedPayment = getSpiPayment();

        //When
        //SpiSinglePayments actualPayment = paymentService.addPayment(expectedPayment);

        //Then
        //assertThat(actualPayment).isEqualTo(expectedPayment);
    }


    public SpiSinglePayments getSpiPayment() {// add field values
        SpiSinglePayments spiPayment = new SpiSinglePayments();
        return spiPayment;
    }
}
