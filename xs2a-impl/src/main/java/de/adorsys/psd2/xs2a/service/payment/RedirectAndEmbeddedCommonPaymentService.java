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

package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.CommonPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CommonPaymentSpi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedirectAndEmbeddedCommonPaymentService implements ScaCommonPaymentService {

    private final CommonPaymentSpi commonPaymentSpi;
    private final SpiToXs2aPaymentMapper spiToXs2aPaymentMapper;
    private final SpiContextDataProvider spiContextDataProvider;
    private final SpiErrorMapper spiErrorMapper;

    @Override
    public CommonPaymentInitiationResponse createPayment(CommonPayment payment, TppInfo tppInfo, String paymentProduct, PsuIdData psuIdData) {
        SpiContextData spiContextData = spiContextDataProvider.provide(psuIdData, tppInfo);

        SpiResponse<SpiPaymentInitiationResponse> spiResponse = commonPaymentSpi.initiatePayment(spiContextData, mapToSpiPaymentRequest(payment, paymentProduct), AspspConsentData.emptyConsentData());

        if (spiResponse.hasError()) {
            return new CommonPaymentInitiationResponse(spiErrorMapper.mapToErrorHolder(spiResponse));
        }

        return spiToXs2aPaymentMapper.mapToCommonPaymentInitiateResponse(spiResponse.getPayload(), payment.getPaymentType(), spiResponse.getAspspConsentData());
    }

    private SpiPaymentInfo mapToSpiPaymentRequest(CommonPayment payment, String paymentProduct) {
        SpiPaymentInfo request = new SpiPaymentInfo(paymentProduct);
        request.setPaymentId(payment.getPaymentId());
        request.setPaymentType(payment.getPaymentType());
        request.setPaymentData(payment.getPaymentData());

        return request;
    }
}
