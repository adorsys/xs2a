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

import de.adorsys.psd2.xs2a.core.profile.PaymentProduct;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInformationResponse;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aSinglePaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("payments")
@RequiredArgsConstructor
public class ReadSinglePaymentService extends ReadPaymentService<PaymentInformationResponse<SinglePayment>> {
    private final SinglePaymentSpi singlePaymentSpi;
    private final SpiToXs2aSinglePaymentMapper xs2aPeriodicPaymentMapper;
    private final SpiErrorMapper spiErrorMapper;

    @Override
    public PaymentInformationResponse<SinglePayment> getPayment(String paymentId, PaymentProduct paymentProduct, PsuIdData psuData) {
        SpiSinglePayment payment = new SpiSinglePayment(paymentProduct);

        // we need to get decrypted payment ID
        String internalPaymentId = pisConsentDataService.getInternalPaymentIdByEncryptedString(paymentId);
        payment.setPaymentId(internalPaymentId);

        SpiPsuData spiPsuData = psuDataMapper.mapToSpiPsuData(psuData);
        SpiResponse<SpiSinglePayment> spiResponse = singlePaymentSpi.getPaymentById(spiPsuData, payment, pisConsentDataService.getAspspConsentDataByPaymentId(paymentId));
        pisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            return new PaymentInformationResponse<>(spiErrorMapper.mapToErrorHolder(spiResponse));
        }

        SpiSinglePayment spiSinglePayment = spiResponse.getPayload();

        return new PaymentInformationResponse<>(xs2aPeriodicPaymentMapper.mapToXs2aSinglePayment(spiSinglePayment));
    }
}
