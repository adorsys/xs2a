package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentMapper {
    public TransactionStatus mapGetPaymentStatusById(SpiTransactionStatus spiTransactionStatus){
        return Optional.ofNullable(spiTransactionStatus)
        .map(ts-> TransactionStatus.valueOf(ts.name()))
        .orElse(null);
    }
}
