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
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInformationResponse;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aBulkPaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.BulkPaymentSpi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service("bulk-payments")
@RequiredArgsConstructor
public class ReadBulkPaymentService extends ReadPaymentService<PaymentInformationResponse<BulkPayment>> {
    private final BulkPaymentSpi bulkPaymentSpi;
    private final SpiToXs2aBulkPaymentMapper spiToXs2aBulkPaymentMapper;
    private final SpiErrorMapper spiErrorMapper;
    private final SpiPaymentFactory spiPaymentFactory;

    @Override
    public PaymentInformationResponse<BulkPayment> getPayment(PisPayment pisPayment, String paymentProduct, PsuIdData psuData, AspspConsentData aspspConsentData) {
        Optional<SpiBulkPayment> spiPaymentOptional = spiPaymentFactory.createSpiBulkPayment(pisPayment, paymentProduct);

        if (!spiPaymentOptional.isPresent()) {
            return new PaymentInformationResponse<>(
                ErrorHolder.builder(MessageErrorCode.FORMAT_ERROR)
                    .messages(Collections.singletonList("Payment not found"))
                    .build()
            );
        }

        SpiPsuData spiPsuData = psuDataMapper.mapToSpiPsuData(psuData);
        SpiResponse<SpiBulkPayment> spiResponse = bulkPaymentSpi.getPaymentById(spiPsuData, spiPaymentOptional.get(), aspspConsentData);
        pisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            return new PaymentInformationResponse<>(spiErrorMapper.mapToErrorHolder(spiResponse));
        }

        SpiBulkPayment spiResponsePayment = spiResponse.getPayload();

        return new PaymentInformationResponse<>(spiToXs2aBulkPaymentMapper.mapToXs2aBulkPayment(spiResponsePayment));
    }
}
