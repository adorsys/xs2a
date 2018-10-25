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
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aBulkPaymentMapper;
import de.adorsys.psd2.xs2a.core.profile.PaymentProduct;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.BulkPaymentSpi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("bulk-payments")
@RequiredArgsConstructor
public class ReadBulkPayment extends ReadPayment<BulkPayment> {
    private final BulkPaymentSpi bulkPaymentSpi;
    private final SpiToXs2aBulkPaymentMapper xs2aBulkPaymentMapper;

    @Override
    public BulkPayment getPayment(String paymentId, PaymentProduct paymentProduct) {
        SpiBulkPayment payment = new SpiBulkPayment();
        payment.setPaymentProduct(paymentProduct);
        payment.setPaymentId(paymentId);
        SpiPsuData psuData = new SpiPsuData(null, null, null, null); // TODO get it from XS2A Interface https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/458
        SpiResponse<SpiBulkPayment> spiResponse = bulkPaymentSpi.getPaymentById(psuData, payment, pisConsentDataService.getAspspConsentDataByPaymentId(paymentId));
        pisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());
        SpiBulkPayment spiResponsePayment = spiResponse.getPayload();

        return xs2aBulkPaymentMapper.mapToXs2aBulkPayment(spiResponsePayment);
    }
}
