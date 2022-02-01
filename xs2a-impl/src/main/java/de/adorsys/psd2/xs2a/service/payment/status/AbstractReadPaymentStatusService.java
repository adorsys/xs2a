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
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.domain.pis.ReadPaymentStatusResponse;
import de.adorsys.psd2.xs2a.service.mapper.MediaTypeMapper;
import de.adorsys.psd2.xs2a.service.mapper.payment.SpiPaymentFactory;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aLinksMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiGetPaymentStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * This class handles traditional payments (single, bulk, periodic).
 */
@Slf4j
@AllArgsConstructor
public abstract class AbstractReadPaymentStatusService implements ReadPaymentStatusService {
    private final SpiErrorMapper spiErrorMapper;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final MediaTypeMapper mediaTypeMapper;
    private final SpiPaymentFactory spiPaymentFactory;
    private final SpiToXs2aLinksMapper spiToXs2aLinksMapper;

    @Override
    public ReadPaymentStatusResponse readPaymentStatus(CommonPaymentData commonPaymentData, SpiContextData spiContextData, @NotNull String encryptedPaymentId, String acceptMediaType) {
        if (ArrayUtils.isEmpty(commonPaymentData.getPaymentData())) {
            return new ReadPaymentStatusResponse(
                ErrorHolder.builder(ErrorType.PIS_400)
                    .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_PAYMENT_NOT_FOUND))
                    .build());
        }

        Optional<? extends SpiPayment> spiPaymentOptional = spiPaymentFactory.getSpiPayment(commonPaymentData);

        if (spiPaymentOptional.isEmpty()) {
            return new ReadPaymentStatusResponse(
                ErrorHolder.builder(ErrorType.PIS_404)
                    .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_PAYMENT))
                    .build()
            );
        }

        SpiAspspConsentDataProvider aspspConsentDataProvider =
            aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(encryptedPaymentId);

        SpiResponse<SpiGetPaymentStatusResponse> spiResponse = getSpiPaymentStatusById(spiContextData, acceptMediaType, spiPaymentOptional.get(), aspspConsentDataProvider);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);
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

    protected abstract SpiResponse<SpiGetPaymentStatusResponse> getSpiPaymentStatusById(SpiContextData spiContextData, String acceptMediaType, Object spiPayment, SpiAspspConsentDataProvider aspspConsentDataProvider);
}
