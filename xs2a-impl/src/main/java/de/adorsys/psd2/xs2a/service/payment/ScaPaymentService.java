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
import de.adorsys.psd2.xs2a.domain.pis.*;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.*;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiBulkPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPeriodicPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiSinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.BulkPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.CommonPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.PeriodicPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public abstract class ScaPaymentService {
    private final SinglePaymentSpi singlePaymentSpi;
    private final PeriodicPaymentSpi periodicPaymentSpi;
    private final BulkPaymentSpi bulkPaymentSpi;
    private final CommonPaymentSpi commonPaymentSpi;
    private final Xs2aToSpiSinglePaymentMapper xs2AToSpiSinglePaymentMapper;
    private final Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper;
    private final Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper;
    private final Xs2aToSpiPaymentInfo xs2aToSpiPaymentInfo;
    private final SpiToXs2aPaymentMapper spiToXs2aPaymentMapper;
    private final SpiContextDataProvider spiContextDataProvider;
    private final SpiErrorMapper spiErrorMapper;

    public SinglePaymentInitiationResponse createSinglePayment(SinglePayment payment, TppInfo tppInfo, String paymentProduct, PsuIdData psuIdData) {
        SpiContextData spiContextData = spiContextDataProvider.provideWithPsuIdData(psuIdData);

        SpiResponse<SpiSinglePaymentInitiationResponse> spiResponse = singlePaymentSpi.initiatePayment(spiContextData, xs2AToSpiSinglePaymentMapper.mapToSpiSinglePayment(payment, paymentProduct), AspspConsentData.emptyConsentData());

        if (spiResponse.hasError()) {
            return new SinglePaymentInitiationResponse(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS));
        }

        return spiToXs2aPaymentMapper.mapToPaymentInitiateResponse(spiResponse.getPayload(), SinglePaymentInitiationResponse::new, spiResponse.getAspspConsentData());
    }

    public PeriodicPaymentInitiationResponse createPeriodicPayment(PeriodicPayment payment, TppInfo tppInfo, String paymentProduct, PsuIdData psuIdData) {
        SpiContextData spiContextData = spiContextDataProvider.provideWithPsuIdData(psuIdData);

        SpiResponse<SpiPeriodicPaymentInitiationResponse> spiResponse = periodicPaymentSpi.initiatePayment(spiContextData, xs2aToSpiPeriodicPaymentMapper.mapToSpiPeriodicPayment(payment, paymentProduct), AspspConsentData.emptyConsentData());

        if (spiResponse.hasError()) {
            return new PeriodicPaymentInitiationResponse(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS));
        }

        return spiToXs2aPaymentMapper.mapToPaymentInitiateResponse(spiResponse.getPayload(), PeriodicPaymentInitiationResponse::new, spiResponse.getAspspConsentData());
    }

    public BulkPaymentInitiationResponse createBulkPayment(BulkPayment bulkPayment, TppInfo tppInfo, String paymentProduct, PsuIdData psuIdData) {
        SpiContextData spiContextData = spiContextDataProvider.provideWithPsuIdData(psuIdData);

        SpiResponse<SpiBulkPaymentInitiationResponse> spiResponse = bulkPaymentSpi.initiatePayment(spiContextData, xs2aToSpiBulkPaymentMapper.mapToSpiBulkPayment(bulkPayment, paymentProduct), AspspConsentData.emptyConsentData());

        if (spiResponse.hasError()) {
            return new BulkPaymentInitiationResponse(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS));
        }

        return spiToXs2aPaymentMapper.mapToPaymentInitiateResponse(spiResponse.getPayload(), BulkPaymentInitiationResponse::new, spiResponse.getAspspConsentData());
    }

    public CommonPaymentInitiationResponse createCommonPayment(CommonPayment payment, TppInfo tppInfo, String paymentProduct, PsuIdData psuIdData) {
        SpiContextData spiContextData = spiContextDataProvider.provide(psuIdData, tppInfo);

        SpiResponse<SpiPaymentInitiationResponse> spiResponse = commonPaymentSpi.initiatePayment(spiContextData, xs2aToSpiPaymentInfo.mapToSpiPaymentRequest(payment, paymentProduct), AspspConsentData.emptyConsentData());

        if (spiResponse.hasError()) {
            return new CommonPaymentInitiationResponse(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS));
        }

        return spiToXs2aPaymentMapper.mapToCommonPaymentInitiateResponse(spiResponse.getPayload(), payment.getPaymentType(), spiResponse.getAspspConsentData());
    }
}
