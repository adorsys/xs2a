package de.adorsys.aspsp.xs2a.service.mapper;

import de.adorsys.aspsp.xs2a.domain.AccountReference;
import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.address.Address;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class PaymentMapper {

    private final ConsentMapper consentMapper;
    private final AccountMapper accountMapper;

    public TransactionStatus mapGetPaymentStatusById(SpiTransactionStatus spiTransactionStatus) {
        return Optional.ofNullable(spiTransactionStatus)
        .map(ts -> TransactionStatus.valueOf(ts.name()))
        .orElse(null);
    }

    public SpiSinglePayments mapToSpiSinglePayments(SinglePayments paymentInitiationRequest) {
        return Optional.ofNullable(paymentInitiationRequest)
        .map(paymentRe -> {
            SpiSinglePayments spiSinglePayments = new SpiSinglePayments();
            spiSinglePayments.setEndToEndIdentification(paymentRe.getEndToEndIdentification());
            spiSinglePayments.setDebtorAccount(mapAccountReference(paymentRe.getDebtorAccount()));
            spiSinglePayments.setUltimateDebtor(paymentRe.getUltimateDebtor());
            spiSinglePayments.setInstructedAmount(mapAmount(paymentRe.getInstructedAmount()));
            spiSinglePayments.setCreditorAccount(mapAccountReference(paymentRe.getCreditorAccount()));
            spiSinglePayments.setCreditorAgent(paymentRe.getCreditorAgent().getCode());
            spiSinglePayments.setCreditorName(paymentRe.getCreditorName());
            spiSinglePayments.setCreditorAddress(mapToSpiAddress(paymentRe.getCreditorAddress()));
            spiSinglePayments.setUltimateCreditor(paymentRe.getUltimateCreditor());
            spiSinglePayments.setPurposeCode(paymentRe.getPurposeCode().getCode());
            spiSinglePayments.setRemittanceInformationUnstructured(paymentRe.getRemittanceInformationUnstructured());
            spiSinglePayments.setRemittanceInformationStructured(mapToSpiRemittance(paymentRe.getRemittanceInformationStructured()));
            spiSinglePayments.setRequestedExecutionDate(paymentRe.getRequestedExecutionDate());
            spiSinglePayments.setRequestedExecutionTime(paymentRe.getRequestedExecutionTime());

            return spiSinglePayments;
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
                   spiPeriodicPayment.setCreditorAddress(mapToSpiAddress(pp.getCreditorAddress()));
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

    public PaymentInitialisationResponse mapFromSpiPaymentInitializationResponse(SpiPaymentInitialisationResponse response) {

        return Optional.ofNullable(response)
               .map(pir -> {
                   PaymentInitialisationResponse initialisationResponse = new PaymentInitialisationResponse();
                   initialisationResponse.setTransactionStatus(consentMapper.mapFromSpiTransactionStatus(pir.getTransactionStatus()));
                   initialisationResponse.set_links(new Links());
                   return initialisationResponse;
               }).orElse(null); //TODO Fill in th Linx
    }

    private SpiAddress mapToSpiAddress(Address address) {
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

    public List<SpiSinglePayment> mapToSpiSinglePaymentList(List<SinglePayments> payments) {

        return payments.stream().map(pt -> new SpiSinglePayment(pt.getEndToEndIdentification(),
        consentMapper.mapToSpiAccountReference(pt.getDebtorAccount()),
        pt.getUltimateDebtor(),
        accountMapper.mapToSpiAmount(pt.getInstructedAmount()),
        consentMapper.mapToSpiAccountReference(pt.getCreditorAccount()),
        pt.getCreditorAgent().getCode(),
        pt.getCreditorName(),
        pt.getCreditorAddress().toString(), // todo
        pt.getUltimateCreditor(),
        pt.getPurposeCode().getCode(),
        pt.getRemittanceInformationUnstructured(),
        pt.getRemittanceInformationStructured().toString(), //todo
        pt.getRequestedExecutionDate())).collect(Collectors.toList());
    }

    public PaymentInitiation mapFromSpiPaymentInitiation(SpiPaymentInitiation spiPayment) {

        return new PaymentInitiation(
        consentMapper.mapFromSpiTransactionStatus(spiPayment.getSpiTransactionStatus()),
        spiPayment.getPaymentId(),
        accountMapper.mapFromSpiAmount(spiPayment.getSpiTransactionFees()),
        spiPayment.isSpiTransactionFeeIndicator(),
        null,
        spiPayment.getPsuMessage(),
        null,
        true);
    }
}
