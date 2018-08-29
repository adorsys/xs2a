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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.code.BICFI;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.psd2.model.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.PERIODIC;
import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.SINGLE;
import static de.adorsys.aspsp.xs2a.service.mapper.AccountModelMapper.*;

public final class PaymentModelMapper {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new ParameterNamesModule());
    }

    public static <T> T mapToXs2aPayment(Object payment, PaymentType type, PaymentProduct product) {
        if (type == SINGLE) {
            PaymentInitiationSctJson single = mapper.convertValue(payment, PaymentInitiationSctJson.class);
            return (T) mapToXs2aSinglePayment(single);

        } else if (type == PERIODIC) {
            PeriodicPaymentInitiationSctJson periodic = mapper.convertValue(payment, PeriodicPaymentInitiationSctJson.class);
            return (T) mapToXs2aPeriodicPayment(periodic);
        } else {
            BulkPaymentInitiationSctJson bulk = mapper.convertValue(payment, BulkPaymentInitiationSctJson.class);
            return (T) mapToXs2aBulkPayment(bulk);
        }
    }

    private static SinglePayment mapToXs2aSinglePayment(@Valid PaymentInitiationSctJson paymentRequest) {
        SinglePayment singlePayment = new SinglePayment();

        singlePayment.setEndToEndIdentification(paymentRequest.getEndToEndIdentification());
        singlePayment.setDebtorAccount(mapToXs2aAccountReference(paymentRequest.getDebtorAccount()));
        singlePayment.setUltimateDebtor(null); //TODO check for presence in new SPEC  https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
        singlePayment.setInstructedAmount(mapToXs2aAmount(paymentRequest.getInstructedAmount()));
        singlePayment.setCreditorAccount(mapToXs2aAccountReference(paymentRequest.getCreditorAccount()));
        singlePayment.setCreditorAgent(mapToXs2aBICFI(paymentRequest.getCreditorAgent()));
        singlePayment.setCreditorName(paymentRequest.getCreditorName());
        singlePayment.setCreditorAddress(mapToXs2aAddress(paymentRequest.getCreditorAddress()));
        singlePayment.setUltimateCreditor(paymentRequest.getCreditorName());  //TODO check for presence in new SPEC https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
        singlePayment.setPurposeCode(new de.adorsys.aspsp.xs2a.domain.code.PurposeCode("N/A"));  //TODO check for presence in new SPEC https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
        singlePayment.setRemittanceInformationUnstructured(paymentRequest.getRemittanceInformationUnstructured());
        singlePayment.setRemittanceInformationStructured(new Remittance()); //TODO check for presence in new SPEC https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
        singlePayment.setRequestedExecutionDate(LocalDate.now()); //TODO check for presence in new SPEC https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
        singlePayment.setRequestedExecutionTime(LocalDateTime.now()); //TODO check for presence in new SPEC https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
        return singlePayment;
    }

    private static AccountReference mapToXs2aAccountReference(Object reference12) {
        return mapper.convertValue(reference12, AccountReference.class);
    }

    private static PeriodicPayment mapToXs2aPeriodicPayment(@Valid PeriodicPaymentInitiationSctJson paymentRequest) {
        PeriodicPayment payment = (PeriodicPayment) mapToXs2aSinglePayment(mapper.convertValue(paymentRequest, PaymentInitiationSctJson.class));

        payment.setStartDate(paymentRequest.getStartDate());
        payment.setExecutionRule(paymentRequest.getExecutionRule().toString());
        payment.setEndDate(paymentRequest.getEndDate());
        payment.setFrequency(mapToXs2aFrequencyCode(paymentRequest.getFrequency()));
        payment.setDayOfExecution(Integer.parseInt(paymentRequest.getDayOfExecution().toString()));
        return payment;
    }

    private static de.adorsys.aspsp.xs2a.domain.code.FrequencyCode mapToXs2aFrequencyCode(@Valid FrequencyCode frequency) {
        return de.adorsys.aspsp.xs2a.domain.code.FrequencyCode.valueOf(frequency.name());
    }

    private static List<SinglePayment> mapToXs2aBulkPayment(@Valid BulkPaymentInitiationSctJson paymentRequest) {
        return paymentRequest.getPayments().stream()
                   .map(p -> {
                       SinglePayment xs2aPayment = new SinglePayment();
                       xs2aPayment.setDebtorAccount(mapToXs2aAccountReference(paymentRequest.getDebtorAccount()));
                       xs2aPayment.setRequestedExecutionDate(paymentRequest.getRequestedExecutionDate());
                       xs2aPayment.setEndToEndIdentification(p.getEndToEndIdentification());
                       xs2aPayment.setUltimateDebtor(null);
                       xs2aPayment.setInstructedAmount(mapToXs2aAmount(p.getInstructedAmount()));
                       xs2aPayment.setCreditorAccount(mapToXs2aAccountReference(p.getCreditorAccount()));
                       xs2aPayment.setCreditorAgent(mapToXs2aBICFI(p.getCreditorAgent()));
                       xs2aPayment.setCreditorName(p.getCreditorName());
                       xs2aPayment.setCreditorAddress(mapToXs2aAddress(p.getCreditorAddress()));
                       xs2aPayment.setUltimateCreditor(null);
                       xs2aPayment.setPurposeCode(new de.adorsys.aspsp.xs2a.domain.code.PurposeCode(null));
                       xs2aPayment.setRemittanceInformationUnstructured(p.getRemittanceInformationUnstructured());
                       xs2aPayment.setRemittanceInformationStructured(new Remittance());//TODO
                       xs2aPayment.setRequestedExecutionTime(null);//TODO
                       return xs2aPayment;
                   }).collect(Collectors.toList());

    }

    public static BICFI mapToXs2aBICFI(String creditorAgent) {
        BICFI bicfi = new BICFI();
        bicfi.setCode(creditorAgent);
        return bicfi;
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

    public static TransactionStatus mapToTransactionStatus12(de.adorsys.aspsp.xs2a.domain.TransactionStatus responseObject) {
        return TransactionStatus.valueOf(responseObject.name());
    }

    public static <T, R> R mapToPaymentInitiationResponse12(T response, PaymentType type, PaymentProduct product) {
        PaymentInitationRequestResponse201 response201 = new PaymentInitationRequestResponse201();
        if (type == SINGLE || type == PERIODIC) {
            PaymentInitialisationResponse specificResponse = (PaymentInitialisationResponse) response;
            response201.setTransactionStatus(mapToTransactionStatus12(specificResponse.getTransactionStatus()));
            response201.setPaymentId(specificResponse.getPaymentId());
            response201.setTransactionFees(mapToAmount12(specificResponse.getTransactionFees()));
            response201.setTransactionFeeIndicator(specificResponse.isTransactionFeeIndicator());
            response201.setScaMethods(null); //TODO Fix Auth methods mapping
            response201.setChosenScaMethod(null); //TODO add to xs2a domain obj https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
            response201.setChallengeData(null); //TODO add to xs2a domain obj https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
            response201.setLinks(null); //TODO add new mapper for Links https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/244
            response201.setPsuMessage(specificResponse.getPsuMessage());
            response201.setTppMessages(null); //TODO add new Mapper https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/242
            return (R) response201;
        } else {
            List<PaymentInitialisationResponse> specificResponse = (List<PaymentInitialisationResponse>) response;
            return (R) specificResponse.stream()
                           .map(r -> mapToGetPaymentResponse12(r, SINGLE, product))
                           .collect(Collectors.toList());
        }
    }
}
