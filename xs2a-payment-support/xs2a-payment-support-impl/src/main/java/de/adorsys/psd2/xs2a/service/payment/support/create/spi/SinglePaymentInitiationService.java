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
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiSinglePaymentMapper;
import de.adorsys.psd2.xs2a.service.payment.create.spi.AbstractPaymentInitiationService;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiSinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import org.springframework.stereotype.Service;

@Service
public class SinglePaymentInitiationService extends AbstractPaymentInitiationService<SinglePayment, SpiSinglePaymentInitiationResponse> {
    private final SpiToXs2aPaymentMapper spiToXs2aPaymentMapper;
    private final Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper;
    private final SinglePaymentSpi singlePaymentSpi;

    public SinglePaymentInitiationService(SpiContextDataProvider spiContextDataProvider,
                                          SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                          SpiErrorMapper spiErrorMapper, SpiToXs2aPaymentMapper spiToXs2aPaymentMapper,
                                          Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper, SinglePaymentSpi singlePaymentSpi) {
        super(spiContextDataProvider, aspspConsentDataProviderFactory, spiErrorMapper);
        this.spiToXs2aPaymentMapper = spiToXs2aPaymentMapper;
        this.xs2aToSpiSinglePaymentMapper = xs2aToSpiSinglePaymentMapper;
        this.singlePaymentSpi = singlePaymentSpi;
    }

    @Override
    protected SpiResponse<SpiSinglePaymentInitiationResponse> initiateSpiPayment(SpiContextData spiContextData, SinglePayment payment, String paymentProduct,
                                                                                 InitialSpiAspspConsentDataProvider aspspConsentDataProvider) {
        return singlePaymentSpi.initiatePayment(spiContextData,
                                                xs2aToSpiSinglePaymentMapper.mapToSpiSinglePayment(payment, paymentProduct),
                                                aspspConsentDataProvider);
    }

    @Override
    protected SinglePaymentInitiationResponse mapToXs2aResponse(SpiSinglePaymentInitiationResponse spiResponse, InitialSpiAspspConsentDataProvider provider, PaymentType paymentType) {
        return spiToXs2aPaymentMapper.mapToPaymentInitiateResponse(spiResponse, provider);
    }
}
