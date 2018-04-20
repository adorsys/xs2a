package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.aspspmockserver.repository.PaymentRepository;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public Optional<SpiSinglePayments> addPayment(@NotNull SpiSinglePayments payment) {
        return Optional.ofNullable(paymentRepository.save(payment));
    }

    public double amountToBeCharged(String accountId) {
        List<SpiSinglePayments> payments = paymentRepository.findAll().stream()
                                           .filter(a -> a.getDebtorAccount().getAccountId().equals(accountId))
                                           .collect(Collectors.toList());

        return payments.stream().mapToDouble(a -> Double.parseDouble(a.getInstructedAmount().getContent())).sum();
    }
}
