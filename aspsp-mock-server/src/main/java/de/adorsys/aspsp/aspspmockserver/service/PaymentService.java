package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.aspspmockserver.repository.PaymentRepository;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public ResponseEntity<SpiSinglePayments> addPayment(SpiSinglePayments payment) {
        if (payment != null) {
            payment.setPaymentId(generatePaymentId());
            SpiSinglePayments saved = paymentRepository.save(payment);
            if (saved != null) {
                return new ResponseEntity<>(saved, HttpStatus.CREATED);
            }
        }
        return ResponseEntity.badRequest().build();
    }

    private static String generatePaymentId() {
        return UUID.randomUUID().toString();
    }
}
