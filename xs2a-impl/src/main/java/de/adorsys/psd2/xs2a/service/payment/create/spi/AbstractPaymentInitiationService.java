/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
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
