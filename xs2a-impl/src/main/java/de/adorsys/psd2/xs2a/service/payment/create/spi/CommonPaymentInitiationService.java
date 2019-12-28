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
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.CommonPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentInfo;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CommonPaymentSpi;
import org.springframework.stereotype.Service;

@Service
public class CommonPaymentInitiationService extends AbstractPaymentInitiationService<CommonPayment, SpiPaymentInitiationResponse> {
    private final SpiToXs2aPaymentMapper spiToXs2aPaymentMapper;
    private final Xs2aToSpiPaymentInfo xs2aToSpiPaymentInfo;
    private final CommonPaymentSpi commonPaymentSpi;

    public CommonPaymentInitiationService(SpiContextDataProvider spiContextDataProvider,
                                          SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                          SpiErrorMapper spiErrorMapper, SpiToXs2aPaymentMapper spiToXs2aPaymentMapper,
                                          Xs2aToSpiPaymentInfo xs2aToSpiPaymentInfo, CommonPaymentSpi commonPaymentSpi) {
        super(spiContextDataProvider, aspspConsentDataProviderFactory, spiErrorMapper);
        this.spiToXs2aPaymentMapper = spiToXs2aPaymentMapper;
        this.xs2aToSpiPaymentInfo = xs2aToSpiPaymentInfo;
        this.commonPaymentSpi = commonPaymentSpi;
    }

    @Override
    protected SpiResponse<SpiPaymentInitiationResponse> initiateSpiPayment(SpiContextData spiContextData, CommonPayment payment, String paymentProduct,
                                                                           InitialSpiAspspConsentDataProvider aspspConsentDataProvider) {
        return commonPaymentSpi.initiatePayment(spiContextData,
                                                xs2aToSpiPaymentInfo.mapToSpiPaymentRequest(payment, paymentProduct),
                                                aspspConsentDataProvider);
    }

    @Override
    protected CommonPaymentInitiationResponse mapToXs2aResponse(SpiPaymentInitiationResponse spiResponse, InitialSpiAspspConsentDataProvider provider, PaymentType paymentType) {
        return spiToXs2aPaymentMapper.mapToCommonPaymentInitiateResponse(spiResponse, paymentType, provider);
    }
}
