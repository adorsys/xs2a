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
import de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReference;
import de.adorsys.aspsp.xs2a.domain.code.Xs2aFrequencyCode;
import de.adorsys.aspsp.xs2a.domain.code.Xs2aPurposeCode;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.aspsp.xs2a.service.validator.ValueValidatorService;
import de.adorsys.psd2.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.PERIODIC;
import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.SINGLE;
import static de.adorsys.aspsp.xs2a.service.mapper.AmountModelMapper.mapToAmount;

@Component
@RequiredArgsConstructor
public class PaymentModelMapper {
    private final ObjectMapper mapper;
    private final ValueValidatorService validationService;
    private final MessageErrorMapper messageErrorMapper;
    private final AccountModelMapper accountModelMapper;

    // Mappers into xs2a domain classes
    public Object mapToXs2aPayment(Object payment, PaymentRequestParameters requestParameters) {
        if (requestParameters.getPaymentType() == SINGLE) {
            return mapToXs2aSinglePayment(validatePayment(payment, PaymentInitiationSctJson.class));
        } else if (requestParameters.getPaymentType() == PERIODIC) {
            return mapToXs2aPeriodicPayment(validatePayment(payment, PeriodicPaymentInitiationSctJson.class));
        } else {
            return mapToXs2aBulkPayment(validatePayment(payment, BulkPaymentInitiationSctJson.class));
        }
    }

    private <R> R validatePayment(Object payment, Class<R> clazz) {
        R result = mapper.convertValue(payment, clazz);
        validationService.validate(result);
        return result;
    }

    private SinglePayment mapToXs2aSinglePayment(PaymentInitiationSctJson paymentRequest) {
        SinglePayment payment = new SinglePayment();

        payment.setEndToEndIdentification(paymentRequest.getEndToEndIdentification());
        payment.setDebtorAccount(mapToXs2aAccountReference(paymentRequest.getDebtorAccount()));
        payment.setUltimateDebtor("NOT SUPPORTED"); //TODO check for presence in new SPEC  https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
        payment.setInstructedAmount(accountModelMapper.mapToXs2aAmount(paymentRequest.getInstructedAmount()));
        payment.setCreditorAccount(mapToXs2aAccountReference(paymentRequest.getCreditorAccount()));
        payment.setCreditorAgent(paymentRequest.getCreditorAgent());
        payment.setCreditorName(paymentRequest.getCreditorName());
        payment.setCreditorAddress(accountModelMapper.mapToXs2aAddress(paymentRequest.getCreditorAddress()));
        payment.setUltimateCreditor(paymentRequest.getCreditorName());  //TODO check for presence in new SPEC https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
        payment.setPurposeCode(new Xs2aPurposeCode("N/A"));  //TODO check for presence in new SPEC https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
        payment.setRemittanceInformationUnstructured(paymentRequest.getRemittanceInformationUnstructured());
        payment.setRemittanceInformationStructured(new Remittance()); //TODO check for presence in new SPEC https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
        payment.setRequestedExecutionDate(LocalDate.now()); //TODO check for presence in new SPEC https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
        payment.setRequestedExecutionTime(LocalDateTime.now().plusHours(1)); //TODO check for presence in new SPEC https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
        return payment;
    }

    private Xs2aAccountReference mapToXs2aAccountReference(Object reference12) {
        return mapper.convertValue(reference12, Xs2aAccountReference.class);
    }

    private PeriodicPayment mapToXs2aPeriodicPayment(PeriodicPaymentInitiationSctJson paymentRequest) {
        PeriodicPayment payment = new PeriodicPayment();

        payment.setEndToEndIdentification(paymentRequest.getEndToEndIdentification());
        payment.setDebtorAccount(mapToXs2aAccountReference(paymentRequest.getDebtorAccount()));
        payment.setUltimateDebtor("NOT SUPPORTED"); //TODO check for presence in new SPEC  https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
        payment.setInstructedAmount(accountModelMapper.mapToXs2aAmount(paymentRequest.getInstructedAmount()));
        payment.setCreditorAccount(mapToXs2aAccountReference(paymentRequest.getCreditorAccount()));
        payment.setCreditorAgent(paymentRequest.getCreditorAgent());
        payment.setCreditorName(paymentRequest.getCreditorName());
        payment.setCreditorAddress(accountModelMapper.mapToXs2aAddress(paymentRequest.getCreditorAddress()));
        payment.setUltimateCreditor(paymentRequest.getCreditorName());  //TODO check for presence in new SPEC https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
        payment.setPurposeCode(new Xs2aPurposeCode("N/A"));  //TODO check for presence in new SPEC https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
        payment.setRemittanceInformationUnstructured(paymentRequest.getRemittanceInformationUnstructured());
        payment.setRemittanceInformationStructured(new Remittance()); //TODO check for presence in new SPEC https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
        payment.setRequestedExecutionDate(LocalDate.now()); //TODO check for presence in new SPEC https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
        payment.setRequestedExecutionTime(LocalDateTime.now().plusHours(1)); //TODO check for presence in new SPEC https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243

        payment.setStartDate(paymentRequest.getStartDate());
        payment.setExecutionRule(Optional.ofNullable(paymentRequest.getExecutionRule()).map(ExecutionRule::toString).orElse(null));
        payment.setEndDate(paymentRequest.getEndDate());
        payment.setFrequency(mapToXs2aFrequencyCode(paymentRequest.getFrequency()));
        payment.setDayOfExecution(Integer.parseInt(paymentRequest.getDayOfExecution().toString()));
        return payment;
    }

    private Xs2aFrequencyCode mapToXs2aFrequencyCode(FrequencyCode frequency) {
        return Xs2aFrequencyCode.valueOf(frequency.name());
    }

    private BulkPayment mapToXs2aBulkPayment(BulkPaymentInitiationSctJson paymentRequest) {
        BulkPayment bulkPayment = new BulkPayment();
        bulkPayment.setBatchBookingPreferred(paymentRequest.getBatchBookingPreferred());
        bulkPayment.setDebtorAccount(mapToXs2aAccountReference(paymentRequest.getDebtorAccount()));
        bulkPayment.setRequestedExecutionDate(paymentRequest.getRequestedExecutionDate());
        bulkPayment.setPayments(paymentRequest.getPayments().stream()
                                    .map(p -> {
                                        SinglePayment payment = new SinglePayment();
                                        payment.setDebtorAccount(mapToXs2aAccountReference(paymentRequest.getDebtorAccount()));
                                        payment.setRequestedExecutionDate(paymentRequest.getRequestedExecutionDate());
                                        payment.setEndToEndIdentification(p.getEndToEndIdentification());
                                        payment.setUltimateDebtor("NOT SUPPORTED"); //TODO check for presence in new SPEC  https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
                                        payment.setInstructedAmount(accountModelMapper.mapToXs2aAmount(p.getInstructedAmount()));
                                        payment.setCreditorAccount(mapToXs2aAccountReference(p.getCreditorAccount()));
                                        payment.setCreditorAgent(p.getCreditorAgent());
                                        payment.setCreditorName(p.getCreditorName());
                                        payment.setCreditorAddress(accountModelMapper.mapToXs2aAddress(p.getCreditorAddress()));
                                        payment.setUltimateCreditor(null);
                                        payment.setPurposeCode(new Xs2aPurposeCode(null));
                                        payment.setRemittanceInformationUnstructured(p.getRemittanceInformationUnstructured());
                                        payment.setRemittanceInformationStructured(new Remittance());//TODO check for presence in new SPEC  https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
                                        payment.setRequestedExecutionTime(LocalDateTime.now().plusHours(1));//TODO check for presence in new SPEC  https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
                                        return payment;
                                    }).collect(Collectors.toList()));

        return bulkPayment;

    }

    //Mappers into PSD2 generated API model classes
    public Object mapToGetPaymentResponse12(Object payment, PaymentType type, PaymentProduct product) {
        if (type == SINGLE) {
            SinglePayment xs2aPayment = (SinglePayment) payment;
            PaymentInitiationTarget2WithStatusResponse paymentResponse = new PaymentInitiationTarget2WithStatusResponse();
            paymentResponse.setEndToEndIdentification(xs2aPayment.getEndToEndIdentification());
            paymentResponse.setDebtorAccount(accountModelMapper.mapToAccountReference12(xs2aPayment.getDebtorAccount()));
            paymentResponse.setInstructedAmount(mapToAmount(xs2aPayment.getInstructedAmount()));
            paymentResponse.setCreditorAccount(accountModelMapper.mapToAccountReference12(xs2aPayment.getCreditorAccount()));
            paymentResponse.setCreditorAgent(xs2aPayment.getCreditorAgent());
            paymentResponse.setCreditorName(xs2aPayment.getCreditorName());
            paymentResponse.setCreditorAddress(accountModelMapper.mapToAddress12(xs2aPayment.getCreditorAddress()));
            paymentResponse.setRemittanceInformationUnstructured(xs2aPayment.getRemittanceInformationUnstructured());
            paymentResponse.setTransactionStatus(mapToTransactionStatus12(xs2aPayment.getTransactionStatus())); //TODO add field to xs2a entity https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
            return paymentResponse;
        } else if (type == PERIODIC) {
            PeriodicPayment xs2aPayment = (PeriodicPayment) payment;
            PeriodicPaymentInitiationTarget2WithStatusResponse paymentResponse = new PeriodicPaymentInitiationTarget2WithStatusResponse();

            paymentResponse.setEndToEndIdentification(xs2aPayment.getEndToEndIdentification());
            paymentResponse.setDebtorAccount(accountModelMapper.mapToAccountReference12(xs2aPayment.getDebtorAccount()));
            paymentResponse.setInstructedAmount(mapToAmount(xs2aPayment.getInstructedAmount()));
            paymentResponse.setCreditorAccount(accountModelMapper.mapToAccountReference12(xs2aPayment.getCreditorAccount()));
            paymentResponse.setCreditorAgent(xs2aPayment.getCreditorAgent());
            paymentResponse.setCreditorName(xs2aPayment.getCreditorName());
            paymentResponse.setCreditorAddress(accountModelMapper.mapToAddress12(xs2aPayment.getCreditorAddress()));
            paymentResponse.setRemittanceInformationUnstructured(xs2aPayment.getRemittanceInformationUnstructured());
            paymentResponse.setStartDate(xs2aPayment.getStartDate());
            paymentResponse.setEndDate(xs2aPayment.getEndDate());
            paymentResponse.setExecutionRule(ExecutionRule.valueOf(xs2aPayment.getExecutionRule()));
            paymentResponse.setFrequency(FrequencyCode.valueOf(xs2aPayment.getFrequency().name()));
            String executionDateString = String.format("%02d", xs2aPayment.getDayOfExecution());
            paymentResponse.setDayOfExecution(DayOfExecution.fromValue(executionDateString));
            paymentResponse.setTransactionStatus(mapToTransactionStatus12(xs2aPayment.getTransactionStatus())); //TODO add field to xs2a entity https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
            return paymentResponse;
        } else {
            BulkPayment xs2aPayment = (BulkPayment) payment;
            BulkPaymentInitiationTarget2WithStatusResponse paymentResponse = new BulkPaymentInitiationTarget2WithStatusResponse();

            paymentResponse.setBatchBookingPreferred(xs2aPayment.getBatchBookingPreferred());
            paymentResponse.setRequestedExecutionDate(xs2aPayment.getRequestedExecutionDate());
            paymentResponse.setDebtorAccount(xs2aPayment.getDebtorAccount());
            paymentResponse.setPayments(mapToBulkPartList12(xs2aPayment.getPayments()));
            paymentResponse.setTransactionStatus(mapToTransactionStatus12(Xs2aTransactionStatus.RCVD)); //TODO add field to xs2a entity https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
            return paymentResponse;
        }
    }

    public static PaymentInitiationStatusResponse200Json mapToStatusResponse12(Xs2aTransactionStatus status) {
        return new PaymentInitiationStatusResponse200Json().transactionStatus(mapToTransactionStatus12(status));
    }

    public static TransactionStatus mapToTransactionStatus12(Xs2aTransactionStatus responseObject) {
        return TransactionStatus.valueOf(responseObject.name());
    }

    public Object mapToPaymentInitiationResponse12(Object response,PaymentRequestParameters requestParameters) {
        PaymentInitationRequestResponse201 response201 = new PaymentInitationRequestResponse201();
        if (EnumSet.of(SINGLE,PERIODIC).contains(requestParameters.getPaymentType())) {
            PaymentInitialisationResponse specificResponse = (PaymentInitialisationResponse) response;
            response201.setTransactionStatus(mapToTransactionStatus12(specificResponse.getTransactionStatus()));
            response201.setPaymentId(specificResponse.getPaymentId());
            response201.setTransactionFees(mapToAmount(specificResponse.getTransactionFees()));
            response201.setTransactionFeeIndicator(specificResponse.isTransactionFeeIndicator());
            response201.setScaMethods(null); //TODO Fix Auth methods mapping
            response201.setChosenScaMethod(null); //TODO add to xs2a domain obj https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
            response201.setChallengeData(null); //TODO add to xs2a domain obj https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/243
            response201.setLinks(mapper.convertValue(((PaymentInitialisationResponse) response).getLinks(), Map.class));
            response201.setPsuMessage(specificResponse.getPsuMessage());
            response201.setTppMessages(messageErrorMapper.mapToTppMessages(specificResponse.getTppMessages()));
            return response201;
        } else {
            List<PaymentInitialisationResponse> specificResponse = (List<PaymentInitialisationResponse>) response;
            return specificResponse.stream()
                       .peek(r -> {
                           PaymentRequestParameters parameters = new PaymentRequestParameters();
                           parameters.setPaymentType(SINGLE);
                           mapToPaymentInitiationResponse12(r, parameters);
                       })
                       .collect(Collectors.toList());
        }
    }

    private List<PaymentInitiationTarget2Json> mapToBulkPartList12(List<SinglePayment> payments) {
        return payments.stream()
                   .map(this::mapToBulkPart12)
                   .collect(Collectors.toList());
    }

    private PaymentInitiationTarget2Json mapToBulkPart12(SinglePayment payment) {
        PaymentInitiationTarget2Json bulkPart = new PaymentInitiationTarget2Json().endToEndIdentification(payment.getEndToEndIdentification());
        bulkPart.setDebtorAccount(accountModelMapper.mapToAccountReference12(payment.getDebtorAccount()));
        bulkPart.setInstructedAmount(mapToAmount(payment.getInstructedAmount()));
        bulkPart.setCreditorAccount(accountModelMapper.mapToAccountReference12(payment.getCreditorAccount()));
        bulkPart.setCreditorAgent(payment.getCreditorAgent());
        bulkPart.setCreditorName(payment.getCreditorName());
        bulkPart.setCreditorAddress(accountModelMapper.mapToAddress12(payment.getCreditorAddress()));
        bulkPart.setRemittanceInformationUnstructured(payment.getRemittanceInformationUnstructured());
        return bulkPart;
    }

    public PaymentRequestParameters mapToPaymentRequestParameters(String paymentProduct, String paymentService, byte[] tpPSignatureCertificate, String tpPRedirectURI, String tpPNokRedirectURI) {
        PaymentRequestParameters parameters = new PaymentRequestParameters();
        parameters.setPaymentProduct(PaymentProduct.getByValue(paymentProduct).orElseThrow(() -> new IllegalArgumentException("Unsupported payment product")));
        parameters.setPaymentType(PaymentType.getByValue(paymentService).orElseThrow(() -> new IllegalArgumentException("Unsupported payment service")));
        parameters.setQwacCertificate(new String(Optional.ofNullable(tpPSignatureCertificate).orElse(new byte[]{}), StandardCharsets.UTF_8));
        parameters.setTppRedirectUri(tpPRedirectURI);
        parameters.setTppNokRedirectUri(tpPNokRedirectURI);
        return parameters;
    }
}
