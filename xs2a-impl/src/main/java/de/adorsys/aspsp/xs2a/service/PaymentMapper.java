package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import org.springframework.stereotype.Service;

@Service
public class PaymentMapper {

    private PaymentSpi paymentSpi;
    private ConsentMapper consentMapper;

}
