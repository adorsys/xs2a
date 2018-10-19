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

package de.adorsys.aspsp.xs2a.service.payment;

import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aPeriodicPaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentProduct;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PeriodicPaymentSpi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("periodic-payments")
@RequiredArgsConstructor
public class ReadPeriodicPayment extends ReadPayment<PeriodicPayment> {
    private final PeriodicPaymentSpi periodicPaymentSpi;
    private final SpiToXs2aPeriodicPaymentMapper xs2aPeriodicPaymentMapper;

    @Override
    public PeriodicPayment getPayment(String paymentId, String paymentProduct) {
        SpiPeriodicPayment payment = new SpiPeriodicPayment(SpiPaymentProduct.getByValue(paymentProduct));
        payment.setPaymentId(paymentId);
        payment.setPaymentProduct(SpiPaymentProduct.getByValue(paymentProduct));
        SpiPsuData psuData = new SpiPsuData(null, null, null, null); // TODO get it from XS2A Interface https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332
        SpiResponse<SpiPeriodicPayment> spiResponse = periodicPaymentSpi.getPaymentById(psuData, payment, pisConsentDataService.getAspspConsentDataByPaymentId(paymentId));
        pisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());
        SpiPeriodicPayment responsePayment = spiResponse.getPayload();

        return xs2aPeriodicPaymentMapper.mapToXs2aPeriodicPayment(responsePayment);
    }
}
