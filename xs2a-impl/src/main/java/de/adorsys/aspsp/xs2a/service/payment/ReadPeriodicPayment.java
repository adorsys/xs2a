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
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import org.springframework.stereotype.Service;

import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.PERIODIC;

@Service("periodic-payments")
public class ReadPeriodicPayment extends ReadPayment<PeriodicPayment> {
    @Override
    public PeriodicPayment getPayment(String paymentId, String paymentProduct) {
        SpiResponse<SpiPeriodicPayment> spiResponse = paymentSpi.getPeriodicPaymentById(paymentMapper.mapToSpiPaymentType(PERIODIC), paymentProduct, paymentId, pisConsentDataService.getAspspConsentDataByPaymentId(paymentId));
        pisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());
        SpiPeriodicPayment periodicPayment = spiResponse.getPayload();
        return paymentMapper.mapToPeriodicPayment(periodicPayment);
    }
}
