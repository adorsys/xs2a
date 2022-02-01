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

package de.adorsys.psd2.xs2a.service.payment.status;

import de.adorsys.psd2.consent.api.pis.CommonPaymentData;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.ReadPaymentStatusResponse;
import de.adorsys.psd2.xs2a.service.mapper.MediaTypeMapper;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.CmsToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aLinksMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentInfoMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiGetPaymentStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CommonPaymentSpi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

/**
 * This class handles common payments (with byte array in the body).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReadCommonPaymentStatusService implements ReadPaymentStatusService {
    private final CommonPaymentSpi commonPaymentSpi;
    private final SpiErrorMapper spiErrorMapper;
    private final Xs2aToSpiPaymentInfoMapper xs2aToSpiPaymentInfoMapper;
    private final CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final MediaTypeMapper mediaTypeMapper;
    private final SpiToXs2aLinksMapper spiToXs2aLinksMapper;

    @Override
    public ReadPaymentStatusResponse readPaymentStatus(CommonPaymentData commonPaymentData, SpiContextData spiContextData, @NotNull String encryptedPaymentId, String acceptMediaType) {
        CommonPayment commonPayment = cmsToXs2aPaymentMapper.mapToXs2aCommonPayment(commonPaymentData);
        SpiPaymentInfo request = xs2aToSpiPaymentInfoMapper.mapToSpiPaymentInfo(commonPayment);

        SpiAspspConsentDataProvider aspspConsentDataProvider =
            aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(encryptedPaymentId);

        SpiResponse<SpiGetPaymentStatusResponse> spiResponse = commonPaymentSpi.getPaymentStatusById(spiContextData, acceptMediaType, request, aspspConsentDataProvider);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);
            log.info("Payment-ID [{}]. READ COMMON Payment STATUS failed. Can't get Payment status by ID at SPI level. Error msg: [{}]",
                     commonPayment.getPaymentId(), errorHolder);
            return new ReadPaymentStatusResponse(errorHolder);
        }

        SpiGetPaymentStatusResponse payload = spiResponse.getPayload();
        return new ReadPaymentStatusResponse(payload.getTransactionStatus(), payload.getFundsAvailable(),
                                             mediaTypeMapper.mapToMediaType(payload.getResponseContentType()),
                                             payload.getPaymentStatusRaw(), payload.getPsuMessage(),
                                             spiToXs2aLinksMapper.toXs2aLinks(payload.getLinks()),
                                             payload.getTppMessageInformation()
        );
    }
}
