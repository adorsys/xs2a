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
