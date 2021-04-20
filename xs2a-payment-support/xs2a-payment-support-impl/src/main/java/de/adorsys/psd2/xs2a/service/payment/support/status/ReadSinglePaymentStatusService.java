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
