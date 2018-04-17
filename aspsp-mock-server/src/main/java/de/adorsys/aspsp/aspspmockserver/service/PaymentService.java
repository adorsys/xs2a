package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.aspspmockserver.repository.PaymentRepository;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public SpiSinglePayments addPayment(@NotNull SpiSinglePayments payment) {
        return paymentRepository.save(payment);
    }
}
