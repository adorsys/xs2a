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

import de.adorsys.aspsp.xs2a.component.JsonConverter;
import de.adorsys.aspsp.xs2a.exception.RestException;
import de.adorsys.aspsp.xs2a.spi.config.rest.AspspRemoteUrls;
import de.adorsys.aspsp.xs2a.spi.impl.service.KeycloakInvokerService;
import de.adorsys.aspsp.xs2a.spi.mapper.SpiBulkPaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiBulkPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.BulkPaymentSpi;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
public class BulkPaymentSpiImpl implements BulkPaymentSpi {
    private static final String TEST_ASPSP_DATA = "Test aspsp data";

    @Qualifier("aspspRestTemplate")
    private final RestTemplate aspspRestTemplate;
    private final AspspRemoteUrls aspspRemoteUrls;
    private final SpiBulkPaymentMapper spiBulkPaymentMapper;
    private final KeycloakInvokerService keycloakInvokerService;
    private final JsonConverter jsonConverter;

    @Override
    @NotNull
    public SpiResponse<SpiBulkPaymentInitiationResponse> initiatePayment(@NotNull SpiPsuData psuData, @NotNull SpiBulkPayment spiBulkPayment, @NotNull AspspConsentData initialAspspConsentData) {
        try {
            de.adorsys.aspsp.xs2a.spi.domain.payment.SpiBulkPayment request = spiBulkPaymentMapper.mapToAspspSpiBulkPayment(spiBulkPayment);
            ResponseEntity<de.adorsys.aspsp.xs2a.spi.domain.payment.SpiBulkPayment> responseEntity = aspspRestTemplate.postForEntity(aspspRemoteUrls.createBulkPayment(), request, de.adorsys.aspsp.xs2a.spi.domain.payment.SpiBulkPayment.class);
            SpiBulkPaymentInitiationResponse response = spiBulkPaymentMapper.mapToSpiBulkPaymentResponse(responseEntity.getBody(), spiBulkPayment.getPaymentProduct());

            return SpiResponse.<SpiBulkPaymentInitiationResponse>builder()
                       .aspspConsentData(initialAspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                       .payload(response)
                       .success();

        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return SpiResponse.<SpiBulkPaymentInitiationResponse>builder()
                           .aspspConsentData(initialAspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }
            return SpiResponse.<SpiBulkPaymentInitiationResponse>builder()
                       .aspspConsentData(initialAspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    @Override
    public @NotNull SpiResponse<SpiResponse.VoidResponse> executePaymentWithoutSca(@NotNull SpiPsuData psuData, @NotNull SpiBulkPayment payment, @NotNull AspspConsentData aspspConsentData) {
        return SpiResponse.<SpiResponse.VoidResponse>builder().fail(SpiResponseStatus.NOT_SUPPORTED);
    }

    @Override
    public @NotNull SpiResponse<SpiResponse.VoidResponse> verifyScaAuthorisationAndExecutePayment(@NotNull SpiPsuData psuData, @NotNull SpiScaConfirmation spiScaConfirmation, @NotNull SpiBulkPayment payment, @NotNull AspspConsentData aspspConsentData) {
        return SpiResponse.<SpiResponse.VoidResponse>builder().fail(SpiResponseStatus.NOT_SUPPORTED);
    }
}
