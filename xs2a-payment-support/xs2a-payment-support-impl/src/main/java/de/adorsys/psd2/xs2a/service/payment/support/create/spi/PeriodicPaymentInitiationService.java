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

package de.adorsys.psd2.xs2a.service.payment.support.create.spi;

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPeriodicPaymentMapper;
import de.adorsys.psd2.xs2a.service.payment.create.spi.AbstractPaymentInitiationService;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPeriodicPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PeriodicPaymentSpi;
import org.springframework.stereotype.Service;

@Service
public class PeriodicPaymentInitiationService extends AbstractPaymentInitiationService<PeriodicPayment, SpiPeriodicPaymentInitiationResponse> {
    private final SpiToXs2aPaymentMapper spiToXs2aPaymentMapper;
    private final Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper;
    private final PeriodicPaymentSpi periodicPaymentSpi;

    public PeriodicPaymentInitiationService(SpiContextDataProvider spiContextDataProvider,
                                            SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                            SpiErrorMapper spiErrorMapper, SpiToXs2aPaymentMapper spiToXs2aPaymentMapper,
                                            Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper, PeriodicPaymentSpi periodicPaymentSpi) {
        super(spiContextDataProvider, aspspConsentDataProviderFactory, spiErrorMapper);
        this.spiToXs2aPaymentMapper = spiToXs2aPaymentMapper;
        this.xs2aToSpiPeriodicPaymentMapper = xs2aToSpiPeriodicPaymentMapper;
        this.periodicPaymentSpi = periodicPaymentSpi;
    }

    @Override
    protected SpiResponse<SpiPeriodicPaymentInitiationResponse> initiateSpiPayment(SpiContextData spiContextData, PeriodicPayment payment, String paymentProduct,
                                                                                   InitialSpiAspspConsentDataProvider aspspConsentDataProvider) {
        return periodicPaymentSpi.initiatePayment(spiContextData,
                                                  xs2aToSpiPeriodicPaymentMapper.mapToSpiPeriodicPayment(payment, paymentProduct),
                                                  aspspConsentDataProvider);
    }

    @Override
    protected PeriodicPaymentInitiationResponse mapToXs2aResponse(SpiPeriodicPaymentInitiationResponse spiResponse, InitialSpiAspspConsentDataProvider provider, PaymentType paymentType) {
        return spiToXs2aPaymentMapper.mapToPaymentInitiateResponse(spiResponse, provider);
    }
}
