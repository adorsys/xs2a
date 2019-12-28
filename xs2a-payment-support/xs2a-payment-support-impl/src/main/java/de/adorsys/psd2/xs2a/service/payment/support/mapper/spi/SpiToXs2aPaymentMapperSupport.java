/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.payment.support.mapper.spi;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.BulkPaymentInitiationJson;
import de.adorsys.psd2.model.PaymentInitiationJson;
import de.adorsys.psd2.model.PeriodicPaymentInitiationJson;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.payment.support.mapper.Xs2aToPsd2PaymentMapperSupport;
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
    private final Xs2aToPsd2PaymentMapperSupport xs2aToPsd2PaymentMapperSupport;
    private final Xs2aObjectMapper xs2aObjectMapper;

    public SinglePayment mapToSinglePayment(SpiSinglePayment spiSinglePayment) {
        SinglePayment singlePayment = spiToXs2aSinglePaymentMapper.mapToXs2aSinglePayment(spiSinglePayment);
        PaymentInitiationJson paymentInitiationJson = xs2aToPsd2PaymentMapperSupport.mapToPaymentInitiationJson(singlePayment);
        singlePayment.setPaymentData(convertToBytes(paymentInitiationJson));
        return singlePayment;
    }

    public PeriodicPayment mapToPeriodicPayment(SpiPeriodicPayment spiPeriodicPayment) {
        PeriodicPayment periodicPayment = spiToXs2aPeriodicPaymentMapper.mapToXs2aPeriodicPayment(spiPeriodicPayment);
        PeriodicPaymentInitiationJson paymentInitiationJson = xs2aToPsd2PaymentMapperSupport.mapToPeriodicPaymentInitiationJson(periodicPayment);
        periodicPayment.setPaymentData(convertToBytes(paymentInitiationJson));
        return periodicPayment;
    }

    public BulkPayment mapToBulkPayment(SpiBulkPayment spiBulkPayment) {
        BulkPayment bulkPayment = spiToXs2aBulkPaymentMapper.mapToXs2aBulkPayment(spiBulkPayment);
        BulkPaymentInitiationJson paymentInitiationJson = xs2aToPsd2PaymentMapperSupport.mapToBulkPaymentInitiationJson(bulkPayment);
        bulkPayment.setPaymentData(convertToBytes(paymentInitiationJson));
        return bulkPayment;
    }

    private byte[] convertToBytes(Object payment) {
        try {
            return xs2aObjectMapper.writeValueAsBytes(payment);
        } catch (JsonProcessingException e) {
            log.warn("Couldn't serialise payment to bytes");
            return new byte[0];
        }
    }
}
