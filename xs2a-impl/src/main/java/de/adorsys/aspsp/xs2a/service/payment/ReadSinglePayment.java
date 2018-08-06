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

import de.adorsys.aspsp.xs2a.domain.pis.SinglePayment;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;
import org.springframework.stereotype.Service;

import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.SINGLE;

@Service("payments")
public class ReadSinglePayment extends ReadPayment<SinglePayment> {
    @Override
    public SinglePayment getPayment(String paymentProduct, String paymentId) {
        SpiSinglePayment singlePayment = paymentSpi.getSinglePaymentById(paymentMapper.mapToSpiPaymentType(SINGLE), paymentProduct, paymentId, new AspspConsentData("zzzzzzzzzzzzzz".getBytes())).getPayload(); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
        return paymentMapper.mapToSinglePayment(singlePayment);
    }
}
