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

package de.adorsys.psd2.xs2a.web.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.PisExecutionRule;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aChosenScaMethod;
import de.adorsys.psd2.xs2a.domain.pis.*;
import de.adorsys.psd2.xs2a.service.mapper.AccountModelMapper;
import de.adorsys.psd2.xs2a.service.mapper.MessageErrorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.profile.PaymentType.PERIODIC;
import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;
import static de.adorsys.psd2.xs2a.service.mapper.AmountModelMapper.mapToAmount;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentModelMapperPsd2 {
    private final ObjectMapper mapper;
    private final CoreObjectsMapper coreObjectsMapper;
    private final MessageErrorMapper messageErrorMapper;
    private final AccountModelMapper accountModelMapper;
    private final TppRedirectUriMapper tppRedirectUriMapper;

    public Object mapToGetPaymentResponse12(Object payment, PaymentType type, String product) {
        if (isRawPayment(payment)) {
            PisPaymentInfo paymentInfo = (PisPaymentInfo) payment;
            String paymentResponse = convertResponseToRawData(paymentInfo.getPaymentData());

            //TODO rework this check during refactoring of payment saving https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/517
            if (paymentInfo.getPaymentProduct().contains("pain")) {
                return paymentResponse;
            }

            // it will be useful when we implement raw json payment
            return enrichPaymentWithStatus(paymentInfo.getTransactionStatus(), paymentResponse);
        }
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
            paymentResponse.setTransactionStatus(mapToTransactionStatus12(xs2aPayment.getTransactionStatus()));
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
            paymentResponse.setExecutionRule(mapToExecutionRule(xs2aPayment.getExecutionRule()).orElse(null));
            paymentResponse.setFrequency(FrequencyCode.valueOf(xs2aPayment.getFrequency().name()));
            paymentResponse.setDayOfExecution(mapToDayOfExecution(xs2aPayment.getDayOfExecution()).orElse(null));
            paymentResponse.setTransactionStatus(mapToTransactionStatus12(xs2aPayment.getTransactionStatus()));
            return paymentResponse;
        } else {
            BulkPayment xs2aPayment = (BulkPayment) payment;
            BulkPaymentInitiationTarget2WithStatusResponse paymentResponse = new BulkPaymentInitiationTarget2WithStatusResponse();

            paymentResponse.setBatchBookingPreferred(xs2aPayment.getBatchBookingPreferred());
            paymentResponse.setRequestedExecutionDate(xs2aPayment.getRequestedExecutionDate());
            paymentResponse.setDebtorAccount(accountModelMapper.mapToAccountReference12(xs2aPayment.getDebtorAccount()));
            paymentResponse.setPayments(mapToBulkPartList12(xs2aPayment.getPayments()));
            paymentResponse.setTransactionStatus(mapToTransactionStatus12(xs2aPayment.getTransactionStatus()));
            return paymentResponse;
        }
    }

    @Nullable
    private Object enrichPaymentWithStatus(de.adorsys.psd2.xs2a.core.pis.TransactionStatus transactionStatus, Object resp) {
        try {
            Map paymentData = mapper.readValue(resp.toString(), Map.class);
            paymentData.put("transactionStatus", transactionStatus);
            return paymentData;
        } catch (IOException e) {
            log.warn("Can not convert payment to json ", e);
        }
        return null;
    }

    public static PaymentInitiationStatusResponse200Json mapToStatusResponse12(de.adorsys.psd2.xs2a.core.pis.TransactionStatus status) {
        return new PaymentInitiationStatusResponse200Json().transactionStatus(mapToTransactionStatus12(status));
    }

    public static TransactionStatus mapToTransactionStatus12(de.adorsys.psd2.xs2a.core.pis.TransactionStatus responseObject) {
        return Optional.ofNullable(responseObject)
                   .map(r -> TransactionStatus.valueOf(r.name()))
                   .orElse(null);
    }

    public Object mapToPaymentInitiationResponse12(Object response) {
        PaymentInitationRequestResponse201 response201 = new PaymentInitationRequestResponse201();
        PaymentInitiationResponse specificResponse = (PaymentInitiationResponse) response;
        response201.setTransactionStatus(mapToTransactionStatus12(specificResponse.getTransactionStatus()));
        response201.setPaymentId(specificResponse.getPaymentId());
        response201.setTransactionFees(mapToAmount(specificResponse.getTransactionFees()));
        response201.setTransactionFeeIndicator(specificResponse.isTransactionFeeIndicator());
        response201.setScaMethods(mapToScaMethods(specificResponse.getScaMethods()));
        response201.setChallengeData(coreObjectsMapper.mapToChallengeData(specificResponse.getChallengeData()));
        response201.setLinks(mapper.convertValue(((PaymentInitiationResponse) response).getLinks(), Map.class));
        response201.setPsuMessage(specificResponse.getPsuMessage());
        response201.setTppMessages(messageErrorMapper.mapToTppMessages(specificResponse.getTppMessages()));
        return response201;
    }

    public PaymentInitiationParameters mapToPaymentRequestParameters(String paymentProduct, String paymentService, byte[] tpPSignatureCertificate, String tpPRedirectURI, String tpPNokRedirectURI, boolean tppExplicitAuthorisationPreferred, PsuIdData psuData) {
        PaymentInitiationParameters parameters = new PaymentInitiationParameters();
        parameters.setPaymentType(PaymentType.getByValue(paymentService).orElseThrow(() -> new IllegalArgumentException("Unsupported payment service")));
        parameters.setPaymentProduct(Optional.ofNullable(paymentProduct).orElseThrow(() -> new IllegalArgumentException("Unsupported payment product")));
        parameters.setQwacCertificate(new String(Optional.ofNullable(tpPSignatureCertificate).orElse(new byte[]{}), StandardCharsets.UTF_8));
        parameters.setTppRedirectUri(tppRedirectUriMapper.mapToTppRedirectUri(tpPRedirectURI, tpPNokRedirectURI));
        parameters.setTppExplicitAuthorisationPreferred(tppExplicitAuthorisationPreferred);
        parameters.setPsuData(psuData);
        return parameters;
    }

    public PaymentInitiationCancelResponse200202 mapToPaymentInitiationCancelResponse(CancelPaymentResponse cancelPaymentResponse) {
        PaymentInitiationCancelResponse200202 response = new PaymentInitiationCancelResponse200202();
        response.setTransactionStatus(mapToTransactionStatus12(cancelPaymentResponse.getTransactionStatus()));
        response.setScaMethods(mapToScaMethods(cancelPaymentResponse.getScaMethods()));
        response.setChosenScaMethod(mapToChosenScaMethod(cancelPaymentResponse.getChosenScaMethod()));
        response.setChallengeData(coreObjectsMapper.mapToChallengeData(cancelPaymentResponse.getChallengeData()));
        response._links(mapper.convertValue(cancelPaymentResponse.getLinks(), Map.class));
        return response;
    }

    private boolean isRawPayment(Object payment) {
        return payment instanceof PisPaymentInfo;
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

    private ScaMethods mapToScaMethods(Xs2aAuthenticationObject... authenticationObjects) {
        return Optional.ofNullable(authenticationObjects)
                   .map(objects -> {
                       ScaMethods scaMethods = new ScaMethods();
                       Arrays.stream(objects)
                           .map(this::mapToAuthenticationObject)
                           .filter(Objects::nonNull)
                           .forEach(scaMethods::add);
                       return scaMethods;
                   })
                   .orElse(null);
    }

    private AuthenticationObject mapToAuthenticationObject(Xs2aAuthenticationObject xs2aAuthenticationObject) {
        return Optional.ofNullable(xs2aAuthenticationObject)
                   .map(a -> {
                       AuthenticationObject psd2Authentication = new AuthenticationObject();
                       psd2Authentication.setAuthenticationType(AuthenticationType.fromValue(a.getAuthenticationType()));
                       psd2Authentication.setAuthenticationVersion(a.getAuthenticationVersion());
                       psd2Authentication.setAuthenticationMethodId(a.getAuthenticationMethodId());
                       psd2Authentication.setName(a.getName());
                       psd2Authentication.setExplanation(a.getExplanation());
                       return psd2Authentication;
                   })
                   .orElse(null);
    }

    private ChosenScaMethod mapToChosenScaMethod(Xs2aChosenScaMethod xs2aChosenScaMethod) {
        return Optional.ofNullable(xs2aChosenScaMethod)
                   .map(ch -> {
                       ChosenScaMethod method = new ChosenScaMethod();
                       method.setAuthenticationMethodId(ch.getAuthenticationMethodId());
                       method.setAuthenticationType(AuthenticationType.fromValue(ch.getAuthenticationType()));
                       return method;
                   }).orElse(null);
    }

    private String convertResponseToRawData(byte[] paymentData) {
        try {
            return IOUtils.toString(paymentData, Charset.defaultCharset().name());
        } catch (IOException e) {
            log.warn("Can not convert payment from byte[] ", e);
            return null;
        }
    }

    private Optional<DayOfExecution> mapToDayOfExecution(PisDayOfExecution dayOfExecution) {
        return Optional.ofNullable(dayOfExecution)
                   .map(PisDayOfExecution::toString)
                   .map(DayOfExecution::fromValue);
    }

    private Optional<ExecutionRule> mapToExecutionRule(PisExecutionRule rule) {
        return Optional.ofNullable(rule)
                   .map(PisExecutionRule::toString)
                   .map(ExecutionRule::fromValue);
    }
}
