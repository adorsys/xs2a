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
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aChosenScaMethod;
import de.adorsys.psd2.xs2a.domain.pis.*;
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
import java.util.Map;
import java.util.Optional;

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

    public Object mapToGetPaymentResponse(Object payment, String paymentProduct) {
        CommonPayment commonPayment = (CommonPayment) payment;
        String rawData = convertResponseToRawData(commonPayment.getPaymentData());

        return standardPaymentProductsResolver.isRawPaymentProduct(paymentProduct)
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
                   .fundsAvailable(response.getFundsAvailable());
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
        return response201;
    }

    public PaymentInitiationParameters mapToPaymentRequestParameters(String paymentProduct, String paymentService, byte[] tpPSignatureCertificate, String tpPRedirectURI,
                                                                     String tpPNokRedirectURI, boolean tppExplicitAuthorisationPreferred, PsuIdData psuData,
                                                                     String tppNotificationUri, List<NotificationSupportedMode> notificationModes) {
        PaymentInitiationParameters parameters = new PaymentInitiationParameters();
        parameters.setPaymentType(PaymentType.getByValue(paymentService).orElseThrow(() -> new IllegalArgumentException("Unsupported payment service")));
        parameters.setPaymentProduct(Optional.ofNullable(paymentProduct).orElseThrow(() -> new IllegalArgumentException("Unsupported payment product")));
        parameters.setQwacCertificate(new String(Optional.ofNullable(tpPSignatureCertificate).orElse(new byte[]{}), StandardCharsets.UTF_8));
        parameters.setTppRedirectUri(tppRedirectUriMapper.mapToTppRedirectUri(tpPRedirectURI, tpPNokRedirectURI));
        parameters.setTppExplicitAuthorisationPreferred(tppExplicitAuthorisationPreferred);
        parameters.setPsuData(psuData);
        parameters.setTppNotificationUri(tppNotificationUri);
        parameters.setNotificationSupportedModes(notificationModes);

        return parameters;
    }

    public PisPaymentCancellationRequest mapToPaymentCancellationRequest(String paymentProduct, String paymentService, String paymentId,
                                                                         Boolean tppExplicitAuthorisationPreferred,
                                                                         String tpPRedirectURI, String tpPNokRedirectURI) {
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
}
