package de.adorsys.aspsp.xs2a.service.mapper;

import de.adorsys.aspsp.xs2a.domain.AccountReference;
import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitiation;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitiation;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PaymentMapper {

    public List<SpiSinglePayment> mapToSpiSinglePaymentList(List<SinglePayments> payments) {

        return payments.stream().map(pt -> new SpiSinglePayment(pt.getEndToEndIdentification(),
        mapToSpiAccountReference(pt.getDebtorAccount()),
        pt.getUltimateDebtor(),
        mapToSpiAmount(pt.getInstructedAmount()),
        mapToSpiAccountReference(pt.getCreditorAccount()),
        pt.getCreditorAgent().getCode(),
        pt.getCreditorName(),
        pt.getCreditorAddress().toString(), // todo
        pt.getUltimateCreditor(),
        pt.getPurposeCode().getCode(),
        pt.getRemittanceInformationUnstructured(),
        pt.getRemittanceInformationStructured().toString(), //todo
        pt.getRequestedExecutionDate())).collect(Collectors.toList());
    }

    private SpiAccountReference mapToSpiAccountReference(AccountReference reference) {
        return Optional.of(reference)
               .map(re -> new SpiAccountReference(
               reference.getAccountId(),
               reference.getIban(),
               reference.getBban(),
               reference.getPan(),
               reference.getMaskedPan(),
               reference.getMsisdn(),
               reference.getCurrency())).orElse(null);
    }


    private Amount mapFromSpiAmount(SpiAmount spiAmount) {
        return Optional.ofNullable(spiAmount)
               .map(a -> {
                   Amount amount = new Amount();
                   amount.setContent(a.getContent());
                   amount.setCurrency(a.getCurrency());
                   return amount;
               })
               .orElse(null);
    }

    private SpiAmount mapToSpiAmount(Amount amount) {
        return Optional.ofNullable(amount)
               .map(a -> new SpiAmount(a.getCurrency(), a.getContent()))
               .orElse(null);
    }


    public SpiSinglePayment mapToSpiSinglePayment(SinglePayments payment) {
        return null;
    }

    public List<SinglePayments> mapFromSpiSinglePaymentList(List<SpiSinglePayment> payments) {
        return null;
    }

    public SinglePayments mapFromSpiSinglePayment(SpiSinglePayment payment) {
        return null;
    }

    public PaymentInitiation mapFromSpiPaymentInitiation(SpiPaymentInitiation payment) {
        return null;
    }
}
