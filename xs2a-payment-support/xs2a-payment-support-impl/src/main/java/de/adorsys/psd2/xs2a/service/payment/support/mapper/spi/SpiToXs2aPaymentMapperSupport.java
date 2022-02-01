/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service.payment.support.mapper.spi;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.BulkPaymentInitiationJson;
import de.adorsys.psd2.model.PaymentInitiationJson;
import de.adorsys.psd2.model.PeriodicPaymentInitiationJson;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.payment.support.mapper.Xs2aToPsd2PaymentSupportMapper;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpiToXs2aPaymentMapperSupport {
    private final SpiToXs2aSinglePaymentMapper spiToXs2aSinglePaymentMapper;
    private final SpiToXs2aPeriodicPaymentMapper spiToXs2aPeriodicPaymentMapper;
    private final SpiToXs2aBulkPaymentMapper spiToXs2aBulkPaymentMapper;
    private final Xs2aToPsd2PaymentSupportMapper xs2AToPsd2PaymentSupportMapper;
    private final Xs2aObjectMapper xs2aObjectMapper;

    public SinglePayment mapToSinglePayment(SpiSinglePayment spiSinglePayment) {
        SinglePayment singlePayment = spiToXs2aSinglePaymentMapper.mapToXs2aSinglePayment(spiSinglePayment);
        PaymentInitiationJson paymentInitiationJson = xs2AToPsd2PaymentSupportMapper.mapToPaymentInitiationJson(singlePayment);

        if (singlePayment != null) {
            singlePayment.setPaymentData(convertToBytes(paymentInitiationJson));
        }

        return singlePayment;
    }

    public PeriodicPayment mapToPeriodicPayment(SpiPeriodicPayment spiPeriodicPayment) {
        PeriodicPayment periodicPayment = spiToXs2aPeriodicPaymentMapper.mapToXs2aPeriodicPayment(spiPeriodicPayment);
        PeriodicPaymentInitiationJson paymentInitiationJson = xs2AToPsd2PaymentSupportMapper.mapToPeriodicPaymentInitiationJson(periodicPayment);

        if (periodicPayment != null) {
            periodicPayment.setPaymentData(convertToBytes(paymentInitiationJson));
        }

        return periodicPayment;
    }

    public BulkPayment mapToBulkPayment(SpiBulkPayment spiBulkPayment) {
        BulkPayment bulkPayment = spiToXs2aBulkPaymentMapper.mapToXs2aBulkPayment(spiBulkPayment);
        BulkPaymentInitiationJson paymentInitiationJson = xs2AToPsd2PaymentSupportMapper.mapToBulkPaymentInitiationJson(bulkPayment);

        if (bulkPayment != null) {
            bulkPayment.setPaymentData(convertToBytes(paymentInitiationJson));
        }

        return bulkPayment;
    }

    private byte[] convertToBytes(Object payment) {
        if (payment == null) {
            return new byte[0];
        }

        try {
            return xs2aObjectMapper.writeValueAsBytes(payment);
        } catch (JsonProcessingException e) {
            log.warn("Couldn't serialise payment to bytes");
            return new byte[0];
        }
    }
}
