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

package de.adorsys.aspsp.xs2a.spi.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import de.adorsys.aspsp.xs2a.spi.config.rest.AspspRemoteUrls;
import de.adorsys.aspsp.xs2a.spi.mapper.SpiPaymentMapper;
import de.adorsys.aspsp.xs2a.spi.mapper.SpiPeriodicPaymentMapper;
import de.adorsys.psd2.aspsp.mock.api.common.AspspTransactionStatus;
import de.adorsys.psd2.aspsp.mock.api.payment.AspspPeriodicPayment;
import de.adorsys.psd2.aspsp.mock.api.psu.AspspPsuData;
import de.adorsys.psd2.xs2a.component.JsonConverter;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.exception.RestException;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentExecutionResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPeriodicPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.PeriodicPaymentSpi;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PeriodicPaymentSpiImpl implements PeriodicPaymentSpi {
    private static final String TEST_ASPSP_DATA = "Test aspsp data";

    @Qualifier("aspspRestTemplate")
    private final RestTemplate aspspRestTemplate;
    private final AspspRemoteUrls aspspRemoteUrls;
    private final SpiPaymentMapper spiPaymentMapper;
    private final SpiPeriodicPaymentMapper spiPeriodicPaymentMapper;
    private final JsonConverter jsonConverter;

    @Override
    @NotNull
    public SpiResponse<SpiPeriodicPaymentInitiationResponse> initiatePayment(@NotNull SpiContextData spiContextData, @NotNull SpiPeriodicPayment payment, @NotNull AspspConsentData initialAspspConsentData) {
        try {
            AspspPeriodicPayment request = spiPeriodicPaymentMapper.mapToAspspPeriodicPayment(payment, TransactionStatus.RCVD);

            ResponseEntity<AspspPeriodicPayment> aspspResponse =
                aspspRestTemplate.postForEntity(aspspRemoteUrls.createPeriodicPayment(), request, AspspPeriodicPayment.class);
            AspspConsentData resultAspspData = initialAspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes());
            List<AspspPsuData> psuDataList = aspspResponse.getBody().getPsuDataList();

            if (CollectionUtils.size(psuDataList) > 1) {
                Map<String, Boolean> authMap = psuDataList.stream()
                                                   .map(AspspPsuData::getPsuId)
                                                   .collect(Collectors.toMap(Function.identity(), id -> false));
                byte[] bytes = jsonConverter.toJson(authMap)
                                   .map(String::getBytes)
                                   .orElse(TEST_ASPSP_DATA.getBytes());

                resultAspspData = initialAspspConsentData.respondWith(bytes);
            }

            return SpiResponse.<SpiPeriodicPaymentInitiationResponse>builder()
                       .aspspConsentData(resultAspspData)
                       .payload(spiPeriodicPaymentMapper.mapToSpiPeriodicPaymentResponse(aspspResponse.getBody()))
                       .success();

        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return SpiResponse.<SpiPeriodicPaymentInitiationResponse>builder()
                           .aspspConsentData(initialAspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }
            return SpiResponse.<SpiPeriodicPaymentInitiationResponse>builder()
                       .aspspConsentData(initialAspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    @Override
    @NotNull
    public SpiResponse<SpiPeriodicPayment> getPaymentById(@NotNull SpiContextData spiContextData, @NotNull SpiPeriodicPayment payment, @NotNull AspspConsentData aspspConsentData) {
        try {
            ResponseEntity<List<AspspPeriodicPayment>> aspspResponse =
                aspspRestTemplate.exchange(aspspRemoteUrls.getPaymentById(), HttpMethod.GET, null, new ParameterizedTypeReference<List<AspspPeriodicPayment>>() {
                }, payment.getPaymentType().getValue(), payment.getPaymentProduct(), payment.getPaymentId());
            AspspPeriodicPayment periodic = aspspResponse.getBody().get(0);
            SpiPeriodicPayment spiPeriodicPayment = spiPeriodicPaymentMapper.mapToSpiPeriodicPayment(periodic, payment.getPaymentProduct());

            return SpiResponse.<SpiPeriodicPayment>builder()
                       .aspspConsentData(aspspConsentData)
                       .payload(spiPeriodicPayment)
                       .success();

        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return SpiResponse.<SpiPeriodicPayment>builder()
                           .aspspConsentData(aspspConsentData)
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }
            return SpiResponse.<SpiPeriodicPayment>builder()
                       .aspspConsentData(aspspConsentData)
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    @Override
    @NotNull
    public SpiResponse<TransactionStatus> getPaymentStatusById(@NotNull SpiContextData spiContextData, @NotNull SpiPeriodicPayment payment, @NotNull AspspConsentData aspspConsentData) {
        try {
            ResponseEntity<AspspTransactionStatus> aspspResponse = aspspRestTemplate.getForEntity(aspspRemoteUrls.getPaymentStatus(), AspspTransactionStatus.class, payment.getPaymentId());
            TransactionStatus status = spiPaymentMapper.mapToTransactionStatus(aspspResponse.getBody());

            return SpiResponse.<TransactionStatus>builder()
                       .aspspConsentData(aspspConsentData)
                       .payload(status)
                       .success();

        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return SpiResponse.<TransactionStatus>builder()
                           .aspspConsentData(aspspConsentData)
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }
            return SpiResponse.<TransactionStatus>builder()
                       .aspspConsentData(aspspConsentData)
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    @Override
    @NotNull
    public SpiResponse<SpiPaymentExecutionResponse> executePaymentWithoutSca(@NotNull SpiContextData spiContextData, @NotNull SpiPeriodicPayment payment, @NotNull AspspConsentData aspspConsentData) {
        TransactionStatus responseStatus = TransactionStatus.ACCP;
        AspspConsentData responseData = aspspConsentData;

        if (aspspConsentData.getAspspConsentData() != null) {
            Optional<Map<String, Boolean>> authMapOptional = jsonConverter.toObject(aspspConsentData.getAspspConsentData(), new TypeReference<Map<String, Boolean>>() {});

            if (authMapOptional.isPresent()) {
                Map<String, Boolean> authMap = authMapOptional.get();
                String psuId = spiContextData.getPsuData().getPsuId();

                if (!authMap.containsKey(psuId)) {
                    return SpiResponse.<SpiPaymentExecutionResponse>builder()
                               .aspspConsentData(responseData)
                               .fail(SpiResponseStatus.LOGICAL_FAILURE);
                }

                authMap.put(psuId, true);

                if (authMap.values().contains(false)) {
                    responseStatus = TransactionStatus.PATC;
                } else {
                    responseStatus = TransactionStatus.ACTC;
                }

                byte[] bytes = jsonConverter.toJson(authMap)
                                   .map(String::getBytes)
                                   .orElse(TEST_ASPSP_DATA.getBytes());
                responseData = aspspConsentData.respondWith(bytes);
            }
        }

        AspspPeriodicPayment request = spiPeriodicPaymentMapper.mapToAspspPeriodicPayment(payment, responseStatus);

        try {
            aspspRestTemplate.postForEntity(aspspRemoteUrls.createPeriodicPayment(), request, AspspPeriodicPayment.class);

            return SpiResponse.<SpiPaymentExecutionResponse>builder()
                       .aspspConsentData(responseData)
                       .payload(new SpiPaymentExecutionResponse(responseStatus))
                       .success();
        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return SpiResponse.<SpiPaymentExecutionResponse>builder()
                           .aspspConsentData(responseData)
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }
            return SpiResponse.<SpiPaymentExecutionResponse>builder()
                       .aspspConsentData(responseData)
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    @Override
    @NotNull
    public SpiResponse<SpiPaymentExecutionResponse> verifyScaAuthorisationAndExecutePayment(@NotNull SpiContextData spiContextData, @NotNull SpiScaConfirmation spiScaConfirmation, @NotNull SpiPeriodicPayment payment, @NotNull AspspConsentData aspspConsentData) {
        TransactionStatus responseStatus = TransactionStatus.ACCP;
        AspspConsentData responseData = aspspConsentData;

        try {
            aspspRestTemplate.exchange(aspspRemoteUrls.applyStrongUserAuthorisation(), HttpMethod.PUT, new HttpEntity<>(spiScaConfirmation), ResponseEntity.class);

            if (aspspConsentData.getAspspConsentData() != null) {
                Optional<Map<String, Boolean>> authMapOptional = jsonConverter.toObject(aspspConsentData.getAspspConsentData(), new TypeReference<Map<String, Boolean>>() {});

                if (authMapOptional.isPresent()) {
                    Map<String, Boolean> authMap = authMapOptional.get();
                    String psuId = spiContextData.getPsuData().getPsuId();

                    if (!authMap.containsKey(psuId)) {
                        return SpiResponse.<SpiPaymentExecutionResponse>builder()
                                   .aspspConsentData(responseData)
                                   .fail(SpiResponseStatus.LOGICAL_FAILURE);
                    }

                    authMap.put(psuId, true);

                    if (authMap.values().contains(false)) {
                        responseStatus = TransactionStatus.PATC;
                    } else {
                        responseStatus = TransactionStatus.ACTC;
                    }

                    byte[] bytes = jsonConverter.toJson(authMap)
                                       .map(String::getBytes)
                                       .orElse(TEST_ASPSP_DATA.getBytes());
                    responseData = aspspConsentData.respondWith(bytes);
                }
            }

            AspspPeriodicPayment request = spiPeriodicPaymentMapper.mapToAspspPeriodicPayment(payment, responseStatus);
            aspspRestTemplate.postForEntity(aspspRemoteUrls.createPeriodicPayment(), request, AspspPeriodicPayment.class);

            return SpiResponse.<SpiPaymentExecutionResponse>builder()
                       .aspspConsentData(responseData)
                       .payload(new SpiPaymentExecutionResponse(responseStatus))
                       .success();

        } catch (RestException e) {
            SpiResponseStatus spiResponseStatus = SpiResponseStatus.LOGICAL_FAILURE;
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
                spiResponseStatus = SpiResponseStatus.TECHNICAL_FAILURE;
            } else if (e.getHttpStatus() == HttpStatus.UNAUTHORIZED) {
                spiResponseStatus = SpiResponseStatus.UNAUTHORIZED_FAILURE;
            }
            return SpiResponse.<SpiPaymentExecutionResponse>builder()
                       .aspspConsentData(responseData)
                       .fail(spiResponseStatus);
        }
    }
}
