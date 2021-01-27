/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

import com.fasterxml.jackson.core.JsonProcessingException;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentCancellationRequest;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppNotificationData;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aChosenScaMethod;
import de.adorsys.psd2.xs2a.domain.pis.*;
import de.adorsys.psd2.xs2a.exception.WrongPaymentTypeException;
import de.adorsys.psd2.xs2a.service.mapper.AmountModelMapper;
import de.adorsys.psd2.xs2a.service.profile.StandardPaymentProductsResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentModelMapperPsd2 {
    private final CoreObjectsMapper coreObjectsMapper;
    private final TppRedirectUriMapper tppRedirectUriMapper;
    private final AmountModelMapper amountModelMapper;
    private final HrefLinkMapper hrefLinkMapper;
    private final ScaMethodsMapper scaMethodsMapper;
    private final StandardPaymentProductsResolver standardPaymentProductsResolver;
    private final Xs2aObjectMapper xs2aObjectMapper;

    public Object mapToGetPaymentResponse(CommonPayment commonPayment) {
        String rawData = convertResponseToRawData(commonPayment.getPaymentData());

        return standardPaymentProductsResolver.isRawPaymentProduct(commonPayment.getPaymentProduct())
                   ? rawData
                   : enrichPaymentWithAdditionalData(rawData, commonPayment);
    }

    private Object enrichPaymentWithAdditionalData(String rawData, CommonPayment commonPayment) {
        try {
            Map<String, String> map = xs2aObjectMapper.readValue(rawData, Map.class);
            map.put("transactionStatus", commonPayment.getTransactionStatus().toString());

            return map;
        } catch (JsonProcessingException e) {
            log.warn("Can't convert payment to map {}", e.getMessage());
            return rawData;
        }
    }

    public PaymentInitiationStatusResponse200Json mapToStatusResponseJson(GetPaymentStatusResponse response) {
        return new PaymentInitiationStatusResponse200Json()
                   .transactionStatus(mapToTransactionStatus(response.getTransactionStatus()))
                   .fundsAvailable(response.getFundsAvailable())
                   .psuMessage(response.getPsuMessage());
    }

    public byte[] mapToStatusResponseRaw(GetPaymentStatusResponse response) {
        return response.getPaymentStatusRaw();
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
        response201.setTppMessages(mapToTppMessage2XXList(response.getTppMessageInformation()));
        response201.setCurrencyConversionFee(amountModelMapper.mapToAmount(response.getCurrencyConversionFee()));
        response201.setEstimatedTotalAmount(amountModelMapper.mapToAmount(response.getEstimatedTotalAmount()));
        response201.setEstimatedInterbankSettlementAmount(amountModelMapper.mapToAmount(response.getEstimatedInterbankSettlementAmount()));

        return response201;
    }

    public PaymentInitiationParameters mapToPaymentRequestParameters(String paymentProduct, String paymentService, byte[] tpPSignatureCertificate, String tpPRedirectURI,
                                                                     String tpPNokRedirectURI, boolean tppExplicitAuthorisationPreferred, PsuIdData psuData,
                                                                     TppNotificationData tppNotificationData, String tppBrandLoggingInformation, String instanceId) {
        PaymentInitiationParameters parameters = new PaymentInitiationParameters();
        parameters.setPaymentType(PaymentType.getByValue(paymentService).orElseThrow(() -> new WrongPaymentTypeException(paymentService)));
        parameters.setPaymentProduct(Optional.ofNullable(paymentProduct).orElseThrow(() -> new IllegalArgumentException("Unsupported payment product")));
        parameters.setQwacCertificate(new String(Optional.ofNullable(tpPSignatureCertificate).orElse(new byte[]{}), StandardCharsets.UTF_8));
        parameters.setTppRedirectUri(tppRedirectUriMapper.mapToTppRedirectUri(tpPRedirectURI, tpPNokRedirectURI));
        parameters.setTppExplicitAuthorisationPreferred(tppExplicitAuthorisationPreferred);
        parameters.setPsuData(psuData);
        parameters.setTppNotificationData(tppNotificationData);
        parameters.setTppBrandLoggingInformation(tppBrandLoggingInformation);
        parameters.setInstanceId(instanceId);

        return parameters;
    }

    public PisPaymentCancellationRequest mapToPaymentCancellationRequest(String paymentProduct, String paymentService, String paymentId,
                                                                         Boolean tppExplicitAuthorisationPreferred,
                                                                         String tpPRedirectURI, String tpPNokRedirectURI) {
        return new PisPaymentCancellationRequest(
            PaymentType.getByValue(paymentService).orElseThrow(() -> new WrongPaymentTypeException(paymentService)),
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
        response.setTppMessages(mapToTppMessage2XXList(cancelPaymentResponse.getTppMessageInformation()));
        return response;
    }

    private TransactionStatus mapToTransactionStatus(de.adorsys.psd2.xs2a.core.pis.TransactionStatus responseObject) {
        return Optional.ofNullable(responseObject)
                   .map(r -> TransactionStatus.valueOf(r.name()))
                   .orElse(null);
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

    private List<TppMessage2XX> mapToTppMessage2XXList(Set<TppMessageInformation> tppMessages) {
        if (CollectionUtils.isEmpty(tppMessages)) {
            return null;
        }
        return tppMessages.stream()
                   .map(this::mapToTppMessage2XX)
                   .collect(Collectors.toList());
    }

    private TppMessage2XX mapToTppMessage2XX(TppMessageInformation tppMessage) {
        TppMessage2XX tppMessage2XX = new TppMessage2XX();
        tppMessage2XX.setCategory(TppMessageCategory.fromValue(tppMessage.getCategory().name()));
        tppMessage2XX.setCode(MessageCode2XX.WARNING);
        tppMessage2XX.setPath(tppMessage.getPath());
        tppMessage2XX.setText(tppMessage.getText());

        return tppMessage2XX;
    }
}
