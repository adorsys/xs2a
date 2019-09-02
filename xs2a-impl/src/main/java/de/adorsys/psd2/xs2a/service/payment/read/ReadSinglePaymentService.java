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

package de.adorsys.psd2.xs2a.service.payment.read;

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aSinglePaymentMapper;
import de.adorsys.psd2.xs2a.service.payment.SpiPaymentFactory;
import de.adorsys.psd2.xs2a.service.payment.Xs2aUpdatePaymentAfterSpiService;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("payments")
public class ReadSinglePaymentService extends AbstractReadPaymentService {

    private SinglePaymentSpi singlePaymentSpi;
    private SpiToXs2aSinglePaymentMapper spiToXs2aSinglePaymentMapper;

    @Autowired
    public ReadSinglePaymentService(SinglePaymentSpi singlePaymentSpi, SpiToXs2aSinglePaymentMapper spiToXs2aSinglePaymentMapper,
                                    SpiErrorMapper spiErrorMapper, SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                    RequestProviderService requestProviderService, Xs2aUpdatePaymentAfterSpiService updatePaymentStatusAfterSpiService,
                                    SpiContextDataProvider spiContextDataProvider, SpiPaymentFactory spiPaymentFactory) {
        super(spiErrorMapper, aspspConsentDataProviderFactory, requestProviderService,updatePaymentStatusAfterSpiService, spiContextDataProvider, spiPaymentFactory);
        this.singlePaymentSpi = singlePaymentSpi;
        this.spiToXs2aSinglePaymentMapper = spiToXs2aSinglePaymentMapper;
    }

    @Override
    public Optional<SpiSinglePayment> createSpiPayment(List<PisPayment> pisPayments, String paymentProduct) {
        return spiPaymentFactory.createSpiSinglePayment(pisPayments.get(0), paymentProduct);
    }

    @Override
    public SpiResponse<SpiSinglePayment> getSpiPaymentById(SpiContextData spiContextData, Object spiPayment, SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return singlePaymentSpi.getPaymentById(spiContextData, (SpiSinglePayment) spiPayment, aspspConsentDataProvider);
    }

    @Override
    public CommonPayment getXs2aPayment(SpiResponse spiResponse) {
        SpiSinglePayment spiSinglePayment = (SpiSinglePayment) spiResponse.getPayload();
        return spiToXs2aSinglePaymentMapper.mapToXs2aSinglePayment(spiSinglePayment);
    }
}
