package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitiation;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitiation;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentMapper {

   public List<SpiSinglePayment> mapToSpiSinglePaymentList(List<SinglePayments> payments){
        return null;
    }

    public SpiSinglePayment mapToSpiSinglePayment(SinglePayments payment){
        return null;
    }

    public List<SinglePayments> mapFromSpiSinglePaymentList(List<SpiSinglePayment> payments){
        return null;
    }

    public SinglePayments mapFromSpiSinglePayment(SpiSinglePayment payment){
        return null;
    }

    public PaymentInitiation mapFromSpiPaymentInitiation(SpiPaymentInitiation payment){
        return null;
    }
}
