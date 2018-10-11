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

import de.adorsys.aspsp.xs2a.spi.config.rest.AspspRemoteUrls;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.mapper.SpiPaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaMethod;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentType;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@AllArgsConstructor
public class SinglePaymentSpiImpl implements SinglePaymentSpi {
    @Qualifier("aspspRestTemplate")
    private final RestTemplate aspspRestTemplate;
    private final SpiPaymentMapper spiPaymentMapper;
    private final AspspRemoteUrls aspspRemoteUrls;

    @NotNull
    @Override
    public SpiResponse<SpiSinglePayment> initiatePayment(@NotNull SpiSinglePayment spiSinglePayment, @NotNull AspspConsentData initialAspspConsentData) {
        de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment request = spiPaymentMapper.mapToSpiSinglePayment(spiSinglePayment);
        ResponseEntity<de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment> responseEntity = aspspRestTemplate.postForEntity(aspspRemoteUrls.createPayment(), request, de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment.class);
        return new SpiResponse<>(spiPaymentMapper.mapToSpiSinglePayment(responseEntity.getBody(), spiSinglePayment.getPaymentProduct()), initialAspspConsentData);
    }

    @Override
    public SpiResponse<SpiResponse.VoidResponse> executePaymentWithoutSca(SpiPaymentType spiPaymentType, SpiSinglePayment payment, AspspConsentData aspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse<SpiSinglePayment> getPaymentById(SpiSinglePayment payment, AspspConsentData aspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse<SpiTransactionStatus> getPaymentStatusById(SpiSinglePayment payment, AspspConsentData aspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse<SpiAuthorisationStatus> authorisePsu(String psuId, String password, SpiSinglePayment payment, AspspConsentData aspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse<List<SpiScaMethod>> requestAvailableScaMethods(String psuId, SpiSinglePayment payment, AspspConsentData aspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(String psuId, SpiScaMethod scaMethod, SpiSinglePayment businessObject, AspspConsentData aspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse<SpiResponse.VoidResponse> verifyAuthorisationCodeAndExecuteRequest(SpiScaConfirmation spiScaConfirmation, SpiSinglePayment businessObject, AspspConsentData aspspConsentData) {
        return null;
    }
}
