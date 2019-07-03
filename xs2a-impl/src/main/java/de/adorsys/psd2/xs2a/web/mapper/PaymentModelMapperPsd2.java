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

import de.adorsys.psd2.consent.api.pis.proto.PisPaymentCancellationRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.PisExecutionRule;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aChosenScaMethod;
import de.adorsys.psd2.xs2a.domain.pis.*;
import de.adorsys.psd2.xs2a.service.mapper.AccountModelMapper;
import de.adorsys.psd2.xs2a.service.mapper.AmountModelMapper;
import de.adorsys.psd2.xs2a.service.profile.StandardPaymentProductsResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.profile.PaymentType.PERIODIC;
import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentModelMapperPsd2 {
    private final CoreObjectsMapper coreObjectsMapper;
    private final AccountModelMapper accountModelMapper;
    private final TppRedirectUriMapper tppRedirectUriMapper;
    private final AmountModelMapper amountModelMapper;
    private final HrefLinkMapper hrefLinkMapper;
    private final StandardPaymentProductsResolver standardPaymentProductsResolver;
    private final ScaMethodsMapper scaMethodsMapper;
    private final Xs2aAddressMapper xs2aAddressMapper;

    public Object mapToGetPaymentResponse(Object payment, PaymentType type, String paymentProduct) {
        if (standardPaymentProductsResolver.isRawPaymentProduct(paymentProduct)) {
            PisPaymentInfo paymentInfo = (PisPaymentInfo) payment;
            return convertResponseToRawData(paymentInfo.getPaymentData());
        }

        if (type == SINGLE) {
            SinglePayment xs2aPayment = (SinglePayment) payment;
            PaymentInitiationWithStatusResponse paymentResponse = new PaymentInitiationWithStatusResponse();
            paymentResponse.setEndToEndIdentification(xs2aPayment.getEndToEndIdentification());
            paymentResponse.setDebtorAccount(accountModelMapper.mapToAccountReference(xs2aPayment.getDebtorAccount()));
            paymentResponse.setInstructedAmount(amountModelMapper.mapToAmount(xs2aPayment.getInstructedAmount()));
            paymentResponse.setCreditorAccount(accountModelMapper.mapToAccountReference(xs2aPayment.getCreditorAccount()));
            paymentResponse.setCreditorAgent(xs2aPayment.getCreditorAgent());
            paymentResponse.setCreditorName(xs2aPayment.getCreditorName());
            paymentResponse.setCreditorAddress(xs2aAddressMapper.mapToAddress(xs2aPayment.getCreditorAddress()));
            paymentResponse.setRemittanceInformationUnstructured(xs2aPayment.getRemittanceInformationUnstructured());
            paymentResponse.setTransactionStatus(mapToTransactionStatus(xs2aPayment.getTransactionStatus()));
            return paymentResponse;
        } else if (type == PERIODIC) {
            PeriodicPayment xs2aPayment = (PeriodicPayment) payment;
            PeriodicPaymentInitiationWithStatusResponse paymentResponse = new PeriodicPaymentInitiationWithStatusResponse();

            paymentResponse.setEndToEndIdentification(xs2aPayment.getEndToEndIdentification());
            paymentResponse.setDebtorAccount(accountModelMapper.mapToAccountReference(xs2aPayment.getDebtorAccount()));
            paymentResponse.setInstructedAmount(amountModelMapper.mapToAmount(xs2aPayment.getInstructedAmount()));
            paymentResponse.setCreditorAccount(accountModelMapper.mapToAccountReference(xs2aPayment.getCreditorAccount()));
            paymentResponse.setCreditorAgent(xs2aPayment.getCreditorAgent());
            paymentResponse.setCreditorName(xs2aPayment.getCreditorName());
            paymentResponse.setCreditorAddress(xs2aAddressMapper.mapToAddress(xs2aPayment.getCreditorAddress()));
            paymentResponse.setRemittanceInformationUnstructured(xs2aPayment.getRemittanceInformationUnstructured());
            paymentResponse.setStartDate(xs2aPayment.getStartDate());
            paymentResponse.setEndDate(xs2aPayment.getEndDate());
            paymentResponse.setExecutionRule(mapToExecutionRule(xs2aPayment.getExecutionRule()).orElse(null));
            paymentResponse.setFrequency(FrequencyCode.valueOf(xs2aPayment.getFrequency().name()));
            paymentResponse.setDayOfExecution(mapToDayOfExecution(xs2aPayment.getDayOfExecution()).orElse(null));
            paymentResponse.setTransactionStatus(mapToTransactionStatus(xs2aPayment.getTransactionStatus()));
            return paymentResponse;
        } else {
            BulkPayment xs2aPayment = (BulkPayment) payment;
            BulkPaymentInitiationWithStatusResponse paymentResponse = new BulkPaymentInitiationWithStatusResponse();

            paymentResponse.setBatchBookingPreferred(xs2aPayment.getBatchBookingPreferred());
            paymentResponse.setRequestedExecutionDate(xs2aPayment.getRequestedExecutionDate());
            paymentResponse.setDebtorAccount(accountModelMapper.mapToAccountReference(xs2aPayment.getDebtorAccount()));
            paymentResponse.setPayments(mapToBulkPartList(xs2aPayment.getPayments()));
            paymentResponse.setTransactionStatus(mapToTransactionStatus(xs2aPayment.getTransactionStatus()));
            return paymentResponse;
        }
    }

    public static PaymentInitiationStatusResponse200Json mapToStatusResponse(de.adorsys.psd2.xs2a.core.pis.TransactionStatus status) {
        return new PaymentInitiationStatusResponse200Json().transactionStatus(mapToTransactionStatus(status));
    }

    public static TransactionStatus mapToTransactionStatus(de.adorsys.psd2.xs2a.core.pis.TransactionStatus responseObject) {
        return Optional.ofNullable(responseObject)
                   .map(r -> TransactionStatus.valueOf(r.name()))
                   .orElse(null);
    }

    public PaymentInitationRequestResponse201 mapToPaymentInitiationResponse(PaymentInitiationResponse response) {
        PaymentInitationRequestResponse201 response201 = new PaymentInitationRequestResponse201();
        response201.setTransactionStatus(mapToTransactionStatus(response.getTransactionStatus()));
        response201.setPaymentId(response.getPaymentId());
        response201.setTransactionFees(amountModelMapper.mapToAmount(response.getTransactionFees()));
        response201.setTransactionFeeIndicator(response.getTransactionFeeIndicator());
        response201.setScaMethods(scaMethodsMapper.mapToScaMethods(response.getScaMethods()));
        response201.setChallengeData(coreObjectsMapper.mapToChallengeData(response.getChallengeData()));
        response201.setLinks(hrefLinkMapper.mapToLinksMap(response.getLinks()));
        response201.setPsuMessage(response.getPsuMessage());
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

    public PisPaymentCancellationRequest mapToPaymentCancellationRequest(String paymentProduct, String paymentService, String paymentId,
                                                                         Boolean tppExplicitAuthorisationPreferred,
                                                                         String tpPRedirectURI, String tpPNokRedirectURI){
        return new PisPaymentCancellationRequest(
            PaymentType.getByValue(paymentService).orElseThrow(() -> new IllegalArgumentException("Unsupported payment service")),
            Optional.ofNullable(paymentProduct).orElseThrow(() -> new IllegalArgumentException("Unsupported payment product")),
            paymentId,
            BooleanUtils.isTrue(tppExplicitAuthorisationPreferred),
            tppRedirectUriMapper.mapToTppRedirectUri(tpPRedirectURI, tpPNokRedirectURI));
    }

    public PaymentInitiationCancelResponse202 mapToPaymentInitiationCancelResponse(CancelPaymentResponse cancelPaymentResponse) {
        PaymentInitiationCancelResponse202 response = new PaymentInitiationCancelResponse202();
        response.setTransactionStatus(mapToTransactionStatus(cancelPaymentResponse.getTransactionStatus()));
        response.setScaMethods(scaMethodsMapper.mapToScaMethods(cancelPaymentResponse.getScaMethods()));
        response.setChosenScaMethod(mapToChosenScaMethod(cancelPaymentResponse.getChosenScaMethod()));
        response.setChallengeData(coreObjectsMapper.mapToChallengeData(cancelPaymentResponse.getChallengeData()));
        response._links(hrefLinkMapper.mapToLinksMap(cancelPaymentResponse.getLinks()));
        return response;
    }

    private List<PaymentInitiationBulkElementJson> mapToBulkPartList(List<SinglePayment> payments) {
        return payments.stream()
                   .map(this::mapToBulkPart)
                   .collect(Collectors.toList());
    }

    private PaymentInitiationBulkElementJson mapToBulkPart(SinglePayment payment) {
        PaymentInitiationBulkElementJson bulkPart = new PaymentInitiationBulkElementJson().endToEndIdentification(payment.getEndToEndIdentification());
        bulkPart.setInstructedAmount(amountModelMapper.mapToAmount(payment.getInstructedAmount()));
        bulkPart.setCreditorAccount(accountModelMapper.mapToAccountReference(payment.getCreditorAccount()));
        bulkPart.setCreditorAgent(payment.getCreditorAgent());
        bulkPart.setCreditorName(payment.getCreditorName());
        bulkPart.setCreditorAddress(xs2aAddressMapper.mapToAddress(payment.getCreditorAddress()));
        bulkPart.setRemittanceInformationUnstructured(payment.getRemittanceInformationUnstructured());
        return bulkPart;
    }

    private ChosenScaMethod mapToChosenScaMethod(Xs2aChosenScaMethod xs2aChosenScaMethod) {
        return Optional.ofNullable(xs2aChosenScaMethod)
                   .map(ch -> {
                       ChosenScaMethod method = new ChosenScaMethod();
                       method.setAuthenticationMethodId(ch.getAuthenticationMethodId());
                       method.setAuthenticationType(ch.getAuthenticationType());
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
