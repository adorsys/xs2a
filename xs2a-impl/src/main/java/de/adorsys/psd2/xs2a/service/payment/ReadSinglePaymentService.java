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
import de.adorsys.psd2.xs2a.domain.pis.PaymentInformationResponse;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.mapper.consent.CmsToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aSinglePaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiSinglePaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service("payments")
@RequiredArgsConstructor
public class ReadSinglePaymentService extends ReadPaymentService<PaymentInformationResponse<SinglePayment>> {
    private final Xs2aUpdatePaymentStatusAfterSpiService updatePaymentStatusAfterSpiService;
    private final SinglePaymentSpi singlePaymentSpi;
    private final CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper;
    private final Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper;
    private final SpiToXs2aSinglePaymentMapper spiToXs2aSinglePaymentMapper;
    private final SpiErrorMapper spiErrorMapper;

    @Override
    public PaymentInformationResponse<SinglePayment> getPayment(PisPayment pisPayment, String paymentProduct, PsuIdData psuData, AspspConsentData aspspConsentData) {
        SinglePayment singlePayment = cmsToXs2aPaymentMapper.mapToSinglePayment(pisPayment);
        SpiSinglePayment spiPayment = xs2aToSpiSinglePaymentMapper.mapToSpiSinglePayment(singlePayment, paymentProduct);

        SpiPsuData spiPsuData = psuDataMapper.mapToSpiPsuData(psuData);
        SpiResponse<SpiSinglePayment> spiResponse = singlePaymentSpi.getPaymentById(spiPsuData, spiPayment, aspspConsentData);
        pisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            return new PaymentInformationResponse<>(spiErrorMapper.mapToErrorHolder(spiResponse));
        }

        SpiSinglePayment spiSinglePayment = spiResponse.getPayload();
        SinglePayment xs2aSinglePayment = spiToXs2aSinglePaymentMapper.mapToXs2aSinglePayment(spiSinglePayment);

        if (!updatePaymentStatusAfterSpiService.updatePaymentStatus(aspspConsentData.getConsentId(), xs2aSinglePayment.getTransactionStatus())) {
            return new PaymentInformationResponse<>(
                ErrorHolder.builder(MessageErrorCode.FORMAT_ERROR)
                    .messages(Collections.singletonList("Payment is finalised already, so its status cannot be changed"))
                    .build()
            );
        }

        return new PaymentInformationResponse<>(xs2aSinglePayment);
    }
}
