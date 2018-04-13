package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class PaymentServiceTest {
    @Autowired
    private PaymentService paymentService;

    @Test
    public void addPayment() {
        //Given
        SpiPaymentInitialisationResponse expectedPayment = getSpiPayment();

        //When
        SpiPaymentInitialisationResponse actualPayment = paymentService.addPayment(expectedPayment);

        //Then
        assertThat(actualPayment).isEqualTo(expectedPayment);
    }


    public SpiPaymentInitialisationResponse getSpiPayment() {
        SpiPaymentInitialisationResponse spiPayment = new SpiPaymentInitialisationResponse();

        return spiPayment;
    }
}
