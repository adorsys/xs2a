/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.payment.support.read;

import de.adorsys.psd2.consent.api.pis.CommonPaymentData;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.payment.SpiPaymentFactory;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.payment.Xs2aUpdatePaymentAfterSpiService;
import de.adorsys.psd2.xs2a.service.payment.read.AbstractReadPaymentService;
import de.adorsys.psd2.xs2a.service.payment.support.mapper.spi.SpiToXs2aPaymentMapperSupport;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.BulkPaymentSpi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("bulk-payments")
public class ReadBulkPaymentService extends AbstractReadPaymentService {

    private BulkPaymentSpi bulkPaymentSpi;
    private SpiToXs2aPaymentMapperSupport spiToXs2aPaymentMapperSupport;
    private SpiPaymentFactory spiPaymentFactory;

    @Autowired
    public ReadBulkPaymentService(BulkPaymentSpi bulkPaymentSpi, SpiToXs2aPaymentMapperSupport spiToXs2aPaymentMapperSupport,
                                  SpiErrorMapper spiErrorMapper, SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                  Xs2aUpdatePaymentAfterSpiService updatePaymentStatusAfterSpiService,
                                  SpiContextDataProvider spiContextDataProvider, SpiPaymentFactory spiPaymentFactory) {
        super(spiErrorMapper, aspspConsentDataProviderFactory, updatePaymentStatusAfterSpiService, spiContextDataProvider);
        this.bulkPaymentSpi = bulkPaymentSpi;
        this.spiToXs2aPaymentMapperSupport = spiToXs2aPaymentMapperSupport;
        this.spiPaymentFactory = spiPaymentFactory;
    }

    @Override
    public Optional<SpiBulkPayment> createSpiPayment(CommonPaymentData commonPaymentData) {
        return spiPaymentFactory.createSpiBulkPayment(commonPaymentData);
    }

    @Override
    public SpiResponse<SpiBulkPayment> getSpiPaymentById(SpiContextData spiContextData, String acceptMediaType, Object spiPayment, SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return bulkPaymentSpi.getPaymentById(spiContextData, acceptMediaType, (SpiBulkPayment) spiPayment, aspspConsentDataProvider);
    }

    @Override
    public CommonPayment getXs2aPayment(SpiResponse spiResponse) {
        SpiBulkPayment spiBulkPayment = (SpiBulkPayment) spiResponse.getPayload();
        return spiToXs2aPaymentMapperSupport.mapToBulkPayment(spiBulkPayment);
    }
}
