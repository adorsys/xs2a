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

package de.adorsys.psd2.xs2a.service.payment.read;

import de.adorsys.psd2.consent.api.pis.CommonPaymentData;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInformationResponse;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aPaymentInfoMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentInfoMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CommonPaymentSpi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * This class handles common payments (with byte array in the body).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReadCommonPaymentService implements ReadPaymentService {
    private final CommonPaymentSpi commonPaymentSpi;

    private final SpiContextDataProvider spiContextDataProvider;
    private final SpiErrorMapper spiErrorMapper;
    private final Xs2aToSpiPaymentInfoMapper xs2aToSpiPaymentInfoMapper;
    private final SpiToXs2aPaymentInfoMapper spiToXs2aPaymentInfoMapper;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;

    @Override
    public PaymentInformationResponse<CommonPayment> getPayment(CommonPaymentData commonPaymentData, PsuIdData psuData, String encryptedPaymentId, String acceptMediaType) {
        SpiPaymentInfo spiPaymentInfo = xs2aToSpiPaymentInfoMapper.mapToSpiPaymentInfo(commonPaymentData);

        SpiContextData spiContextData = spiContextDataProvider.provideWithPsuIdData(psuData);
        SpiAspspConsentDataProvider aspspConsentDataProvider =
            aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(encryptedPaymentId);


        SpiResponse<SpiPaymentInfo> spiResponse = commonPaymentSpi.getPaymentById(spiContextData, acceptMediaType, spiPaymentInfo, aspspConsentDataProvider);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);
            log.info("Payment-ID [{}]. Read common payment failed. Can't get Payment by ID at SPI level. Error msg: [{}]",
                     commonPaymentData.getExternalId(), errorHolder);
            return new PaymentInformationResponse<>(errorHolder);
        }

        SpiPaymentInfo responsePaymentInfo = spiResponse.getPayload();

        return new PaymentInformationResponse<>(spiToXs2aPaymentInfoMapper.mapToXs2aPaymentInfo(responsePaymentInfo));
    }
}
