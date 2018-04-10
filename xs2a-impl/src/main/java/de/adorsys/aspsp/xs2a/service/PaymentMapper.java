package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.address.Address;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.domain.pis.Remittance;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiAddress;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiRemittance;
import org.springframework.beans.factory.annotation.Autowired;
import de.adorsys.aspsp.xs2a.domain.AccountReference;
import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.address.Address;
import de.adorsys.aspsp.xs2a.domain.pis.Remittance;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAddress;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiRemittance;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentMapper {
    public TransactionStatus mapGetPaymentStatusById(SpiTransactionStatus spiTransactionStatus) {
        return Optional.ofNullable(spiTransactionStatus)
        .map(ts -> TransactionStatus.valueOf(ts.name()))
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
            paymentRe.getCreditorAgent().getCode(),
            paymentRe.getCreditorName(),
            mapAddress(paymentRe.getCreditorAddress()),
            paymentRe.getUltimateCreditor(),
            paymentRe.getPurposeCode().getCode(),
            paymentRe.getRemittanceInformationUnstructured(),
            mapRemittance(paymentRe.getRemittanceInformationStructured()),
            paymentRe.getRequestedExecutionDate(),
            paymentRe.getRequestedExecutionTime()
            );
            return spiSinglePayments;
        })
        .orElse(null);
    }

    private SpiRemittance mapRemittance(Remittance remittance) {
        return Optional.ofNullable(remittance)
        .map(s -> {
            SpiRemittance spiRemittance = new SpiRemittance(
            s.getReference(),
            s.getReferenceType(),
            s.getReferenceIssuer()
            );
            return spiRemittance;
        })
        .orElse(null);
    }

    private SpiAddress mapAddress(Address address) {
        return Optional.ofNullable(address)
        .map(a -> {
            SpiAddress spiAddress = new SpiAddress(
            a.getStreet(),
            a.getBuildingNumber(),
            a.getCity(),
            a.getPostalCode(),
            a.getCountry().getCode()
            );
            return spiAddress;
        })
        .orElse(null);
    }

    private SpiAmount mapAmount(Amount amount) {
        return Optional.ofNullable(amount)
        .map(a -> {
            SpiAmount spiAmount = new SpiAmount(
            a.getCurrency(),
            a.getContent()
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

    private ConsentMapper consentMapper;
    private AccountMapper accountMapper;

    @Autowired
    public PaymentMapper(ConsentMapper consentMapper, AccountMapper accountMapper) {
        this.consentMapper = consentMapper;
        this.accountMapper = accountMapper;
    }

    public SpiPeriodicPayment mapToSpiPeriodicPayment(PeriodicPayment periodicPayment) {
        return Optional.ofNullable(periodicPayment)
               .map(pp -> {
                   SpiPeriodicPayment spiPeriodicPayment = new SpiPeriodicPayment();
                   spiPeriodicPayment.setEndToEndIdentification(pp.getEndToEndIdentification());
                   spiPeriodicPayment.setDebtorAccount(consentMapper.mapToSpiAccountReference(pp.getDebtorAccount()));
                   spiPeriodicPayment.setUltimateDebtor(pp.getUltimateDebtor());
                   spiPeriodicPayment.setInstructedAmount(accountMapper.mapToSpiAmount(pp.getInstructedAmount()));
                   spiPeriodicPayment.setCreditorAccount(consentMapper.mapToSpiAccountReference(pp.getCreditorAccount()));
                   spiPeriodicPayment.setCreditorAgent(pp.getCreditorAgent() != null ? pp.getCreditorAgent().getCode() : null);
                   spiPeriodicPayment.setCreditorName(pp.getCreditorName());
                   spiPeriodicPayment.setCreditorAddress(mapToSpiAdress(pp.getCreditorAddress()));
                   spiPeriodicPayment.setUltimateCreditor(pp.getUltimateCreditor());
                   spiPeriodicPayment.setPurposeCode(pp.getPurposeCode() != null ? pp.getPurposeCode().getCode() : null);
                   spiPeriodicPayment.setRemittanceInformationUnstructured(pp.getRemittanceInformationUnstructured());
                   spiPeriodicPayment.setRemittanceInformationStructured(mapToSpiRemittance(pp.getRemittanceInformationStructured()));
                   spiPeriodicPayment.setRequestedExecutionDate(pp.getRequestedExecutionDate());
                   spiPeriodicPayment.setRequestedExecutionTime(pp.getRequestedExecutionTime());

                   spiPeriodicPayment.setStartDate(pp.getStartDate());
                   spiPeriodicPayment.setExecutionRule(pp.getExecutionRule());
                   spiPeriodicPayment.setEndDate(pp.getEndDate());
                   spiPeriodicPayment.setFrequency(pp.getFrequency() != null ? pp.getFrequency().name() : null);
                   spiPeriodicPayment.setDayOfExecution(pp.getDayOfExecution());

                   return spiPeriodicPayment;

               }).orElse(null);
    }

    public PaymentInitialisationResponse mapFromSpiPaymentInitializationResponsepaymentSpi(SpiPaymentInitialisationResponse response) {

        return Optional.ofNullable(response)
               .map(pir -> {
                   PaymentInitialisationResponse initialisationResponse = new PaymentInitialisationResponse();
                   initialisationResponse.setTransaction_status(TransactionStatus.valueOf(pir.getTransactionStatus()));
                   initialisationResponse.set_links(new Links());
                   return initialisationResponse;
               }).orElse(null); //TODO Fill in th Linx
    }

    private SpiAddress mapToSpiAdress(Address address) {
        return Optional.ofNullable(address)
               .map(a -> new SpiAddress(a.getStreet(), a.getBuildingNumber(), a.getCity(), a.getPostalCode(), a.getCountry().toString()))
               .orElse(null);
    }

    private SpiRemittance mapToSpiRemittance(Remittance remittance) {
        return Optional.ofNullable(remittance)
               .map(r -> {
                   SpiRemittance spiRemittance = new SpiRemittance();
                   spiRemittance.setReference(r.getReference());
                   spiRemittance.setReferenceType(r.getReferenceType());
                   spiRemittance.setReferenceIssuer(r.getReferenceIssuer());
                   return spiRemittance;
               }).orElse(null);
    }
}
