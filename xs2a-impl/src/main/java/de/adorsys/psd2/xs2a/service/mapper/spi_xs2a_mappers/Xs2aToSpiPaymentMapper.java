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

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Xs2aToSpiPaymentMapper {
    private final Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;

    public SpiPayment mapToSpiPayment(PisCommonPaymentResponse commonPaymentResponse) {
        SpiPaymentInfo spiPaymentInfo = new SpiPaymentInfo(commonPaymentResponse.getPaymentProduct());
        spiPaymentInfo.setPaymentId(commonPaymentResponse.getExternalId());
        spiPaymentInfo.setPaymentType(commonPaymentResponse.getPaymentType());
        spiPaymentInfo.setStatus(commonPaymentResponse.getTransactionStatus());
        spiPaymentInfo.setPaymentData(commonPaymentResponse.getPaymentData());
        spiPaymentInfo.setPsuDataList(xs2aToSpiPsuDataMapper.mapToSpiPsuDataList(commonPaymentResponse.getPsuData()));
        spiPaymentInfo.setStatusChangeTimestamp(commonPaymentResponse.getStatusChangeTimestamp());
        spiPaymentInfo.setCreationTimestamp(commonPaymentResponse.getCreationTimestamp());
        spiPaymentInfo.setInstanceId(commonPaymentResponse.getInstanceId());
        return spiPaymentInfo;
    }
}
