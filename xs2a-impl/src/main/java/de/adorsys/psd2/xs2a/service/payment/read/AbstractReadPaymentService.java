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
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.payment.Xs2aUpdatePaymentAfterSpiService;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * This class handles traditional payments (single, bulk, periodic).
 */
@Slf4j
public abstract class AbstractReadPaymentService implements ReadPaymentService {

    protected SpiContextDataProvider spiContextDataProvider;

    private SpiErrorMapper spiErrorMapper;
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private RequestProviderService requestProviderService;
    private Xs2aUpdatePaymentAfterSpiService updatePaymentStatusAfterSpiService;

    public AbstractReadPaymentService(SpiErrorMapper spiErrorMapper, SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                      RequestProviderService requestProviderService, Xs2aUpdatePaymentAfterSpiService updatePaymentStatusAfterSpiService,
                                      SpiContextDataProvider spiContextDataProvider) {
        this.spiContextDataProvider = spiContextDataProvider;
        this.spiErrorMapper = spiErrorMapper;
        this.aspspConsentDataProviderFactory = aspspConsentDataProviderFactory;
        this.requestProviderService = requestProviderService;
        this.updatePaymentStatusAfterSpiService = updatePaymentStatusAfterSpiService;
    }

    @Override
    public PaymentInformationResponse<CommonPayment> getPayment(CommonPaymentData commonPaymentData, PsuIdData psuData, @NotNull String encryptedPaymentId, String acceptMediaType) {
        if (ArrayUtils.isEmpty(commonPaymentData.getPaymentData())) {
            return new PaymentInformationResponse<>(
                ErrorHolder.builder(ErrorType.PIS_400)
                    .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_PAYMENT_NOT_FOUND))
                    .build());
        }

        Optional spiPaymentOptional = createSpiPayment(commonPaymentData);
        if (!spiPaymentOptional.isPresent()) {
            return new PaymentInformationResponse<>(
                ErrorHolder.builder(ErrorType.PIS_404)
                    .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_PAYMENT))
                    .build());
        }

        SpiContextData spiContextData = spiContextDataProvider.provideWithPsuIdData(psuData);

        SpiAspspConsentDataProvider aspspConsentDataProvider =
            aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(encryptedPaymentId);

        SpiResponse spiResponse = getSpiPaymentById(spiContextData, acceptMediaType, spiPaymentOptional.get(), aspspConsentDataProvider);
        UUID internalRequestId = requestProviderService.getInternalRequestId();
        UUID xRequestId = requestProviderService.getRequestId();

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);
            log.info("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}]. Read payment failed. Can't get payment by ID at SPI level. Error msg: [{}]",
                     internalRequestId, xRequestId, ((SpiPayment) spiPaymentOptional.get()).getPaymentId(), errorHolder);
            return new PaymentInformationResponse<>(errorHolder);
        }

        CommonPayment xs2aPayment = getXs2aPayment(spiResponse);

        TransactionStatus paymentStatus = xs2aPayment.getTransactionStatus();

        if (!updatePaymentStatusAfterSpiService.updatePaymentStatus(encryptedPaymentId, paymentStatus)) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Internal payment ID: [{}], Transaction status: [{}]. Update of a payment status in the CMS has failed.",
                     internalRequestId, xRequestId, xs2aPayment.getPaymentId(), paymentStatus);
        }

        return new PaymentInformationResponse<>(xs2aPayment);
    }

    protected abstract Optional createSpiPayment(CommonPaymentData commonPaymentData);

    protected abstract SpiResponse getSpiPaymentById(SpiContextData spiContextData, String acceptMediaType, Object spiPayment, SpiAspspConsentDataProvider aspspConsentDataProvider);

    protected abstract CommonPayment getXs2aPayment(SpiResponse spiResponse);
}
