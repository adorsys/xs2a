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

package de.adorsys.psd2.xs2a.service.payment.status;

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.payment.SpiPaymentFactory;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiGetPaymentStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.BulkPaymentSpi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("status-bulk-payments")
public class ReadBulkPaymentStatusService extends AbstractReadPaymentStatusService {

    private BulkPaymentSpi bulkPaymentSpi;

    @Autowired
    public ReadBulkPaymentStatusService(BulkPaymentSpi bulkPaymentSpi, SpiErrorMapper spiErrorMapper,
                                        SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                        SpiPaymentFactory spiPaymentFactory) {
        super(spiPaymentFactory, spiErrorMapper, aspspConsentDataProviderFactory);
        this.bulkPaymentSpi = bulkPaymentSpi;
    }

    @Override
    public Optional<SpiBulkPayment> createSpiPayment(List<PisPayment> pisPayments, String paymentProduct) {
        return spiPaymentFactory.createSpiBulkPayment(pisPayments, paymentProduct);
    }

    @Override
    public SpiResponse<SpiGetPaymentStatusResponse> getSpiPaymentStatusById(SpiContextData spiContextData, Object spiPayment,
                                                                            SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return bulkPaymentSpi.getPaymentStatusById(spiContextData, (SpiBulkPayment) spiPayment, aspspConsentDataProvider);
    }
}
