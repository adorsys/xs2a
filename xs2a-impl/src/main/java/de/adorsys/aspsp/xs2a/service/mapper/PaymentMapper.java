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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.Xs2aAmount;
import de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus;
import de.adorsys.aspsp.xs2a.domain.address.Xs2aAddress;
import de.adorsys.aspsp.xs2a.domain.address.Xs2aCountryCode;
import de.adorsys.aspsp.xs2a.domain.code.Xs2aFrequencyCode;
import de.adorsys.aspsp.xs2a.domain.code.Xs2aPurposeCode;
import de.adorsys.aspsp.xs2a.domain.consent.AuthenticationObject;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class PaymentMapper {
    private final ObjectMapper objectMapper;
    private final AccountMapper accountMapper;

    public Xs2aTransactionStatus mapToTransactionStatus(SpiTransactionStatus spiTransactionStatus) {
        return Optional.ofNullable(spiTransactionStatus)
                   .map(ts -> Xs2aTransactionStatus.valueOf(ts.name()))
                   .orElse(null);
    }

    public List<SpiSinglePayment> mapToSpiSinglePaymentList(List<SinglePayment> payments) {
        return payments.stream()
                   .map(this::mapToSpiSinglePayment)
                   .collect(Collectors.toList());
    }

    public SpiSinglePayment mapToSpiSinglePayment(SinglePayment paymentInitiationRequest) {
        return Optional.ofNullable(paymentInitiationRequest)
                   .map(pr -> {
                       SpiSinglePayment spiSinglePayment = new SpiSinglePayment();
                       spiSinglePayment.setEndToEndIdentification(pr.getEndToEndIdentification());
                       spiSinglePayment.setDebtorAccount(accountMapper.mapToSpiAccountReference(pr.getDebtorAccount()));
                       spiSinglePayment.setUltimateDebtor(pr.getUltimateDebtor());
                       spiSinglePayment.setInstructedAmount(mapToSpiAmount(pr.getInstructedAmount()));
                       spiSinglePayment.setCreditorAccount(accountMapper.mapToSpiAccountReference(pr.getCreditorAccount()));

                       spiSinglePayment.setCreditorAgent(pr.getCreditorAgent());
                       spiSinglePayment.setCreditorName(pr.getCreditorName());
                       spiSinglePayment.setCreditorAddress(mapToSpiAddress(pr.getCreditorAddress()));
                       spiSinglePayment.setUltimateCreditor(pr.getUltimateCreditor());
                       spiSinglePayment.setPurposeCode(Optional.ofNullable(pr.getPurposeCode())
                                                           .map(Xs2aPurposeCode::getCode).orElse(""));
                       spiSinglePayment.setRemittanceInformationUnstructured(pr.getRemittanceInformationUnstructured());
                       spiSinglePayment.setRemittanceInformationStructured(mapToSpiRemittance(pr.getRemittanceInformationStructured()));
                       spiSinglePayment.setRequestedExecutionDate(pr.getRequestedExecutionDate());
                       spiSinglePayment.setRequestedExecutionTime(pr.getRequestedExecutionTime());
                       spiSinglePayment.setPaymentStatus(SpiTransactionStatus.RCVD);

                       return spiSinglePayment;
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
                       spiPeriodicPayment.setInstructedAmount(mapToSpiAmount(pp.getInstructedAmount()));
                       spiPeriodicPayment.setCreditorAccount(accountMapper.mapToSpiAccountReference(pp.getCreditorAccount()));
                       spiPeriodicPayment.setCreditorAgent(pp.getCreditorAgent());
                       spiPeriodicPayment.setCreditorName(pp.getCreditorName());
                       spiPeriodicPayment.setCreditorAddress(mapToSpiAddress(pp.getCreditorAddress()));
                       spiPeriodicPayment.setUltimateCreditor(pp.getUltimateCreditor());
                       spiPeriodicPayment.setPurposeCode(Optional.ofNullable(pp.getPurposeCode())
                                                             .map(Xs2aPurposeCode::getCode)
                                                             .orElse(null));
                       spiPeriodicPayment.setRemittanceInformationUnstructured(pp.getRemittanceInformationUnstructured());
                       spiPeriodicPayment.setRemittanceInformationStructured(mapToSpiRemittance(pp.getRemittanceInformationStructured()));
                       spiPeriodicPayment.setRequestedExecutionDate(pp.getRequestedExecutionDate());
                       spiPeriodicPayment.setRequestedExecutionTime(pp.getRequestedExecutionTime());
                       spiPeriodicPayment.setStartDate(pp.getStartDate());
                       spiPeriodicPayment.setExecutionRule(pp.getExecutionRule());
                       spiPeriodicPayment.setEndDate(pp.getEndDate());
                       spiPeriodicPayment.setFrequency(Optional.ofNullable(pp.getFrequency())
                                                           .map(Enum::name)
                                                           .orElse(null));
                       spiPeriodicPayment.setDayOfExecution(pp.getDayOfExecution());
                       spiPeriodicPayment.setPaymentStatus(SpiTransactionStatus.RCVD);

                       return spiPeriodicPayment;

                   }).orElse(null);
    }

    public PaymentInitialisationResponse mapToPaymentInitializationResponse(SpiPaymentInitialisationResponse response) {
        return Optional.ofNullable(response)
                   .map(pir -> {
                       PaymentInitialisationResponse initialisationResponse = new PaymentInitialisationResponse();
                       initialisationResponse.setTransactionStatus(mapToTransactionStatus(pir.getTransactionStatus()));
                       initialisationResponse.setPaymentId(pir.getPaymentId());
                       initialisationResponse.setTransactionFees(accountMapper.mapToAmount(pir.getSpiTransactionFees()));
                       initialisationResponse.setTransactionFeeIndicator(pir.isSpiTransactionFeeIndicator());
                       initialisationResponse.setPsuMessage(pir.getPsuMessage());
                       initialisationResponse.setTppRedirectPreferred(pir.isTppRedirectPreferred());
                       initialisationResponse.setScaMethods(mapToAuthenticationObjects(pir.getScaMethods()));
                       initialisationResponse.setTppMessages(mapToMessageErrorCodes(pir.getTppMessages()));
                       initialisationResponse.setLinks(new Links());
                       return initialisationResponse;
                   }).orElse(new PaymentInitialisationResponse());
    }

    public PaymentInitialisationResponse mapToPaymentInitResponseFailedPayment(SinglePayment payment, MessageErrorCode error) {
        log.warn("Payment initiation has an error: {}. Payment : {}", error, payment);
        PaymentInitialisationResponse response = new PaymentInitialisationResponse();
        response.setTransactionStatus(Xs2aTransactionStatus.RJCT);
        response.setPaymentId(payment.getEndToEndIdentification());
        response.setTppMessages(new MessageErrorCode[]{error});
        response.setTransactionFees(null); //Not Present in 1.1 payment entity
        response.setTransactionFeeIndicator(false); //Not Present in 1.1 payment entity
        response.setScaMethods(null); //Not Present in 1.1 payment entity
        response.setPsuMessage(null);
        response.setLinks(null); //Not Present in 1.1 payment entity
        response.setTppRedirectPreferred(false); //Not Present in 1.1 payment entity
        return response;
    }

    public SpiPaymentType mapToSpiPaymentType(PaymentType paymentType) {
        return SpiPaymentType.valueOf(paymentType.name());
    }

    public SinglePayment mapToSinglePayment(SpiSinglePayment spiSinglePayment) {
        return Optional.ofNullable(spiSinglePayment)
                   .map(sp -> {
                       SinglePayment payments = new SinglePayment();
                       payments.setEndToEndIdentification(sp.getEndToEndIdentification());
                       payments.setDebtorAccount(accountMapper.mapToAccountReference(sp.getDebtorAccount()));
                       payments.setUltimateDebtor(sp.getUltimateDebtor());
                       payments.setInstructedAmount(accountMapper.mapToAmount(sp.getInstructedAmount()));
                       payments.setCreditorAccount(accountMapper.mapToAccountReference(sp.getCreditorAccount()));
                       payments.setCreditorAgent(sp.getCreditorAgent());
                       payments.setCreditorName(sp.getCreditorName());
                       payments.setCreditorAddress(mapToAddress(sp.getCreditorAddress()));
                       payments.setUltimateCreditor(sp.getUltimateCreditor());
                       payments.setPurposeCode(mapToPurposeCode(sp.getPurposeCode()));
                       payments.setRemittanceInformationUnstructured(sp.getRemittanceInformationUnstructured());
                       payments.setRemittanceInformationStructured(mapToRemittance(sp.getRemittanceInformationStructured()));
                       payments.setRequestedExecutionDate(sp.getRequestedExecutionDate());
                       payments.setRequestedExecutionTime(sp.getRequestedExecutionTime());
                       payments.setTransactionStatus(mapToTransactionStatus(spiSinglePayment.getPaymentStatus()));
                       return payments;
                   })
                   .orElse(null);
    }

    public PeriodicPayment mapToPeriodicPayment(SpiPeriodicPayment spiPeriodicPayment) {
        return Optional.ofNullable(spiPeriodicPayment).map(sp -> {
            PeriodicPayment payment = new PeriodicPayment();
            payment.setEndToEndIdentification(sp.getEndToEndIdentification());
            payment.setDebtorAccount(accountMapper.mapToAccountReference(sp.getDebtorAccount()));
            payment.setUltimateDebtor(sp.getUltimateDebtor());
            payment.setInstructedAmount(accountMapper.mapToAmount(sp.getInstructedAmount()));
            payment.setCreditorAccount(accountMapper.mapToAccountReference(sp.getCreditorAccount()));
            payment.setCreditorAgent(sp.getCreditorAgent());
            payment.setCreditorName(sp.getCreditorName());
            payment.setCreditorAddress(mapToAddress(sp.getCreditorAddress()));
            payment.setUltimateCreditor(sp.getUltimateCreditor());
            payment.setPurposeCode(mapToPurposeCode(sp.getPurposeCode()));
            payment.setRemittanceInformationUnstructured(sp.getRemittanceInformationUnstructured());
            payment.setRemittanceInformationStructured(mapToRemittance(sp.getRemittanceInformationStructured()));
            payment.setRequestedExecutionDate(sp.getRequestedExecutionDate());
            payment.setRequestedExecutionTime(sp.getRequestedExecutionTime());
            payment.setExecutionRule(sp.getExecutionRule());
            payment.setFrequency(Xs2aFrequencyCode.valueOf(sp.getFrequency()));
            payment.setDayOfExecution(sp.getDayOfExecution());
            payment.setEndDate(sp.getEndDate());
            payment.setStartDate(sp.getStartDate());
            payment.setTransactionStatus(mapToTransactionStatus(spiPeriodicPayment.getPaymentStatus()));
            return payment;
        })
                   .orElse(null);
    }

    public List<SinglePayment> mapToBulkPayment(List<SpiSinglePayment> spiSinglePayment) {
        return CollectionUtils.isNotEmpty(spiSinglePayment)
                   ? spiSinglePayment.stream().map(this::mapToSinglePayment).collect(Collectors.toList())
                   : null;
    }

    private AuthenticationObject[] mapToAuthenticationObjects(String[] authObjects) { //NOPMD TODO review and check PMD assertion https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/115
        return new AuthenticationObject[]{};//TODO Fill in th Linx
    }

    private MessageErrorCode[] mapToMessageErrorCodes(String[] messageCodes) { //NOPMD TODO review and check PMD assertion https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/115
        return Optional.ofNullable(messageCodes)
                   .map(codes -> Arrays.stream(codes)
                                     .map(MessageErrorCode::valueOf)
                                     .toArray(MessageErrorCode[]::new))
                   .orElse(new MessageErrorCode[]{});
    }

    private SpiAddress mapToSpiAddress(Xs2aAddress address) {
        return Optional.ofNullable(address)
                   .map(a -> new SpiAddress(
                       a.getStreet(),
                       a.getBuildingNumber(),
                       a.getCity(),
                       a.getPostalCode(),
                       Optional.ofNullable(a.getCountry()).map(Xs2aCountryCode::getCode).orElse("")))
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

    private Xs2aPurposeCode mapToPurposeCode(String purposeCode) {
        return Optional.ofNullable(purposeCode)
                   .map(p -> {
                       Xs2aPurposeCode code = new Xs2aPurposeCode();
                       code.setCode(p);
                       return code;
                   })
                   .orElseGet(Xs2aPurposeCode::new);
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
                   .orElseGet(Remittance::new);
    }

    private Xs2aAddress mapToAddress(SpiAddress creditorAddress) {
        return Optional.ofNullable(creditorAddress)
                   .map(a -> {
                       Xs2aAddress address = new Xs2aAddress();
                       Xs2aCountryCode code = new Xs2aCountryCode();
                       code.setCode(Optional.ofNullable(a.getCountry()).orElse(null));
                       address.setCountry(code);
                       address.setPostalCode(a.getPostalCode());
                       address.setCity(a.getCity());
                       address.setStreet(a.getStreet());
                       address.setBuildingNumber(a.getBuildingNumber());
                       return address;
                   })
                   .orElseGet(Xs2aAddress::new);

    }

    private SpiAmount mapToSpiAmount(Xs2aAmount amount) {
        return Optional.ofNullable(amount)
                   .map(am -> new SpiAmount(am.getCurrency(), new BigDecimal(am.getAmount())))
                   .orElse(null);
    }

    public TppInfo mapToTppInfo(String tppSignatureCertificate) {
        if (StringUtils.isBlank(tppSignatureCertificate)) {
            return null;
        }

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(tppSignatureCertificate);
            String decodedJson = new String(decodedBytes);

            return objectMapper.readValue(decodedJson, TppInfo.class);
        } catch (Exception e) {
            log.warn("Error with converting TppInfo from certificate {}", tppSignatureCertificate);
            return null;
        }
    }
}
