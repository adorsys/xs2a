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
