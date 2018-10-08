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
import de.adorsys.aspsp.xs2a.spi.domain.SpiResponse;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiScaMethod;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentType;
import de.adorsys.aspsp.xs2a.spi.domain.v2.SpiSinglePayment;
import de.adorsys.aspsp.xs2a.spi.mapper.v2.NewSpiPaymentMapper;
import de.adorsys.aspsp.xs2a.spi.service.v2.SinglePaymentSpi;
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
    private final NewSpiPaymentMapper newSpiPaymentMapper;
    private final de.adorsys.aspsp.xs2a.spi.mapper.SpiPaymentMapper spiPaymentMapper;
    private final AspspRemoteUrls aspspRemoteUrls;

    @NotNull
    @Override
    public SpiResponse<SpiSinglePayment> initiatePayment(SpiSinglePayment spiSinglePayment, @NotNull AspspConsentData initialAspspConsentData) {
        de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment request = newSpiPaymentMapper.mapToSpiSinglePayment(spiSinglePayment);
        ResponseEntity<de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment> responseEntity = aspspRestTemplate.postForEntity(aspspRemoteUrls.createPayment(), request, de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment.class);
        return new SpiResponse<>(newSpiPaymentMapper.mapToSpiSinglePayment(responseEntity.getBody(), spiSinglePayment.getPaymentProduct()), initialAspspConsentData);
    }

    @Override
    public SpiResponse executePaymentWithoutSca(SpiPaymentType spiPaymentType, SpiSinglePayment payment, AspspConsentData aspspConsentData) {
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
    public SpiResponse requestAuthorisationCode(String psuId, SpiScaMethod scaMethod, SpiSinglePayment payment, AspspConsentData aspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse verifyAuthorisationCodeAndExecuteRequest(SpiScaConfirmation spiScaConfirmation, SpiSinglePayment payment, AspspConsentData aspspConsentData) {
        return null;
    }
}
