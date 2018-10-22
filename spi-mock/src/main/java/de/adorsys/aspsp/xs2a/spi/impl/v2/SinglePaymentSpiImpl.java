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

package de.adorsys.aspsp.xs2a.spi.impl.v2;

import de.adorsys.aspsp.xs2a.exception.RestException;
import de.adorsys.aspsp.xs2a.spi.config.rest.AspspRemoteUrls;
import de.adorsys.aspsp.xs2a.spi.mapper.SpiSinglePaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiSinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import lombok.AllArgsConstructor;
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

@Service
@AllArgsConstructor
public class SinglePaymentSpiImpl implements SinglePaymentSpi {
    private static final String TEST_ASPSP_DATA = "Test aspsp data";

    @Qualifier("aspspRestTemplate")
    private final RestTemplate aspspRestTemplate;
    private final AspspRemoteUrls aspspRemoteUrls;
    private final SpiSinglePaymentMapper spiSinglePaymentMapper;

    @Override
    @NotNull
    public SpiResponse<SpiSinglePaymentInitiationResponse> initiatePayment(@NotNull SpiPsuData psuData, @NotNull SpiSinglePayment payment, @NotNull AspspConsentData initialAspspConsentData) {
        try {
            de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment request = spiSinglePaymentMapper.mapToAspspSpiSinglePayment(payment);

            ResponseEntity<de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment> responseEntity =
                aspspRestTemplate.postForEntity(aspspRemoteUrls.createPayment(), request, de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment.class);

            return SpiResponse.<SpiSinglePaymentInitiationResponse>builder()
                       .aspspConsentData(initialAspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                       .payload(spiSinglePaymentMapper.mapToSpiSinglePaymentResponse(responseEntity.getBody()))
                       .success();

        } catch (RestException e) {

            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return SpiResponse.<SpiSinglePaymentInitiationResponse>builder()
                           .aspspConsentData(initialAspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }
            return SpiResponse.<SpiSinglePaymentInitiationResponse>builder()
                       .aspspConsentData(initialAspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    @Override
    @NotNull
    public SpiResponse<SpiSinglePayment> getPaymentById(@NotNull SpiPsuData psuData, @NotNull SpiSinglePayment payment, @NotNull AspspConsentData initialAspspConsentData) {
        try {
            ResponseEntity<List<de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment>> aspspResponse =
                aspspRestTemplate.exchange(aspspRemoteUrls.getPaymentById(), HttpMethod.GET, null, new ParameterizedTypeReference<List<de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment>>() {
                }, payment.getPaymentType().getValue(), payment.getPaymentProduct().getValue(), payment.getPaymentId());
            de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment single = aspspResponse.getBody().get(0);
            SpiSinglePayment spiPeriodicPayment = spiSinglePaymentMapper.mapToSpiSinglePayment(single, payment.getPaymentProduct());

            return SpiResponse.<SpiSinglePayment>builder()
                       .aspspConsentData(initialAspspConsentData)
                       .payload(spiPeriodicPayment)
                       .success();

        } catch (RestException e) {

            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {

                return SpiResponse.<SpiSinglePayment>builder()
                           .aspspConsentData(initialAspspConsentData)
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }

            return SpiResponse.<SpiSinglePayment>builder()
                       .aspspConsentData(initialAspspConsentData)
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    @Override
    @NotNull
    public SpiResponse<SpiTransactionStatus> getPaymentStatusById(@NotNull SpiPsuData psuData, @NotNull SpiSinglePayment payment, @NotNull AspspConsentData initialAspspConsentData) {
        try {
            ResponseEntity<SpiTransactionStatus> aspspResponse = aspspRestTemplate.getForEntity(aspspRemoteUrls.getPaymentStatus(), SpiTransactionStatus.class, payment.getPaymentId());
            SpiTransactionStatus status = aspspResponse.getBody();

            return SpiResponse.<SpiTransactionStatus>builder()
                       .aspspConsentData(initialAspspConsentData)
                       .payload(status)
                       .success();

        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return SpiResponse.<SpiTransactionStatus>builder()
                           .aspspConsentData(initialAspspConsentData)
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }
            return SpiResponse.<SpiTransactionStatus>builder()
                       .aspspConsentData(initialAspspConsentData)
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    @Override
    @NotNull
    public SpiResponse<SpiResponse.VoidResponse> executePaymentWithoutSca(@NotNull SpiPsuData psuData, @NotNull SpiSinglePayment payment, @NotNull AspspConsentData initialAspspConsentData) {
        de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment request = spiSinglePaymentMapper.mapToAspspSpiSinglePayment(payment);

        try {
            aspspRestTemplate.postForEntity(aspspRemoteUrls.createPayment(), request, de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment.class);

            return SpiResponse.<SpiResponse.VoidResponse>builder()
                       .aspspConsentData(initialAspspConsentData)
                       .success();

        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return SpiResponse.<SpiResponse.VoidResponse>builder()
                           .aspspConsentData(initialAspspConsentData)
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }
            return SpiResponse.<SpiResponse.VoidResponse>builder()
                       .aspspConsentData(initialAspspConsentData)
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    @Override
    @NotNull
    public SpiResponse<SpiResponse.VoidResponse> verifyScaAuthorisationAndExecutePayment(@NotNull SpiPsuData psuData, @NotNull SpiScaConfirmation spiScaConfirmation, @NotNull SpiSinglePayment payment, @NotNull AspspConsentData initialAspspConsentData) {
        try {
            aspspRestTemplate.exchange(aspspRemoteUrls.applyStrongUserAuthorisation(), HttpMethod.PUT, new HttpEntity<>(spiScaConfirmation), ResponseEntity.class);
            de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment request = spiSinglePaymentMapper.mapToAspspSpiSinglePayment(payment);
            aspspRestTemplate.postForEntity(aspspRemoteUrls.createPayment(), request, de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment.class);

            return SpiResponse.<SpiResponse.VoidResponse>builder()
                       .aspspConsentData(initialAspspConsentData)
                       .success();

        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {

                return SpiResponse.<SpiResponse.VoidResponse>builder()
                           .aspspConsentData(initialAspspConsentData)
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }

            return SpiResponse.<SpiResponse.VoidResponse>builder()
                       .aspspConsentData(initialAspspConsentData)
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }
}
