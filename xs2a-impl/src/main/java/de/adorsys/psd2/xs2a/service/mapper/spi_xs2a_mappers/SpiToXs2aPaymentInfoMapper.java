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

import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import org.springframework.stereotype.Component;

@Component
public class SpiToXs2aPaymentInfoMapper {

    public PisPaymentInfo mapToXs2aPaymentInfo(SpiPaymentInfo paymentInfo) {
        PisPaymentInfo info = new PisPaymentInfo();
        info.setPaymentId(paymentInfo.getPaymentId());
        info.setPaymentProduct(paymentInfo.getPaymentProduct());
        info.setPaymentType(paymentInfo.getPaymentType());
        info.setTransactionStatus(paymentInfo.getStatus());
        info.setPaymentData(paymentInfo.getPaymentData());
        info.setStatusChangeTimestamp(paymentInfo.getStatusChangeTimestamp());
        return info;
    }
}
