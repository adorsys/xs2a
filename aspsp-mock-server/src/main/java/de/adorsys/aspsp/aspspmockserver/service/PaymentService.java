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
    private final AccountService accountService;

    public ResponseEntity<SpiSinglePayments> addPayment(SpiSinglePayments payment) {
        if (payment != null && authoriseUser(payment)) {
            payment.setPaymentId(generatePaymentId());
            SpiSinglePayments saved = paymentRepository.save(payment);
            if (saved != null) {
                return new ResponseEntity<>(saved, HttpStatus.CREATED);
            }
        }
        return ResponseEntity.badRequest().build();
    }

    private boolean authoriseUser(SpiSinglePayments payment) {
        String id = Optional.ofNullable(payment.getDebtorAccount().getAccountId()).orElse(null);
        if (!accountService.getAccount(id).equals(Optional.empty())) {
            return true;
        }
        return false;
    }

    private static String generatePaymentId() {
        return UUID.randomUUID().toString();
    }
}
