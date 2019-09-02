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

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.BulkPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiBulkPaymentMapper;
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
    BulkPaymentInitiationResponse mapToXs2aResponse(SpiBulkPaymentInitiationResponse spiResponse, InitialSpiAspspConsentDataProvider provider, PaymentType paymentType) {
        return spiToXs2aPaymentMapper.mapToPaymentInitiateResponse(spiResponse, provider);
    }

    @Override
    SpiResponse<SpiBulkPaymentInitiationResponse> initiateSpiPayment(SpiContextData spiContextData, BulkPayment payment, String paymentProduct,
                                                                     InitialSpiAspspConsentDataProvider aspspConsentDataProvider) {
        return bulkPaymentSpi.initiatePayment(spiContextData,
                                              xs2aToSpiBulkPaymentMapper.mapToSpiBulkPayment(payment, paymentProduct),
                                              aspspConsentDataProvider);
    }
}
