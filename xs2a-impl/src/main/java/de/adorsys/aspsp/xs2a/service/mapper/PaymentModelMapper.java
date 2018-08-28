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

import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.address.Address;
import de.adorsys.aspsp.xs2a.domain.address.CountryCode;
import de.adorsys.aspsp.xs2a.domain.code.BICFI;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.psd2.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.PERIODIC;
import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.SINGLE;
import static de.adorsys.aspsp.xs2a.service.mapper.AccountModelMapper.*;

public final class PaymentModelMapper<T, R> {
    public static TransactionStatus mapToTransactionStatus12(de.adorsys.aspsp.xs2a.domain.TransactionStatus responseObject) {
        return TransactionStatus.valueOf(responseObject.name());
    }

    public static PaymentInitationRequestResponse201 mapToPaymentInitiationResponse12(PaymentInitialisationResponse response, PaymentType type, PaymentProduct product) {
        PaymentInitationRequestResponse201 response201 = new PaymentInitationRequestResponse201();
        response201.setTransactionStatus(mapToTransactionStatus12(response.getTransactionStatus()));
        response201.setPaymentId(response.getPaymentId());
        response201.setTransactionFees(mapToAmount12(response.getTransactionFees()));
        response201.setTransactionFeeIndicator(response.isTransactionFeeIndicator());
        response201.setScaMethods(null); //TODO Fix Auth methods mapping
        response201.setChosenScaMethod(null); //TODO add to xs2a domain obj https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
        response201.setChallengeData(null); //TODO add to xs2a domain obj https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
        response201.setLinks(null); //TODO add new mapper for Links https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/244
        response201.setPsuMessage(response.getPsuMessage());
        response201.setTppMessages(null); //TODO add new Mapper https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/242
        return response201;
    }

    public static <T, R> T mapToXs2aPayment(R payment, PaymentType type, PaymentProduct product) {
        if (type == SINGLE) {
            SinglePayment targetPayment = new SinglePayment();
            Map<String, Object> paymentRequest = (LinkedHashMap<String, Object>) payment;

            targetPayment.setEndToEndIdentification(Optional.ofNullable(paymentRequest.get("endToEndIdentification"))
                                                        .map(Object::toString)
                                                        .orElse(null));
            targetPayment.setDebtorAccount(Optional.ofNullable(paymentRequest.get("debtorAccount"))
                                               .map(PaymentModelMapper::mapToXs2aAccountReference)
                                               .orElse(new AccountReference()));
            targetPayment.setUltimateDebtor(""); //TODO check for presence in new SPEC  https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
            targetPayment.setInstructedAmount(Optional.ofNullable(paymentRequest.get("instructedAmount"))
                                                  .map(PaymentModelMapper::mapToXs2aAmount)
                                                  .orElse(new de.adorsys.aspsp.xs2a.domain.Amount()));
            targetPayment.setCreditorAccount(Optional.ofNullable(paymentRequest.get("creditorAccount"))
                                                 .map(PaymentModelMapper::mapToXs2aAccountReference)
                                                 .orElse(new AccountReference()));
            targetPayment.setCreditorAgent(Optional.ofNullable(paymentRequest.get("creditorAgent"))
                                               .map(Object::toString)
                                               .map(PaymentModelMapper::mapToXs2aBICFI)
                                               .orElse(new BICFI()));
            targetPayment.setCreditorName(Optional.ofNullable(paymentRequest.get("creditorName"))
                                              .map(Object::toString)
                                              .orElse(null));
            targetPayment.setCreditorAddress(Optional.ofNullable(paymentRequest.get("creditorAddress"))
                                                 .map(PaymentModelMapper::mapToXs2aAddress)
                                                 .orElse(new Address()));
            targetPayment.setUltimateCreditor(Optional.ofNullable(paymentRequest.get("creditorName"))
                                                  .map(Object::toString)
                                                  .orElse(null));  //TODO check for presence in new SPEC https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
            targetPayment.setPurposeCode(new de.adorsys.aspsp.xs2a.domain.code.PurposeCode("N/A"));  //TODO check for presence in new SPEC https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
            targetPayment.setRemittanceInformationUnstructured(Optional.ofNullable(paymentRequest.get("remittanceInformationUnstructured"))
                                                                   .map(Object::toString)
                                                                   .orElse(null));
            targetPayment.setRemittanceInformationStructured(new Remittance()); //TODO check for presence in new SPEC https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
            targetPayment.setRequestedExecutionDate(LocalDate.now()); //TODO check for presence in new SPEC https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
            targetPayment.setRequestedExecutionTime(LocalDateTime.now()); //TODO check for presence in new SPEC https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
            return (T) targetPayment;
        } else if (type == PERIODIC) {
            PeriodicPayment targetPayment = new PeriodicPayment();

            return (T) targetPayment;
        } else {
            List<SinglePayment> targetPayment = new ArrayList<>();

            return (T) targetPayment;
        }
    }

    public static BICFI mapToXs2aBICFI(String creditorAgent) {
        BICFI bicfi = new BICFI();
        bicfi.setCode(creditorAgent);
        return bicfi;
    }

    public static AccountReference mapToXs2aAccountReference(Object account) {
        Map<String, Object> accRef = (LinkedHashMap) account;
        AccountReference reference = new AccountReference();
        reference.setIban(Optional.ofNullable(accRef.get("iban"))
                              .map(Object::toString)
                              .orElse(null));
        reference.setBban(Optional.ofNullable(accRef.get("bban"))
                              .map(Object::toString)
                              .orElse(null));
        reference.setPan(Optional.ofNullable(accRef.get("pan"))
                             .map(Object::toString)
                             .orElse(null));
        reference.setMaskedPan(Optional.ofNullable(accRef.get("maskedPan"))
                                   .map(Object::toString)
                                   .orElse(null));
        reference.setMsisdn(Optional.ofNullable(accRef.get("msisdn"))
                                .map(Object::toString)
                                .orElse(null));
        reference.setCurrency(Optional.ofNullable(accRef.get("currency"))
                                  .map(PaymentModelMapper::mapToCurrency)
                                  .orElse(null));
        return reference;
    }

    public static <T, R> T mapToGetPaymentResponse12(R payment, PaymentType type, PaymentProduct product) {
        if (type == SINGLE) {
            SinglePayment xs2aPayment = (SinglePayment) payment;
            PaymentInitiationTarget2WithStatusResponse paymentResponse = new PaymentInitiationTarget2WithStatusResponse();
            paymentResponse.setEndToEndIdentification(xs2aPayment.getEndToEndIdentification());
            paymentResponse.setDebtorAccount(mapToAccountReference12(xs2aPayment.getDebtorAccount()));
            paymentResponse.setInstructedAmount(mapToAmount12(xs2aPayment.getInstructedAmount()));
            paymentResponse.setCreditorAccount(mapToAccountReference12(xs2aPayment.getCreditorAccount()));
            paymentResponse.setCreditorAgent(xs2aPayment.getCreditorAgent().getCode());
            paymentResponse.setCreditorName(xs2aPayment.getCreditorName());
            paymentResponse.setCreditorAddress(mapToAddress12(xs2aPayment.getCreditorAddress()));
            paymentResponse.setRemittanceInformationUnstructured(xs2aPayment.getRemittanceInformationUnstructured());
            paymentResponse.setTransactionStatus(mapToTransactionStatus12(de.adorsys.aspsp.xs2a.domain.TransactionStatus.RCVD)); //TODO add field to xs2a entity https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
            return (T) paymentResponse;
        } else if (type == PERIODIC) {
            PeriodicPayment xs2aPayment = (PeriodicPayment) payment;
            PeriodicPaymentInitiationTarget2WithStatusResponse paymentResponse = new PeriodicPaymentInitiationTarget2WithStatusResponse();

            paymentResponse.setEndToEndIdentification(xs2aPayment.getEndToEndIdentification());
            paymentResponse.setDebtorAccount(mapToAccountReference12(xs2aPayment.getDebtorAccount()));
            paymentResponse.setInstructedAmount(mapToAmount12(xs2aPayment.getInstructedAmount()));
            paymentResponse.setCreditorAccount(mapToAccountReference12(xs2aPayment.getCreditorAccount()));
            paymentResponse.setCreditorAgent(xs2aPayment.getCreditorAgent().getCode());
            paymentResponse.setCreditorName(xs2aPayment.getCreditorName());
            paymentResponse.setCreditorAddress(mapToAddress12(xs2aPayment.getCreditorAddress()));
            paymentResponse.setRemittanceInformationUnstructured(xs2aPayment.getRemittanceInformationUnstructured());
            paymentResponse.setStartDate(xs2aPayment.getStartDate());
            paymentResponse.setEndDate(xs2aPayment.getEndDate());
            paymentResponse.setExecutionRule(ExecutionRule.valueOf(xs2aPayment.getExecutionRule()));
            paymentResponse.setFrequency(FrequencyCode.valueOf(xs2aPayment.getFrequency().name()));
            String executionDateString = String.format("%02d", xs2aPayment.getDayOfExecution());
            paymentResponse.setDayOfExecution(DayOfExecution.fromValue(executionDateString));
            paymentResponse.setTransactionStatus(mapToTransactionStatus12(de.adorsys.aspsp.xs2a.domain.TransactionStatus.RCVD)); //TODO add field to xs2a entity https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
            return (T) paymentResponse;
        } else {
            List<SinglePayment> xs2aPayment = (List<SinglePayment>) payment;
            BulkPaymentInitiationTarget2WithStatusResponse paymentResponse = new BulkPaymentInitiationTarget2WithStatusResponse();

            paymentResponse.setBatchBookingPreferred(false); //TODO create entity and add value! https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
            paymentResponse.setRequestedExecutionDate(LocalDate.now()); //TODO create entity and add field! https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
            paymentResponse.setDebtorAccount(mapToAccountReference12(xs2aPayment.get(0).getDebtorAccount())); //TODO create entity and add field! https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
            paymentResponse.setPayments(mapToBulkPartList12(xs2aPayment));
            paymentResponse.setTransactionStatus(mapToTransactionStatus12(de.adorsys.aspsp.xs2a.domain.TransactionStatus.RCVD)); //TODO add field to xs2a entity https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
            return (T) paymentResponse;
        }
    }

    private static List<PaymentInitiationTarget2Json> mapToBulkPartList12(List<SinglePayment> payments) {
        return payments.stream()
                   .map(PaymentModelMapper::mapToBulkPart12)
                   .collect(Collectors.toList());
    }

    private static PaymentInitiationTarget2Json mapToBulkPart12(SinglePayment payment) {
        PaymentInitiationTarget2Json bulkPart = new PaymentInitiationTarget2Json().endToEndIdentification(payment.getEndToEndIdentification());
        bulkPart.setDebtorAccount(mapToAccountReference12(payment.getDebtorAccount()));
        bulkPart.setInstructedAmount(mapToAmount12(payment.getInstructedAmount()));
        bulkPart.setCreditorAccount(mapToAccountReference12(payment.getCreditorAccount()));
        bulkPart.setCreditorAgent(payment.getCreditorAgent().getCode());
        bulkPart.setCreditorName(payment.getCreditorName());
        bulkPart.setCreditorAddress(mapToAddress12(payment.getCreditorAddress()));
        bulkPart.setRemittanceInformationUnstructured(payment.getRemittanceInformationUnstructured());
        return bulkPart;
    }

    public static de.adorsys.aspsp.xs2a.domain.Amount mapToXs2aAmount(Object amount) {
        Map<String, Object> requestAmount = (LinkedHashMap) amount;
        de.adorsys.aspsp.xs2a.domain.Amount resultAmount = new de.adorsys.aspsp.xs2a.domain.Amount();
        resultAmount.setCurrency(Optional.ofNullable(requestAmount.get("currency"))
                                     .map(PaymentModelMapper::mapToCurrency)
                                     .orElse(null));
        resultAmount.setContent(Optional.ofNullable(requestAmount.get("content"))
                                    .map(Object::toString)
                                    .orElse(null));
        return resultAmount;
    }

    public static Currency mapToCurrency(Object currency) {
        return Currency.getInstance(currency.toString());
    }

    public static Address mapToXs2aAddress(Object address) {
        Map<String, Object> requestAddress = (LinkedHashMap) address;
        Address resultAddress = new Address();
        CountryCode code = new CountryCode();
        code.setCode(Optional.ofNullable(requestAddress.get("country"))
                         .map(Object::toString)
                         .orElse(null));
        resultAddress.setCountry(code);
        resultAddress.setPostalCode(Optional.ofNullable(requestAddress.get("postalCode"))
                                        .map(Object::toString)
                                        .orElse(null));
        resultAddress.setCity(Optional.ofNullable(requestAddress.get("city"))
                                  .map(Object::toString)
                                  .orElse(null));
        resultAddress.setStreet(Optional.ofNullable(requestAddress.get("street"))
                                    .map(Object::toString)
                                    .orElse(null));
        resultAddress.setBuildingNumber(Optional.ofNullable(requestAddress.get("buildingNumber"))
                                            .map(Object::toString)
                                            .orElse(null));
        return resultAddress;
    }
}
