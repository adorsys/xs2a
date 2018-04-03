package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.CreatePaymentInitiationRequest;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiCreatePaymentRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentMapper {
    public TransactionStatus mapGetPaymentStatusById(SpiTransactionStatus spiTransactionStatus){
        return Optional.ofNullable(spiTransactionStatus)
        .map(ts-> TransactionStatus.valueOf(ts.name()))
        .orElse(null);
    }

    public SpiCreatePaymentRequest mapSpiCreatePaymentRequest(CreatePaymentInitiationRequest createPaymentInitiationRequest) {
        return Optional.ofNullable(createPaymentInitiationRequest)
        .map(paymentRe -> new SpiCreatePaymentRequest(mapSpiTransactionFees(createPaymentInitiationRequest.getTransactionFees()),
            createPaymentInitiationRequest.isTransactionFeeIndicator(),
            new String[0],
            createPaymentInitiationRequest.getPsuMessage(),
            new String[0]))
        .orElse(null);
    }

    private SpiAmount mapSpiTransactionFees(Amount transactionFees) {
        return Optional.ofNullable(transactionFees)
        .map(a -> {
            SpiAmount spitransactionFees = new SpiAmount(a.getCurrency(),a.getContent());
            return spitransactionFees;
        })
        .orElse(null);
    }
}
