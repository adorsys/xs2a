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

import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.payment.support.mapper.RawToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiBulkPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPeriodicPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiSinglePaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpiPaymentMapper {
    private final Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper;
    private final Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper;
    private final Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper;
    private final RawToXs2aPaymentMapper rawToXs2aPaymentMapper;

    public SpiSinglePayment mapToSpiSinglePayment(SpiPayment spiPayment) {
        SpiPaymentInfo spiPaymentInfo = (SpiPaymentInfo) spiPayment;

        byte[] paymentData = spiPaymentInfo.getPaymentData();
        SinglePayment xs2aSinglePayment = rawToXs2aPaymentMapper.mapToSinglePayment(paymentData);
        SpiSinglePayment spiSinglePayment = xs2aToSpiSinglePaymentMapper.mapToSpiSinglePayment(xs2aSinglePayment, spiPaymentInfo.getPaymentProduct());
        spiSinglePayment.setPaymentId(spiPaymentInfo.getPaymentId());
        spiSinglePayment.setPaymentProduct(spiPaymentInfo.getPaymentProduct());
        spiSinglePayment.setPaymentStatus(spiPaymentInfo.getPaymentStatus());
        spiSinglePayment.setPsuDataList(spiPaymentInfo.getPsuDataList());
        spiSinglePayment.setStatusChangeTimestamp(spiPaymentInfo.getStatusChangeTimestamp());
        spiSinglePayment.setCreationTimestamp(spiPaymentInfo.getCreationTimestamp());

        return spiSinglePayment;
    }

    public SpiPeriodicPayment mapToSpiPeriodicPayment(SpiPayment spiPayment) {
        SpiPaymentInfo spiPaymentInfo = (SpiPaymentInfo) spiPayment;

        byte[] paymentData = spiPaymentInfo.getPaymentData();
        PeriodicPayment xs2aPeriodicPayment = rawToXs2aPaymentMapper.mapToPeriodicPayment(paymentData);
        SpiPeriodicPayment spiPeriodicPayment = xs2aToSpiPeriodicPaymentMapper.mapToSpiPeriodicPayment(xs2aPeriodicPayment, spiPaymentInfo.getPaymentProduct());
        spiPeriodicPayment.setPaymentId(spiPaymentInfo.getPaymentId());
        spiPeriodicPayment.setPaymentProduct(spiPaymentInfo.getPaymentProduct());
        spiPeriodicPayment.setPaymentStatus(spiPaymentInfo.getPaymentStatus());
        spiPeriodicPayment.setPsuDataList(spiPaymentInfo.getPsuDataList());
        spiPeriodicPayment.setStatusChangeTimestamp(spiPaymentInfo.getStatusChangeTimestamp());
        spiPeriodicPayment.setCreationTimestamp(spiPaymentInfo.getCreationTimestamp());

        return spiPeriodicPayment;
    }

    public SpiBulkPayment mapToSpiBulkPayment(SpiPayment spiPayment) {
        SpiPaymentInfo spiPaymentInfo = (SpiPaymentInfo) spiPayment;

        byte[] paymentData = spiPaymentInfo.getPaymentData();
        BulkPayment xs2aBulkPayment = rawToXs2aPaymentMapper.mapToBulkPayment(paymentData);
        SpiBulkPayment spiBulkPayment = xs2aToSpiBulkPaymentMapper.mapToSpiBulkPayment(xs2aBulkPayment, spiPaymentInfo.getPaymentProduct());
        spiBulkPayment.setPaymentId(spiPaymentInfo.getPaymentId());
        spiBulkPayment.setPaymentProduct(spiPaymentInfo.getPaymentProduct());
        spiBulkPayment.setPaymentStatus(spiPaymentInfo.getPaymentStatus());
        spiBulkPayment.setPsuDataList(spiPaymentInfo.getPsuDataList());
        spiBulkPayment.setStatusChangeTimestamp(spiPaymentInfo.getStatusChangeTimestamp());
        spiBulkPayment.setCreationTimestamp(spiPaymentInfo.getCreationTimestamp());

        return spiBulkPayment;
    }
}
