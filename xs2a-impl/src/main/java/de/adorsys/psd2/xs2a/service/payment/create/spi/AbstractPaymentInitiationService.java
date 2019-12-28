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

package de.adorsys.psd2.xs2a.service.payment.create.spi;

import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.ErrorPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Common service for initiating payments in SPI
 *
 * @param <T> type of payments handled by this service
 * @param <R> type of SPI response on payment initiation
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractPaymentInitiationService<T extends CommonPayment, R extends SpiPaymentInitiationResponse> implements PaymentInitiationService<T> {
    private final SpiContextDataProvider spiContextDataProvider;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final SpiErrorMapper spiErrorMapper;

    @Override
    public PaymentInitiationResponse initiatePayment(T payment, String paymentProduct, PsuIdData psuIdData) {
        SpiContextData spiContextData = spiContextDataProvider.provideWithPsuIdData(psuIdData);
        InitialSpiAspspConsentDataProvider aspspConsentDataProvider =
            aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider();

        SpiResponse<R> spiResponse = initiateSpiPayment(spiContextData, payment, paymentProduct, aspspConsentDataProvider);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);
            return new ErrorPaymentInitiationResponse(errorHolder);
        }

        return mapToXs2aResponse(spiResponse.getPayload(), aspspConsentDataProvider, payment.getPaymentType());
    }

    protected abstract SpiResponse<R> initiateSpiPayment(SpiContextData spiContextData, T payment, String paymentProduct,
                                                         InitialSpiAspspConsentDataProvider aspspConsentDataProvider);

    protected abstract PaymentInitiationResponse mapToXs2aResponse(R spiResponse, InitialSpiAspspConsentDataProvider provider, PaymentType paymentType);
}
