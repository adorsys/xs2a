package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.AccountReference;
import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentMapper {
    public TransactionStatus mapGetPaymentStatusById(SpiTransactionStatus spiTransactionStatus){
        return Optional.ofNullable(spiTransactionStatus)
        .map(ts-> TransactionStatus.valueOf(ts.name()))
        .orElse(null);
    }

    public SpiSinglePayments mapSinlePayments(SinglePayments paymentInitiationRequest) {
        return Optional.ofNullable(paymentInitiationRequest)
        .map(paymentRe -> {
            SpiSinglePayments spiSinglePayments = new SpiSinglePayments(
                paymentRe.getEndToEndIdentification(),
                mapAccountReference(paymentRe.getDebtorAccount()),
                paymentRe.getUltimateDebtor(),
                mapAmount(paymentRe.getInstructedAmount()),
                mapAccountReference(paymentRe.getCreditorAccount()),
                paymentRe.getCreditorAgent().toString(),
                paymentRe.getCreditorName(),
                paymentRe.getCreditorAddress().toString(),
                paymentRe.getUltimateCreditor(),
                paymentRe.getPurposeCode().getCode(),
                paymentRe.getRemittanceInformationUnstructured(),
                "",
                paymentRe.getRequestedExecutionDate(),
                paymentRe.getRequestedExecutionTime()
            );
            return spiSinglePayments;
        })
            .orElse(null);
    }

    private SpiAmount mapAmount(Amount amount) {
        return Optional.ofNullable(amount)
        .map(a -> {
            SpiAmount spiAmount = new SpiAmount(
                amount.getCurrency(),
                amount.getContent()
            );
            return spiAmount;
        })
        .orElse(null);
    }

    private SpiAccountReference mapAccountReference(AccountReference accountReference) {
        return Optional.ofNullable(accountReference)
        .map(ar -> {
            SpiAccountReference spiAccountReference = new SpiAccountReference(
            ar.getAccountId(),
            ar.getIban(),
            ar.getBban(),
            ar.getPan(),
            ar.getMaskedPan(),
            ar.getMsisdn(),
            ar.getCurrency());
            return spiAccountReference;
        })
        .orElse(null);

    }
}
