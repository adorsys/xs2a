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

package de.adorsys.psd2.xs2a.service.payment.support.read;

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

@Service("bulk-payments")
public class ReadBulkPaymentService extends AbstractReadPaymentService {

    private BulkPaymentSpi bulkPaymentSpi;
    private SpiToXs2aPaymentMapperSupport spiToXs2aPaymentMapperSupport;

    @Autowired
    public ReadBulkPaymentService(BulkPaymentSpi bulkPaymentSpi, SpiToXs2aPaymentMapperSupport spiToXs2aPaymentMapperSupport,
                                  SpiErrorMapper spiErrorMapper, SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                  Xs2aUpdatePaymentAfterSpiService updatePaymentStatusAfterSpiService,
                                  SpiContextDataProvider spiContextDataProvider, SpiPaymentFactory spiPaymentFactory) {
        super(spiContextDataProvider, spiErrorMapper, aspspConsentDataProviderFactory, updatePaymentStatusAfterSpiService,
              spiPaymentFactory);
        this.bulkPaymentSpi = bulkPaymentSpi;
        this.spiToXs2aPaymentMapperSupport = spiToXs2aPaymentMapperSupport;
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
