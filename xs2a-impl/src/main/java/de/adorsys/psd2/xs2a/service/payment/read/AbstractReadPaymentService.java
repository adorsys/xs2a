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
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInformationResponse;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.payment.SpiPaymentFactory;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.payment.Xs2aUpdatePaymentAfterSpiService;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
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
public abstract class AbstractReadPaymentService implements ReadPaymentService {

    protected SpiContextDataProvider spiContextDataProvider;

    private final SpiErrorMapper spiErrorMapper;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final Xs2aUpdatePaymentAfterSpiService updatePaymentStatusAfterSpiService;
    private final SpiPaymentFactory spiPaymentFactory;

    @Override
    public PaymentInformationResponse<CommonPayment> getPayment(CommonPaymentData commonPaymentData, PsuIdData psuData,
                                                                @NotNull String encryptedPaymentId, String acceptMediaType) {
        if (ArrayUtils.isEmpty(commonPaymentData.getPaymentData())) {
            return new PaymentInformationResponse<>(
                ErrorHolder.builder(ErrorType.PIS_400)
                    .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_PAYMENT_NOT_FOUND))
                    .build());
        }

        Optional<? extends SpiPayment> spiPaymentOptional = spiPaymentFactory.getSpiPayment(commonPaymentData);
        if (spiPaymentOptional.isEmpty()) {
            return new PaymentInformationResponse<>(
                ErrorHolder.builder(ErrorType.PIS_404)
                    .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_PAYMENT))
                    .build());
        }

        SpiContextData spiContextData = spiContextDataProvider.provideWithPsuIdData(psuData);

        SpiAspspConsentDataProvider aspspConsentDataProvider =
            aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(encryptedPaymentId);

        SpiResponse spiResponse = getSpiPaymentById(spiContextData, acceptMediaType, spiPaymentOptional.get(), aspspConsentDataProvider);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);
            log.info("Payment-ID [{}]. Read payment failed. Can't get payment by ID at SPI level. Error msg: [{}]",
                     ((SpiPayment) spiPaymentOptional.get()).getPaymentId(), errorHolder);
            return new PaymentInformationResponse<>(errorHolder);
        }

        CommonPayment xs2aPayment = getXs2aPayment(spiResponse);

        TransactionStatus paymentStatus = xs2aPayment.getTransactionStatus();

        if (!updatePaymentStatusAfterSpiService.updatePaymentStatus(encryptedPaymentId, paymentStatus)) {
            log.info("Internal payment ID: [{}], Transaction status: [{}]. Update of a payment status in the CMS has failed.",
                     xs2aPayment.getPaymentId(), paymentStatus);
        }

        return new PaymentInformationResponse<>(xs2aPayment);
    }

    protected abstract SpiResponse getSpiPaymentById(SpiContextData spiContextData, String acceptMediaType, Object spiPayment, SpiAspspConsentDataProvider aspspConsentDataProvider);

    protected abstract CommonPayment getXs2aPayment(SpiResponse spiResponse);
}
