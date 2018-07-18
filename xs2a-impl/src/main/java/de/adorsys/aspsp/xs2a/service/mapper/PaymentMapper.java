/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.aspsp.xs2a.service.mapper;

import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.address.Address;
import de.adorsys.aspsp.xs2a.domain.address.CountryCode;
import de.adorsys.aspsp.xs2a.domain.code.BICFI;
import de.adorsys.aspsp.xs2a.domain.code.FrequencyCode;
import de.adorsys.aspsp.xs2a.domain.code.PurposeCode;
import de.adorsys.aspsp.xs2a.domain.consent.AuthenticationObject;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.*;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class PaymentMapper {

    private final ConsentMapper consentMapper;
    private final AccountMapper accountMapper;

    public TransactionStatus mapToTransactionStatus(SpiTransactionStatus spiTransactionStatus) {
        return Optional.ofNullable(spiTransactionStatus)
                   .map(ts -> TransactionStatus.valueOf(ts.name()))
                   .orElse(null);
    }

    public SpiSinglePayments mapToSpiSinglePayments(SinglePayments paymentInitiationRequest) {
        return Optional.ofNullable(paymentInitiationRequest)
                   .map(paymentRe -> {
                       SpiSinglePayments spiSinglePayments = new SpiSinglePayments();
                       spiSinglePayments.setEndToEndIdentification(paymentRe.getEndToEndIdentification());
                       spiSinglePayments.setDebtorAccount(accountMapper.mapToSpiAccountReference(paymentRe.getDebtorAccount()));
                       spiSinglePayments.setUltimateDebtor(paymentRe.getUltimateDebtor());
                       spiSinglePayments.setInstructedAmount(accountMapper.mapToSpiAmount(paymentRe.getInstructedAmount()));
                       spiSinglePayments.setCreditorAccount(accountMapper.mapToSpiAccountReference(paymentRe.getCreditorAccount()));

                       spiSinglePayments.setCreditorAgent(Optional.ofNullable(paymentRe.getCreditorAgent())
                                                              .map(BICFI::getCode).orElse(""));
                       spiSinglePayments.setCreditorName(paymentRe.getCreditorName());
                       spiSinglePayments.setCreditorAddress(mapToSpiAddress(paymentRe.getCreditorAddress()));
                       spiSinglePayments.setUltimateCreditor(paymentRe.getUltimateCreditor());
                       spiSinglePayments.setPurposeCode(Optional.ofNullable(paymentRe.getPurposeCode())
                                                            .map(PurposeCode::getCode).orElse(""));
                       spiSinglePayments.setRemittanceInformationUnstructured(paymentRe.getRemittanceInformationUnstructured());
                       spiSinglePayments.setRemittanceInformationStructured(mapToSpiRemittance(paymentRe.getRemittanceInformationStructured()));
                       spiSinglePayments.setRequestedExecutionDate(paymentRe.getRequestedExecutionDate());
                       spiSinglePayments.setRequestedExecutionTime(paymentRe.getRequestedExecutionTime());
                       spiSinglePayments.setPaymentStatus(SpiTransactionStatus.RCVD);

                       return spiSinglePayments;
                   })
                   .orElse(null);
    }

    public SpiPeriodicPayment mapToSpiPeriodicPayment(PeriodicPayment periodicPayment) {
        return Optional.ofNullable(periodicPayment)
                   .map(pp -> {
                       SpiPeriodicPayment spiPeriodicPayment = new SpiPeriodicPayment();
                       spiPeriodicPayment.setEndToEndIdentification(pp.getEndToEndIdentification());
                       spiPeriodicPayment.setDebtorAccount(accountMapper.mapToSpiAccountReference(pp.getDebtorAccount()));
                       spiPeriodicPayment.setUltimateDebtor(pp.getUltimateDebtor());
                       spiPeriodicPayment.setInstructedAmount(accountMapper.mapToSpiAmount(pp.getInstructedAmount()));
                       spiPeriodicPayment.setCreditorAccount(accountMapper.mapToSpiAccountReference(pp.getCreditorAccount()));
                       spiPeriodicPayment.setCreditorAgent(getCreditorAgentCode(pp));
                       spiPeriodicPayment.setCreditorName(pp.getCreditorName());
                       spiPeriodicPayment.setCreditorAddress(mapToSpiAddress(pp.getCreditorAddress()));
                       spiPeriodicPayment.setUltimateCreditor(pp.getUltimateCreditor());
                       spiPeriodicPayment.setPurposeCode(getPurposeCode(pp));
                       spiPeriodicPayment.setRemittanceInformationUnstructured(pp.getRemittanceInformationUnstructured());
                       spiPeriodicPayment.setRemittanceInformationStructured(mapToSpiRemittance(pp.getRemittanceInformationStructured()));
                       spiPeriodicPayment.setRequestedExecutionDate(pp.getRequestedExecutionDate());
                       spiPeriodicPayment.setRequestedExecutionTime(pp.getRequestedExecutionTime());
                       spiPeriodicPayment.setStartDate(pp.getStartDate());
                       spiPeriodicPayment.setExecutionRule(pp.getExecutionRule());
                       spiPeriodicPayment.setEndDate(pp.getEndDate());
                       spiPeriodicPayment.setFrequency(getFrequency(pp));
                       spiPeriodicPayment.setDayOfExecution(pp.getDayOfExecution());
                       spiPeriodicPayment.setPaymentStatus(SpiTransactionStatus.RCVD);

                       return spiPeriodicPayment;

                   }).orElse(null);
    }

    public Optional<PaymentInitialisationResponse> mapToPaymentInitializationResponse(SpiPaymentInitialisationResponse response) {
        return Optional.ofNullable(response)
                   .map(pir -> {
                       PaymentInitialisationResponse initialisationResponse = new PaymentInitialisationResponse();
                       initialisationResponse.setTransactionStatus(consentMapper.mapToTransactionStatus(pir.getTransactionStatus()));
                       initialisationResponse.setPaymentId(pir.getPaymentId());
                       initialisationResponse.setTransactionFees(accountMapper.mapToAmount(pir.getSpiTransactionFees()));
                       initialisationResponse.setTransactionFeeIndicator(pir.isSpiTransactionFeeIndicator());
                       initialisationResponse.setPsuMessage(pir.getPsuMessage());
                       initialisationResponse.setTppRedirectPreferred(pir.isTppRedirectPreferred());
                       initialisationResponse.setScaMethods(mapToAuthenticationObjects(pir.getScaMethods()));
                       initialisationResponse.setTppMessages(mapToMessageCodes(pir.getTppMessages()));
                       initialisationResponse.setLinks(new Links());
                       return initialisationResponse;
                   });
    }

    public Optional<PaymentInitialisationResponse> mapToPaymentInitResponseFailedPayment(SinglePayments payment, MessageErrorCode error, boolean tppRedirectPreferred) {
        return Optional.ofNullable(payment)
                   .map(p -> {
                       PaymentInitialisationResponse response = new PaymentInitialisationResponse();
                       response.setTransactionStatus(TransactionStatus.RJCT);
                       response.setPaymentId(p.getEndToEndIdentification());
                       response.setTppRedirectPreferred(tppRedirectPreferred);
                       response.setTppMessages(new MessageErrorCode[]{error});
                       return response;
                   });
    }

    String getFrequency(PeriodicPayment pp) {
        return Optional.ofNullable(pp.getFrequency())
                   .map(Enum::name)
                   .orElse(null);
    }

    String getCreditorAgentCode(PeriodicPayment payment) {
        return Optional.ofNullable(payment.getCreditorAgent())
                   .map(BICFI::getCode)
                   .orElse(null);
    }

    String getPurposeCode(PeriodicPayment payment) {
        return Optional.ofNullable(payment.getPurposeCode())
                   .map(PurposeCode::getCode)
                   .orElse(null);
    }

    private AuthenticationObject[] mapToAuthenticationObjects(String[] authObjects) { //NOPMD TODO review and check PMD assertion https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/115
        return new AuthenticationObject[]{};//TODO Fill in th Linx
    }

    private MessageErrorCode[] mapToMessageCodes(String[] messageCodes) { //NOPMD TODO review and check PMD assertion https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/115
        return new MessageErrorCode[]{};//TODO Fill in th Linx
    }

    private SpiAddress mapToSpiAddress(Address address) {
        return Optional.ofNullable(address)
                   .map(a -> new SpiAddress(null, a.getStreet(), a.getBuildingNumber(), a.getCity(), a.getPostalCode(), a.getCountry().toString()))
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

    public List<SpiSinglePayments> mapToSpiSinglePaymentList(List<SinglePayments> payments) {
        return payments.stream()
                   .map(this::mapToSpiSinglePayments)
                   .collect(Collectors.toList());
    }

    public SpiPaymentType mapToSpiPaymentType(PaymentType paymentType) {
        return SpiPaymentType.valueOf(paymentType.name());
    }

    public SinglePayments mapToSinglePayment(SpiSinglePayments spiSinglePayment) {
        return Optional.ofNullable(spiSinglePayment)
                   .map(sp -> {
                       SinglePayments payments = new SinglePayments();
                       payments.setEndToEndIdentification(spiSinglePayment.getEndToEndIdentification());
                       payments.setDebtorAccount(accountMapper.mapToAccountReference(spiSinglePayment.getDebtorAccount()));
                       payments.setUltimateDebtor(spiSinglePayment.getUltimateDebtor());
                       payments.setInstructedAmount(accountMapper.mapToAmount(spiSinglePayment.getInstructedAmount()));
                       payments.setCreditorAccount(accountMapper.mapToAccountReference(spiSinglePayment.getCreditorAccount()));
                       payments.setCreditorAgent(mapToBICFI(spiSinglePayment.getCreditorAgent()));
                       payments.setCreditorName(spiSinglePayment.getCreditorName());
                       payments.setCreditorAddress(mapToAddress(spiSinglePayment.getCreditorAddress()));
                       payments.setUltimateCreditor(spiSinglePayment.getUltimateCreditor());
                       payments.setPurposeCode(mapToPurposeCode(spiSinglePayment.getPurposeCode()));
                       payments.setRemittanceInformationUnstructured(spiSinglePayment.getRemittanceInformationUnstructured());
                       payments.setRemittanceInformationStructured(mapToRemittance(spiSinglePayment.getRemittanceInformationStructured()));
                       payments.setRequestedExecutionDate(spiSinglePayment.getRequestedExecutionDate());
                       payments.setRequestedExecutionTime(spiSinglePayment.getRequestedExecutionTime());
                       return payments;
                   })
                   .orElse(null);
    }

    private PurposeCode mapToPurposeCode(String purposeCode) {
        return Optional.ofNullable(purposeCode)
                   .map(p -> {
                       PurposeCode code = new PurposeCode();
                       code.setCode(purposeCode);
                       return code;
                   })
                   .orElse(new PurposeCode());
    }

    private Remittance mapToRemittance(SpiRemittance remittanceInformationStructured) {
        return Optional.ofNullable(remittanceInformationStructured)
                   .map(r -> {
                       Remittance remittance = new Remittance();
                       remittance.setReference(r.getReference());
                       remittance.setReferenceIssuer(r.getReferenceIssuer());
                       remittance.setReferenceType(r.getReferenceType());
                       return remittance;
                   })
                   .orElse(new Remittance());
    }

    private Address mapToAddress(SpiAddress creditorAddress) {
        return Optional.ofNullable(creditorAddress)
                   .map(a -> {
                       Address address = new Address();
                       CountryCode code = new CountryCode();
                       code.setCode(Optional.ofNullable(a.getCountry()).orElse(null));
                       address.setCountry(code);
                       address.setPostalCode(a.getPostalCode());
                       address.setCity(a.getCity());
                       address.setStreet(a.getStreet());
                       address.setBuildingNumber(a.getBuildingNumber());
                       return address;
                   })
                   .orElse(new Address());

    }

    private BICFI mapToBICFI(String creditorAgent) {
        BICFI bicfi = new BICFI();
        bicfi.setCode(creditorAgent);
        return bicfi;
    }

    public PeriodicPayment mapToPeriodicPayment(SpiPeriodicPayment spiPeriodicPayment) {
        return Optional.ofNullable(spiPeriodicPayment).map(sp -> {
            PeriodicPayment payment = new PeriodicPayment();
            payment.setEndToEndIdentification(sp.getEndToEndIdentification());
            payment.setDebtorAccount(accountMapper.mapToAccountReference(sp.getDebtorAccount()));
            payment.setUltimateDebtor(sp.getUltimateDebtor());
            payment.setInstructedAmount(accountMapper.mapToAmount(sp.getInstructedAmount()));
            payment.setCreditorAccount(accountMapper.mapToAccountReference(sp.getCreditorAccount()));
            payment.setCreditorAgent(mapToBICFI(sp.getCreditorAgent()));
            payment.setCreditorName(sp.getCreditorName());
            payment.setCreditorAddress(mapToAddress(sp.getCreditorAddress()));
            payment.setUltimateCreditor(sp.getUltimateCreditor());
            payment.setPurposeCode(mapToPurposeCode(sp.getPurposeCode()));
            payment.setRemittanceInformationUnstructured(sp.getRemittanceInformationUnstructured());
            payment.setRemittanceInformationStructured(mapToRemittance(sp.getRemittanceInformationStructured()));
            payment.setRequestedExecutionDate(sp.getRequestedExecutionDate());
            payment.setRequestedExecutionTime(sp.getRequestedExecutionTime());
            payment.setExecutionRule(sp.getExecutionRule());
            payment.setFrequency(FrequencyCode.valueOf(sp.getFrequency()));
            payment.setDayOfExecution(sp.getDayOfExecution());
            payment.setEndDate(sp.getEndDate());
            payment.setStartDate(sp.getStartDate());
            return payment;
        })
                   .orElse(null);
    }

    public List<SinglePayments> mapToBulkPayment(List<SpiSinglePayments> spiSinglePayments) {
        return CollectionUtils.isNotEmpty(spiSinglePayments)
                   ? spiSinglePayments.stream().map(this::mapToSinglePayment).collect(Collectors.toList())
                   : null;
    }
}
