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
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.BulkPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiBulkPaymentMapper;
import de.adorsys.psd2.xs2a.service.payment.create.spi.AbstractPaymentInitiationService;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiBulkPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.BulkPaymentSpi;
import org.springframework.stereotype.Service;

@Service
public class BulkPaymentInitiationService extends AbstractPaymentInitiationService<BulkPayment, SpiBulkPaymentInitiationResponse> {
    private final SpiToXs2aPaymentMapper spiToXs2aPaymentMapper;
    private final Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper;
    private final BulkPaymentSpi bulkPaymentSpi;

    public BulkPaymentInitiationService(SpiContextDataProvider spiContextDataProvider,
                                        SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                        SpiErrorMapper spiErrorMapper, SpiToXs2aPaymentMapper spiToXs2aPaymentMapper,
                                        Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper, BulkPaymentSpi bulkPaymentSpi) {
        super(spiContextDataProvider, aspspConsentDataProviderFactory, spiErrorMapper);
        this.spiToXs2aPaymentMapper = spiToXs2aPaymentMapper;
        this.xs2aToSpiBulkPaymentMapper = xs2aToSpiBulkPaymentMapper;
        this.bulkPaymentSpi = bulkPaymentSpi;
    }

    @Override
    protected BulkPaymentInitiationResponse mapToXs2aResponse(SpiBulkPaymentInitiationResponse spiResponse, InitialSpiAspspConsentDataProvider provider, PaymentType paymentType) {
        return spiToXs2aPaymentMapper.mapToPaymentInitiateResponse(spiResponse, provider);
    }

    @Override
    protected SpiResponse<SpiBulkPaymentInitiationResponse> initiateSpiPayment(SpiContextData spiContextData, BulkPayment payment, String paymentProduct,
                                                                               InitialSpiAspspConsentDataProvider aspspConsentDataProvider) {
        return bulkPaymentSpi.initiatePayment(spiContextData,
                                              xs2aToSpiBulkPaymentMapper.mapToSpiBulkPayment(payment, paymentProduct),
                                              aspspConsentDataProvider);
    }
}
