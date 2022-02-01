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

package de.adorsys.psd2.xs2a.service.payment.support.status;

import de.adorsys.psd2.xs2a.service.mapper.MediaTypeMapper;
import de.adorsys.psd2.xs2a.service.mapper.payment.SpiPaymentFactory;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aLinksMapper;
import de.adorsys.psd2.xs2a.service.payment.status.AbstractReadPaymentStatusService;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiGetPaymentStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("status-payments")
public class ReadSinglePaymentStatusService extends AbstractReadPaymentStatusService {
    private final SinglePaymentSpi singlePaymentSpi;

    @Autowired
    public ReadSinglePaymentStatusService(SinglePaymentSpi singlePaymentSpi, SpiErrorMapper spiErrorMapper,
                                          SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                          SpiPaymentFactory spiPaymentFactory,
                                          MediaTypeMapper mediaTypeMapper,
                                          SpiToXs2aLinksMapper spiToXs2aLinksMapper) {
        super(spiErrorMapper, aspspConsentDataProviderFactory, mediaTypeMapper, spiPaymentFactory, spiToXs2aLinksMapper);
        this.singlePaymentSpi = singlePaymentSpi;
    }

    @Override
    public SpiResponse<SpiGetPaymentStatusResponse> getSpiPaymentStatusById(SpiContextData spiContextData, String acceptMediaType, Object spiPayment, SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return singlePaymentSpi.getPaymentStatusById(spiContextData, acceptMediaType, (SpiSinglePayment) spiPayment, aspspConsentDataProvider);
    }
}
