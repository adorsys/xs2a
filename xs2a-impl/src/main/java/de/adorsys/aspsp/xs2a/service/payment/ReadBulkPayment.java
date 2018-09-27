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

import de.adorsys.aspsp.xs2a.domain.pis.BulkPayment;
import de.adorsys.aspsp.xs2a.spi.domain.SpiResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;
import org.springframework.stereotype.Service;

import java.util.List;

import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.BULK;

@Service("bulk-payments")
public class ReadBulkPayment extends ReadPayment<BulkPayment> {
    @Override
    public BulkPayment getPayment(String paymentId, String paymentProduct) {
        SpiResponse<List<SpiSinglePayment>> spiResponse = paymentSpi.getBulkPaymentById(paymentMapper.mapToSpiPaymentType(BULK), paymentProduct, paymentId, pisConsentDataService.getConsentDataByPaymentId(paymentId));
        pisConsentDataService.updateConsentData(spiResponse.getAspspConsentData());
        List<SpiSinglePayment> bulkPayments = spiResponse.getPayload();
        return paymentMapper.mapToBulkPayment(bulkPayments);
    }
}
