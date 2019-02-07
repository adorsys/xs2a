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

import de.adorsys.aspsp.xs2a.spi.config.rest.AspspRemoteUrls;
import de.adorsys.aspsp.xs2a.spi.mapper.SpiPaymentInfoMapper;
import de.adorsys.aspsp.xs2a.spi.mapper.SpiPaymentMapper;
import de.adorsys.psd2.aspsp.mock.api.common.AspspTransactionStatus;
import de.adorsys.psd2.aspsp.mock.api.payment.AspspPaymentInfo;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.exception.RestException;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentExecutionResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.CommonPaymentSpi;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
public class CommonPaymentSpiImpl implements CommonPaymentSpi {
    private static final String TEST_ASPSP_DATA = "Test aspsp data";

    @Qualifier("aspspRestTemplate")
    private final RestTemplate aspspRestTemplate;
    private final AspspRemoteUrls aspspRemoteUrls;
    private final SpiPaymentMapper spiPaymentMapper;
    private final SpiPaymentInfoMapper spiPaymentInfoMapper;

    @Override
    @NotNull
    public SpiResponse<SpiPaymentInitiationResponse> initiatePayment(@NotNull SpiContextData spiContextData, @NotNull SpiPaymentInfo payment, @NotNull AspspConsentData initialAspspConsentData) {
        try {
            AspspPaymentInfo request = spiPaymentInfoMapper.mapToAspspPayment(payment, TransactionStatus.RCVD);

            ResponseEntity<AspspPaymentInfo> responseEntity =
                aspspRestTemplate.postForEntity(aspspRemoteUrls.createCommonPayment(), request, AspspPaymentInfo.class);

            return SpiResponse.<SpiPaymentInitiationResponse>builder()
                       .aspspConsentData(initialAspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                       .payload(spiPaymentInfoMapper.mapToSpiPaymentInitiationResponse(responseEntity.getBody()))
                       .success();

        } catch (RestException e) {

            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return SpiResponse.<SpiPaymentInitiationResponse>builder()
                           .aspspConsentData(initialAspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }
            return SpiResponse.<SpiPaymentInitiationResponse>builder()
                       .aspspConsentData(initialAspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    @Override
    @NotNull
    public SpiResponse<SpiPaymentInfo> getPaymentById(@NotNull SpiContextData spiContextData, @NotNull SpiPaymentInfo payment, @NotNull AspspConsentData aspspConsentData) {
        try {

            ResponseEntity<AspspPaymentInfo> aspspResponse = aspspRestTemplate.getForEntity(aspspRemoteUrls.getCommonPaymentById(), AspspPaymentInfo.class, payment.getPaymentId());

            AspspPaymentInfo aspspPaymentInfo = aspspResponse.getBody();
            SpiPaymentInfo spiPaymentInfo = spiPaymentInfoMapper.mapToSpiPaymentInfo(aspspPaymentInfo);

            return SpiResponse.<SpiPaymentInfo>builder()
                       .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                       .payload(spiPaymentInfo)
                       .success();

        } catch (RestException e) {

            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {

                return SpiResponse.<SpiPaymentInfo>builder()
                           .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }

            return SpiResponse.<SpiPaymentInfo>builder()
                       .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    @Override
    @NotNull
    public SpiResponse<TransactionStatus> getPaymentStatusById(@NotNull SpiContextData spiContextData, @NotNull SpiPaymentInfo payment, @NotNull AspspConsentData aspspConsentData) {
        try {
            ResponseEntity<AspspTransactionStatus> aspspResponse = aspspRestTemplate.getForEntity(aspspRemoteUrls.getCommonPaymentStatus(), AspspTransactionStatus.class, payment.getPaymentId());
            TransactionStatus status = spiPaymentMapper.mapToTransactionStatus(aspspResponse.getBody());

            return SpiResponse.<TransactionStatus>builder()
                       .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                       .payload(status)
                       .success();

        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return SpiResponse.<TransactionStatus>builder()
                           .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }
            return SpiResponse.<TransactionStatus>builder()
                       .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    @Override
    @NotNull
    public SpiResponse<SpiPaymentExecutionResponse> executePaymentWithoutSca(@NotNull SpiContextData spiContextData, @NotNull SpiPaymentInfo payment, @NotNull AspspConsentData aspspConsentData) {
        AspspPaymentInfo request = spiPaymentInfoMapper.mapToAspspPayment(payment, TransactionStatus.ACCP);

        try {
            aspspRestTemplate.postForEntity(aspspRemoteUrls.createCommonPayment(), request, AspspPaymentInfo.class);

            return SpiResponse.<SpiPaymentExecutionResponse>builder()
                       .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                       .payload(new SpiPaymentExecutionResponse(TransactionStatus.ACCP))
                       .success();

        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return SpiResponse.<SpiPaymentExecutionResponse>builder()
                           .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }
            return SpiResponse.<SpiPaymentExecutionResponse>builder()
                       .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    @Override
    @NotNull
    public SpiResponse<SpiPaymentExecutionResponse> verifyScaAuthorisationAndExecutePayment(@NotNull SpiContextData spiContextData, @NotNull SpiScaConfirmation spiScaConfirmation, @NotNull SpiPaymentInfo payment, @NotNull AspspConsentData aspspConsentData) {
        try {
            aspspRestTemplate.exchange(aspspRemoteUrls.applyStrongUserAuthorisation(), HttpMethod.PUT, new HttpEntity<>(spiScaConfirmation), ResponseEntity.class);

            AspspPaymentInfo request = spiPaymentInfoMapper.mapToAspspPayment(payment, TransactionStatus.ACCP);
            aspspRestTemplate.postForEntity(aspspRemoteUrls.createCommonPayment(), request, AspspPaymentInfo.class);


            return SpiResponse.<SpiPaymentExecutionResponse>builder()
                       .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                       .payload(new SpiPaymentExecutionResponse(TransactionStatus.ACCP))
                       .success();

        } catch (RestException e) {
            SpiResponseStatus spiResponseStatus = SpiResponseStatus.LOGICAL_FAILURE;
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
                spiResponseStatus = SpiResponseStatus.TECHNICAL_FAILURE;
            } else if (e.getHttpStatus() == HttpStatus.UNAUTHORIZED) {
                spiResponseStatus = SpiResponseStatus.UNAUTHORIZED_FAILURE;
            }
            return SpiResponse.<SpiPaymentExecutionResponse>builder()
                       .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                       .fail(spiResponseStatus);
        }
    }
}
