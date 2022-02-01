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
