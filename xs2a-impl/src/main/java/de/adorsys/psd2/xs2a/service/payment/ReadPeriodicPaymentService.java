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

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInformationResponse;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aPeriodicPaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PeriodicPaymentSpi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service("periodic-payments")
@RequiredArgsConstructor
public class ReadPeriodicPaymentService extends ReadPaymentService<PaymentInformationResponse<PeriodicPayment>> {
    private final PisAspspDataService pisAspspDataService;
    private final SpiContextDataProvider spiContextDataProvider;
    private final Xs2aUpdatePaymentStatusAfterSpiService updatePaymentStatusAfterSpiService;
    private final PeriodicPaymentSpi periodicPaymentSpi;
    private final SpiToXs2aPeriodicPaymentMapper spiToXs2aPeriodicPaymentMapper;
    private final SpiErrorMapper spiErrorMapper;
    private final SpiPaymentFactory spiPaymentFactory;
    private final RequestProviderService requestProviderService;

    @Override
    public PaymentInformationResponse<PeriodicPayment> getPayment(List<PisPayment> pisPayments, String paymentProduct, PsuIdData psuData, AspspConsentData aspspConsentData) {
        Optional<SpiPeriodicPayment> spiPaymentOptional = spiPaymentFactory.createSpiPeriodicPayment(pisPayments.get(0), paymentProduct);

        if (!spiPaymentOptional.isPresent()) {
            return new PaymentInformationResponse<>(
                ErrorHolder.builder(MessageErrorCode.RESOURCE_UNKNOWN_404)
                    .messages(Collections.singletonList("Payment not found"))
                    .build()
            );
        }

        SpiContextData spiContextData = spiContextDataProvider.provideWithPsuIdData(psuData);
        SpiResponse<SpiPeriodicPayment> spiResponse = periodicPaymentSpi.getPaymentById(spiContextData, spiPaymentOptional.get(), aspspConsentData);
        pisAspspDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            return new PaymentInformationResponse<>(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS));
        }

        SpiPeriodicPayment spiResponsePayment = spiResponse.getPayload();
        PeriodicPayment xs2aPeriodicPayment = spiToXs2aPeriodicPaymentMapper.mapToXs2aPeriodicPayment(spiResponsePayment);

        TransactionStatus paymentStatus = xs2aPeriodicPayment.getTransactionStatus();
        if (!updatePaymentStatusAfterSpiService.updatePaymentStatus(aspspConsentData.getConsentId(), paymentStatus)) {
            log.info("X-Request-ID: [{}], Internal payment ID: [{}], Transaction status: [{}]. Update of a payment status in the CMS has failed.",
                     requestProviderService.getRequestId(), xs2aPeriodicPayment.getPaymentId(), paymentStatus);
        }

        return new PaymentInformationResponse<>(xs2aPeriodicPayment);
    }
}
