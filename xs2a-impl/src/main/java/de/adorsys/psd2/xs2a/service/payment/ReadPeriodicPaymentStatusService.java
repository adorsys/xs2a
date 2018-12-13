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
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.PeriodicPaymentSpi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("status-periodic-payments")
@RequiredArgsConstructor
public class ReadPeriodicPaymentStatusService implements ReadPaymentStatusService {
    private final SpiPaymentFactory spiPaymentFactory;
    private final PeriodicPaymentSpi periodicPaymentSpi;

    @Override
    public SpiResponse<SpiTransactionStatus> readPaymentStatus(PisPayment pisPayment, String paymentProduct, SpiContextData spiContextData, AspspConsentData aspspConsentData) {
        Optional<SpiPeriodicPayment> spiPeriodicPaymentOptional = spiPaymentFactory.createSpiPeriodicPayment(pisPayment, paymentProduct);

        return spiPeriodicPaymentOptional
                   .map(spiPeriodicPayment -> periodicPaymentSpi.getPaymentStatusById(spiContextData, spiPeriodicPayment, aspspConsentData))
                   .orElseGet(() -> SpiResponse.<SpiTransactionStatus>builder()
                                        .message("Payment not found")
                                        .fail(SpiResponseStatus.LOGICAL_FAILURE));
    }
}
