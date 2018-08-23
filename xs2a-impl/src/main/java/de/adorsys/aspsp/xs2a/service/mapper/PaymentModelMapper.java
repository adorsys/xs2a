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

import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.code.BICFI;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.psd2.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.PERIODIC;
import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.SINGLE;
import static de.adorsys.aspsp.xs2a.service.mapper.AccountModelMapper.*;

public final class PaymentModelMapper<T, R> {
    public static TransactionStatus mapToTransactionStatus12(ResponseObject<de.adorsys.aspsp.xs2a.domain.TransactionStatus> responseObject) {
        return mapToTransactionStatus12(responseObject.getBody());
    }

    private static TransactionStatus mapToTransactionStatus12(de.adorsys.aspsp.xs2a.domain.TransactionStatus responseObject) {
        return TransactionStatus.valueOf(responseObject.name());
    }

    public static PaymentInitationRequestResponse201 mapToPaymentInitiationResponse12(PaymentInitialisationResponse response, PaymentType type, PaymentProduct product) {
        PaymentInitationRequestResponse201 response201 = new PaymentInitationRequestResponse201();
        response201.setTransactionStatus(mapToTransactionStatus12(response.getTransactionStatus()));
        response201.setPaymentId(response.getPaymentId());
        response201.setTransactionFees(null);
        response201.setTransactionFeeIndicator(null);
        response201.setScaMethods(null);
        response201.setChosenScaMethod(null);
        response201.setChallengeData(null);
        response201.setLinks(null);
        response201.setPsuMessage(null);
        response201.setTppMessages(null);
        return response201;
    }

    public static <T, R> T mapToXs2aPayment(R payment, PaymentType type, PaymentProduct product) {
        if (type == SINGLE) {
            SinglePayment targetPayment = new SinglePayment();
            PaymentInitiationSctJson paymentRequest = (PaymentInitiationSctJson) payment;
            targetPayment.setEndToEndIdentification(paymentRequest.getEndToEndIdentification());
            targetPayment.setDebtorAccount(mapToXs2aAccountReference(paymentRequest.getDebtorAccount()));
            targetPayment.setUltimateDebtor(null); //TODO check for presence in new SPEC
            targetPayment.setInstructedAmount(mapToXs2aAmount(paymentRequest.getInstructedAmount()));
            targetPayment.setCreditorAccount(mapToXs2aAccountReference(paymentRequest.getCreditorAccount()));
            BICFI bicfi = new BICFI();
            bicfi.setCode(paymentRequest.getCreditorAgent());
            targetPayment.setCreditorAgent(bicfi);
            targetPayment.setCreditorName(paymentRequest.getCreditorName());
            targetPayment.setCreditorAddress(mapToXs2aAddress(paymentRequest.getCreditorAddress()));
            targetPayment.setUltimateCreditor(null);  //TODO check for presence in new SPEC
            targetPayment.setPurposeCode(new de.adorsys.aspsp.xs2a.domain.code.PurposeCode(null));  //TODO check for presence in new SPEC
            targetPayment.setRemittanceInformationUnstructured(paymentRequest.getRemittanceInformationUnstructured());
            targetPayment.setRemittanceInformationStructured(null); //TODO check for presence in new SPEC
            targetPayment.setRequestedExecutionDate(LocalDate.now()); //TODO check for presence in new SPEC
            targetPayment.setRequestedExecutionTime(LocalDateTime.now()); //TODO check for presence in new SPEC
            return (T) targetPayment;
        } else if (type == PERIODIC) {
            PeriodicPayment targetPayment = new PeriodicPayment();

            return (T) targetPayment;
        } else {
            List<SinglePayment> targetPayment = new ArrayList<>();

            return (T) targetPayment;
        }
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
            paymentResponse.setTransactionStatus(mapToTransactionStatus12(de.adorsys.aspsp.xs2a.domain.TransactionStatus.RCVD)); //TODO add field to xs2a entity
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
            paymentResponse.setTransactionStatus(mapToTransactionStatus12(de.adorsys.aspsp.xs2a.domain.TransactionStatus.RCVD)); //TODO add field to xs2a entity
            return (T) paymentResponse;
        } else {
            List<SinglePayment> xs2aPayment = (List<SinglePayment>) payment;
            BulkPaymentInitiationTarget2WithStatusResponse paymentResponse = new BulkPaymentInitiationTarget2WithStatusResponse();

            paymentResponse.setBatchBookingPreferred(false); //TODO create entity and add value!
            paymentResponse.setRequestedExecutionDate(LocalDate.now()); //TODO create entity and add field!
            paymentResponse.setDebtorAccount(mapToAccountReference12(xs2aPayment.get(0).getDebtorAccount())); //TODO create entity and add field!
            paymentResponse.setPayments(mapToBulkPartList12(xs2aPayment));
            paymentResponse.setTransactionStatus(mapToTransactionStatus12(de.adorsys.aspsp.xs2a.domain.TransactionStatus.RCVD)); //TODO add field to xs2a entity
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
}
