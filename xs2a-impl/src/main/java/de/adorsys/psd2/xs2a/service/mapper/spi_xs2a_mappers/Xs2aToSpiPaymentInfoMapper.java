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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.consent.api.pis.CommonPaymentData;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Xs2aToSpiPaymentInfoMapper {
    private final Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;

    public SpiPaymentInfo mapToSpiPaymentInfo(CommonPayment commonPayment) {
        SpiPaymentInfo info = new SpiPaymentInfo(commonPayment.getPaymentProduct());
        info.setPaymentId(commonPayment.getPaymentId());
        info.setPaymentType(commonPayment.getPaymentType());
        info.setStatus(commonPayment.getTransactionStatus());
        info.setPaymentData(commonPayment.getPaymentData());
        info.setPsuDataList(xs2aToSpiPsuDataMapper.mapToSpiPsuDataList(commonPayment.getPsuDataList()));
        info.setStatusChangeTimestamp(commonPayment.getStatusChangeTimestamp());
        info.setCreationTimestamp(commonPayment.getCreationTimestamp());
        info.setContentType(commonPayment.getContentType());
        info.setInstanceId(commonPayment.getInstanceId());
        return info;
    }

    public SpiPaymentInfo mapToSpiPaymentInfo(CommonPaymentData commonPaymentData) {
        SpiPaymentInfo info = new SpiPaymentInfo(commonPaymentData.getPaymentProduct());
        info.setPaymentId(commonPaymentData.getExternalId());
        info.setPaymentType(commonPaymentData.getPaymentType());
        info.setStatus(commonPaymentData.getTransactionStatus());
        info.setPaymentData(commonPaymentData.getPaymentData());
        info.setPsuDataList(xs2aToSpiPsuDataMapper.mapToSpiPsuDataList(commonPaymentData.getPsuData()));
        info.setStatusChangeTimestamp(commonPaymentData.getStatusChangeTimestamp());
        info.setCreationTimestamp(commonPaymentData.getCreationTimestamp());
        info.setContentType(commonPaymentData.getContentType());
        info.setInstanceId(commonPaymentData.getInstanceId());
        return info;
    }
}
