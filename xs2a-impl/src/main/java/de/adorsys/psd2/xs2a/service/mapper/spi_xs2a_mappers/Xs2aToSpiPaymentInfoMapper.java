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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

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
        return info;
    }
}
