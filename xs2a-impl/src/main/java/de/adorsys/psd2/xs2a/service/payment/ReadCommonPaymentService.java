/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInformationResponse;
import de.adorsys.psd2.xs2a.service.consent.PisConsentDataService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aPaymentInfoMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentInfoMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CommonPaymentSpi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReadCommonPaymentService {
    private final CommonPaymentSpi commonPaymentSpi;
    private final PisConsentDataService pisConsentDataService;
    private final SpiContextDataProvider spiContextDataProvider;
    private final SpiErrorMapper spiErrorMapper;
    private final Xs2aToSpiPaymentInfoMapper xs2aToSpiPaymentInfoMapper;
    private final SpiToXs2aPaymentInfoMapper spiToXs2aPaymentInfoMapper;

    public PaymentInformationResponse<PisPaymentInfo> getPayment(CommonPayment commonPayment, PsuIdData psuData, AspspConsentData aspspConsentData) {
        SpiPaymentInfo spiPaymentInfo = xs2aToSpiPaymentInfoMapper.mapToSpiPaymentInfo(commonPayment);

        SpiContextData spiContextData = spiContextDataProvider.provideWithPsuIdData(psuData);
        SpiResponse<SpiPaymentInfo> spiResponse = commonPaymentSpi.getPaymentById(spiContextData, spiPaymentInfo, aspspConsentData);
        pisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            return new PaymentInformationResponse<>(spiErrorMapper.mapToErrorHolder(spiResponse));
        }

        SpiPaymentInfo responsePaymentInfo = spiResponse.getPayload();

        return new PaymentInformationResponse<>(spiToXs2aPaymentInfoMapper.mapToXs2aPaymentInfo(responsePaymentInfo));
    }
}
