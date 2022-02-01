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

import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class Xs2aToSpiPaymentInfo {
    private final Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;

    public SpiPaymentInfo mapToSpiPaymentRequest(CommonPayment payment, String paymentProduct) {
        SpiPaymentInfo request = new SpiPaymentInfo(paymentProduct);
        request.setPaymentId(payment.getPaymentId());
        request.setPaymentType(payment.getPaymentType());
        request.setPaymentStatus(payment.getTransactionStatus());
        request.setPsuDataList(xs2aToSpiPsuDataMapper.mapToSpiPsuDataList(payment.getPsuDataList()));
        request.setPaymentData(payment.getPaymentData());
        request.setStatusChangeTimestamp(payment.getStatusChangeTimestamp());
        request.setCreationTimestamp(payment.getCreationTimestamp());
        request.setContentType(payment.getContentType());
        request.setInstanceId(payment.getInstanceId());
        return request;
    }
}
