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

package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.pis.ReadPaymentStatusResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiGetPaymentStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PeriodicPaymentSpi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service("status-periodic-payments")
@RequiredArgsConstructor
public class ReadPeriodicPaymentStatusService implements ReadPaymentStatusService {
    private final SpiPaymentFactory spiPaymentFactory;
    private final SpiErrorMapper spiErrorMapper;
    private final PeriodicPaymentSpi periodicPaymentSpi;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final RequestProviderService requestProviderService;

    @Override
    public ReadPaymentStatusResponse readPaymentStatus(List<PisPayment> pisPayments, String paymentProduct, SpiContextData spiContextData, @NotNull String encryptedPaymentId) {
        Optional<SpiPeriodicPayment> spiPeriodicPaymentOptional = spiPaymentFactory.createSpiPeriodicPayment(pisPayments.get(0), paymentProduct);

        if (!spiPeriodicPaymentOptional.isPresent()) {
            return new ReadPaymentStatusResponse(
                ErrorHolder.builder(ErrorType.PIS_404)
                    .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404, "Payment not found"))
                    .build()
            );
        }

        SpiAspspConsentDataProvider aspspConsentDataProvider =
            aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(encryptedPaymentId);

        SpiResponse<SpiGetPaymentStatusResponse> spiResponse = periodicPaymentSpi.getPaymentStatusById(spiContextData, spiPeriodicPaymentOptional.get(), aspspConsentDataProvider);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);
            log.info("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}]. READ PERIODIC Payment STATUS failed. Can't get Payment status by id at SPI-level. Error msg: [{}]",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), spiPeriodicPaymentOptional.get().getPaymentId(), errorHolder);
            return new ReadPaymentStatusResponse(errorHolder);
        }

        SpiGetPaymentStatusResponse payload = spiResponse.getPayload();
        return new ReadPaymentStatusResponse(payload.getTransactionStatus(), payload.getFundsAvailable());
    }
}
