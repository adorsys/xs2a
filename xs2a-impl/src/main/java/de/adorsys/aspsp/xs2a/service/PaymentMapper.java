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
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentMapper {
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
                   spiPeriodicPayment.setDebtorAccount(consentMapper.mapSpiAccountReference(pp.getDebtorAccount()));
                   spiPeriodicPayment.setUltimateDebtor(pp.getUltimateDebtor());
                   spiPeriodicPayment.setInstructedAmount(accountMapper.mapToSpiAmount(pp.getInstructedAmount()));
                   spiPeriodicPayment.setCreditorAccount(consentMapper.mapSpiAccountReference(pp.getCreditorAccount()));
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
                   initialisationResponse.setTransaction_status(TransactionStatus.valueOf(pir.getTransaction_status()));
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
