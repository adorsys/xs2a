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
import de.adorsys.aspsp.xs2a.spi.mapper.SpiPaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiSinglePaymentInitiateResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
public class SinglePaymentSpiImpl implements SinglePaymentSpi {
    @Qualifier("aspspRestTemplate")
    private final RestTemplate aspspRestTemplate;
    private final SpiPaymentMapper spiPaymentMapper;
    private final AspspRemoteUrls aspspRemoteUrls;

    @Override
    @NotNull
    public SpiResponse<SpiSinglePaymentInitiateResponse> initiatePayment(@NotNull SpiPsuData psuData, @NotNull SpiSinglePayment spiSinglePayment, @NotNull AspspConsentData initialAspspConsentData) {
        try {
            de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment request = spiPaymentMapper.mapToSpiSinglePayment(spiSinglePayment);
            ResponseEntity<de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment> responseEntity = aspspRestTemplate.postForEntity(aspspRemoteUrls.createPayment(), request, de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment.class);
            return new SpiResponse<>(spiPaymentMapper.mapToSpiSinglePaymentResponse(responseEntity.getBody()), initialAspspConsentData);
        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return SpiResponse.<SpiSinglePaymentInitiateResponse>builder()
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }
            return SpiResponse.<SpiSinglePaymentInitiateResponse>builder()
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }
}
