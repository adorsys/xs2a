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

package de.adorsys.aspsp.xs2a.service.mapper; //NOPMD

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReference;
import de.adorsys.aspsp.xs2a.domain.address.Xs2aAddress;
import de.adorsys.aspsp.xs2a.domain.address.Xs2aCountryCode;
import de.adorsys.aspsp.xs2a.domain.code.Xs2aFrequencyCode;
import de.adorsys.aspsp.xs2a.domain.code.Xs2aPurposeCode;
import de.adorsys.aspsp.xs2a.domain.consent.Xs2aAuthenticationObject;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiXs2aAccountMapper;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.payment.*;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiAddress;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiChallengeData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentType;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiRemittance;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
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
public class PaymentMapper { // NOPMD TODO fix large amount of methods in PaymentMapper https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/333
    // TODO fix high amount of different objects as members denotes a high coupling https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/322
    private final ObjectMapper objectMapper;
    private final SpiXs2aAccountMapper spiXs2aAccountMapper;

    public SpiBulkPayment mapToSpiBulkPayment(BulkPayment bulkPayment) {
        return Optional.ofNullable(bulkPayment)
                   .map(bulk -> {
                       SpiBulkPayment spiBulkPayment = new SpiBulkPayment();
                       spiBulkPayment.setBatchBookingPreferred(bulk.getBatchBookingPreferred());
                       spiBulkPayment.setDebtorAccount(spiXs2aAccountMapper.mapToSpiAccountReference(bulk.getDebtorAccount()));
                       spiBulkPayment.setRequestedExecutionDate(bulk.getRequestedExecutionDate());
                       spiBulkPayment.setPayments(bulk.getPayments().stream()
                                                      .map(this::mapToSpiSinglePayment)
                                                      .collect(Collectors.toList()));
                       spiBulkPayment.setPaymentStatus(mapToSpiTransactionStatus(bulk.getTransactionStatus()));
                       return spiBulkPayment;
                   })
                   .orElse(null);
    }

    public Xs2aTransactionStatus mapToTransactionStatus(SpiTransactionStatus spiTransactionStatus) {
        return Optional.ofNullable(spiTransactionStatus)
                   .map(ts -> Xs2aTransactionStatus.valueOf(ts.name()))
                   .orElse(null);
    }

    private SpiTransactionStatus mapToSpiTransactionStatus(Xs2aTransactionStatus xs2aTransactionStatus) {
        return Optional.ofNullable(xs2aTransactionStatus)
                   .map(ts -> SpiTransactionStatus.valueOf(ts.name()))
                   .orElse(null);
    }

    public SpiSinglePayment mapToSpiSinglePayment(SinglePayment paymentInitiationRequest) {
        return Optional.ofNullable(paymentInitiationRequest)
                   .map(pr -> {
                       SpiSinglePayment spiSinglePayment = new SpiSinglePayment();
                       spiSinglePayment.setEndToEndIdentification(pr.getEndToEndIdentification());
                       spiSinglePayment.setDebtorAccount(spiXs2aAccountMapper.mapToSpiAccountReference(pr.getDebtorAccount()));
                       spiSinglePayment.setUltimateDebtor(pr.getUltimateDebtor());
                       spiSinglePayment.setInstructedAmount(mapToSpiAmount(pr.getInstructedAmount()));
                       spiSinglePayment.setCreditorAccount(spiXs2aAccountMapper.mapToSpiAccountReference(pr.getCreditorAccount()));

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
                       spiPeriodicPayment.setDebtorAccount(spiXs2aAccountMapper.mapToSpiAccountReference(pp.getDebtorAccount()));
                       spiPeriodicPayment.setUltimateDebtor(pp.getUltimateDebtor());
                       spiPeriodicPayment.setInstructedAmount(mapToSpiAmount(pp.getInstructedAmount()));
                       spiPeriodicPayment.setCreditorAccount(spiXs2aAccountMapper.mapToSpiAccountReference(pp.getCreditorAccount()));
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
    //TODO remote AspspConsentData from here https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332
    public PaymentInitialisationResponse mapToPaymentInitializationResponse(SpiPaymentInitialisationResponse response, AspspConsentData aspspConsentData) {
        return Optional.ofNullable(response)
                   .map(pir -> {
                       PaymentInitialisationResponse initialisationResponse = new PaymentInitialisationResponse();
                       initialisationResponse.setTransactionStatus(mapToTransactionStatus(pir.getTransactionStatus()));
                       initialisationResponse.setPaymentId(pir.getPaymentId());
                       initialisationResponse.setTransactionFees(spiXs2aAccountMapper.mapToXs2aAmount(pir.getSpiTransactionFees()));
                       initialisationResponse.setTransactionFeeIndicator(pir.isSpiTransactionFeeIndicator());
                       initialisationResponse.setPsuMessage(pir.getPsuMessage());
                       initialisationResponse.setTppRedirectPreferred(pir.isTppRedirectPreferred());
                       initialisationResponse.setScaMethods(mapToAuthenticationObjects(pir.getScaMethods()));
                       initialisationResponse.setChallengeData(mapToChallengeData(pir.getChallengeData()));
                       initialisationResponse.setTppMessages(mapToMessageErrorCodes(pir.getTppMessages()));
                       initialisationResponse.setLinks(new Links());
                       initialisationResponse.setAspspConsentData(aspspConsentData);
                       return initialisationResponse;
                   }).orElseGet(PaymentInitialisationResponse::new);
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
                       payments.setDebtorAccount(spiXs2aAccountMapper.mapToXs2aAccountReference(sp.getDebtorAccount()));
                       payments.setUltimateDebtor(sp.getUltimateDebtor());
                       payments.setInstructedAmount(spiXs2aAccountMapper.mapToXs2aAmount(sp.getInstructedAmount()));
                       payments.setCreditorAccount(spiXs2aAccountMapper.mapToXs2aAccountReference(sp.getCreditorAccount()));
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
            payment.setDebtorAccount(spiXs2aAccountMapper.mapToXs2aAccountReference(sp.getDebtorAccount()));
            payment.setUltimateDebtor(sp.getUltimateDebtor());
            payment.setInstructedAmount(spiXs2aAccountMapper.mapToXs2aAmount(sp.getInstructedAmount()));
            payment.setCreditorAccount(spiXs2aAccountMapper.mapToXs2aAccountReference(sp.getCreditorAccount()));
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

    public BulkPayment mapToBulkPayment(List<SpiSinglePayment> spiSinglePayments) {
        if (CollectionUtils.isNotEmpty(spiSinglePayments)) {
            BulkPayment bulkPayment = new BulkPayment();
            bulkPayment.setBatchBookingPreferred(false);
            bulkPayment.setDebtorAccount(getDebtorAccountForBulkPayment(spiSinglePayments));
            bulkPayment.setPayments(spiSinglePayments.stream()
                                        .map(this::mapToSinglePayment)
                                        .collect(Collectors.toList()));
            return bulkPayment;
        }
        return null;
    }

    private Xs2aAccountReference getDebtorAccountForBulkPayment(List<SpiSinglePayment> spiSinglePayments) {
        return spiXs2aAccountMapper.mapToXs2aAccountReference(spiSinglePayments.get(0).getDebtorAccount());
    }

    private Xs2aAuthenticationObject[] mapToAuthenticationObjects(String[] authObjects) { //NOPMD TODO review and check PMD assertion https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/115
        return new Xs2aAuthenticationObject[]{};//TODO Fill in th Linx
    }

    private MessageErrorCode[] mapToMessageErrorCodes(String[] messageCodes) { //NOPMD TODO review and check PMD assertion https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/115
        return Optional.ofNullable(messageCodes)
                   .map(codes -> Arrays.stream(codes)
                                     .map(MessageErrorCode::valueOf)
                                     .toArray(MessageErrorCode[]::new))
                   .orElseGet(() -> new MessageErrorCode[]{});
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
                       address.setCountry(new Xs2aCountryCode(a.getCountry()));
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

    private Xs2aChallengeData mapToChallengeData(SpiChallengeData challengeData) {
        return Optional.ofNullable(challengeData)
                   .map(c -> new Xs2aChallengeData(
                       c.getImage(),
                       c.getData(),
                       c.getImageLink(),
                       c.getOtpMaxLength(),
                       OtpFormat.getByValue(c.getSpiOtpFormat().getValue()).orElse(null),
                       c.getAdditionalInformation()))
                   .orElse(null);
    }

    public TppInfo mapToTppInfo(PaymentRequestParameters requestParameters) {
        if (StringUtils.isBlank(requestParameters.getQwacCertificate())) {
            return null;
        }

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(requestParameters.getQwacCertificate());
            String decodedJson = new String(decodedBytes);
            TppInfo tppInfo = objectMapper.readValue(decodedJson, TppInfo.class);
            tppInfo.setRedirectUri(requestParameters.getTppRedirectUri());
            tppInfo.setNokRedirectUri(requestParameters.getTppNokRedirectUri());
            return tppInfo;
        } catch (java.lang.Exception e) {
            log.warn("Error with converting TppInfo from certificate {}", requestParameters.getQwacCertificate());
            return null;
        }
    }
}
