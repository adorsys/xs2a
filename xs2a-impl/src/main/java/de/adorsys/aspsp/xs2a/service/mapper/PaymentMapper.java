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
import de.adorsys.aspsp.xs2a.domain.MessageCode;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.address.Address;
import de.adorsys.aspsp.xs2a.domain.code.BICFI;
import de.adorsys.aspsp.xs2a.domain.code.PurposeCode;
import de.adorsys.aspsp.xs2a.domain.consent.AuthenticationObject;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.domain.pis.Remittance;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.*;
import lombok.AllArgsConstructor;
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

                       return spiPeriodicPayment;

                   }).orElse(null);
    }

    public PaymentInitialisationResponse mapToPaymentInitializationResponse(SpiPaymentInitialisationResponse response) {

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
                   }).orElse(null);
    }

    private String getFrequency(PeriodicPayment pp) {
        return Optional.ofNullable(pp.getFrequency())
                   .map(Enum::name)
                   .orElse(null);
    }

    private String getCreditorAgentCode(PeriodicPayment payment) {
        return Optional.ofNullable(payment.getCreditorAgent())
                   .map(BICFI::getCode)
                   .orElse(null);
    }

    private String getPurposeCode(PeriodicPayment payment) {
        return Optional.ofNullable(payment.getPurposeCode())
                   .map(PurposeCode::getCode)
                   .orElse(null);
    }

    private AuthenticationObject[] mapToAuthenticationObjects(String[] authObjects) { //NOPMD TODO review and check PMD assertion https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/115
        return new AuthenticationObject[]{};//TODO Fill in th Linx
    }

    private MessageCode[] mapToMessageCodes(String[] messageCodes) { //NOPMD TODO review and check PMD assertion https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/115
        return new MessageCode[]{};//TODO Fill in th Linx
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
        return payments.stream().map(this::mapToSpiSinglePayments)
                   .collect(Collectors.toList());
    }
}
