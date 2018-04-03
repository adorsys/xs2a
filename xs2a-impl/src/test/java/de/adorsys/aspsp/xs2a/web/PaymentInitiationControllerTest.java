package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.service.PaymentService;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PaymentInitiationControllerTest {

    @Autowired
    PaymentInitiationController paymentInitiationController;
    @Autowired
    PaymentService paymentService;

    @Test
    public void createPaymentInitiation() {
    }

    @Test
    public void getPaymentInitiation() {
    }

}
