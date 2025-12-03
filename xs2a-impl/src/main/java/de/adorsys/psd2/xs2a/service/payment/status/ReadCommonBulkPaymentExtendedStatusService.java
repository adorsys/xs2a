/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service.payment.status;

import de.adorsys.psd2.consent.api.pis.CommonPaymentData;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.domain.pis.ReadBulkPaymentExtendedStatusResponse;
import de.adorsys.psd2.xs2a.service.mapper.MediaTypeMapper;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.CmsToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentInfoMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiBulkPaymentExtendedStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.BulkPaymentSpi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

/**
 * This class handles bulk payment extended status reading.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReadCommonBulkPaymentExtendedStatusService implements ReadBulkPaymentExtendedStatusService {
    private final BulkPaymentSpi bulkPaymentSpi;
    private final SpiErrorMapper spiErrorMapper;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper;
    private final Xs2aToSpiPaymentInfoMapper xs2aToSpiPaymentInfoMapper;
    private final MediaTypeMapper mediaTypeMapper;

    @Override
    public ReadBulkPaymentExtendedStatusResponse readBulkPaymentExtendedStatus(CommonPaymentData commonPaymentData,
            SpiContextData spiContextData, @NotNull String encryptedPaymentId, String acceptMediaType) {

        // Convert CommonPaymentData to SpiBulkPayment
        SpiBulkPayment spiBulkPayment = new SpiBulkPayment();
        // Note: We'll need proper mapping here - for now using basic mapping

        SpiAspspConsentDataProvider aspspConsentDataProvider = aspspConsentDataProviderFactory
                .getSpiAspspDataProviderFor(encryptedPaymentId);

        SpiResponse<SpiBulkPaymentExtendedStatusResponse> spiResponse = bulkPaymentSpi
                .getBulkPaymentExtendedStatusById(spiContextData, acceptMediaType, spiBulkPayment,
                        aspspConsentDataProvider);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);
            log.info("Payment-ID [{}]. READ BULK Payment Extended Status failed. Error msg: [{}]",
                    encryptedPaymentId, errorHolder);
            return new ReadBulkPaymentExtendedStatusResponse(errorHolder);
        }

        SpiBulkPaymentExtendedStatusResponse spiPaymentExtendedStatus = spiResponse.getPayload();

        // For now, return a basic mapping - this should be enhanced with proper mapper
        return new ReadBulkPaymentExtendedStatusResponse(
                spiPaymentExtendedStatus.getGroupStatus(),
                spiPaymentExtendedStatus.getReasonCode(),
                spiPaymentExtendedStatus.getReasonProprietary(),
                spiPaymentExtendedStatus.getFundsAvailable(),
                spiPaymentExtendedStatus.getOriginalTransactionInformationAndStatus(),
                spiPaymentExtendedStatus.getTransactionFees(),
                spiPaymentExtendedStatus.getCurrencyConversionFees(),
                spiPaymentExtendedStatus.getEstimatedTotalAmount(),
                spiPaymentExtendedStatus.getEstimatedInterbankSettlementAmount(),
                spiPaymentExtendedStatus.getPsuMessage(),
                spiPaymentExtendedStatus.getOwnerNames(),
                mediaTypeMapper.mapToMediaType(acceptMediaType),
                spiPaymentExtendedStatus.getPaymentStatusRaw(),
                null, // links - to be properly mapped
                null // tppMessageInformation - to be properly mapped
        );
    }
}