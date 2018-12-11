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

package de.adorsys.aspsp.xs2a.spi.mapper;

import de.adorsys.psd2.aspsp.mock.api.payment.AspspPaymentInfo;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiCommonPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentInitiationResponse;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SpiPaymentInfoMapper {
    private final SpiPaymentMapper spiPaymentMapper;

    public AspspPaymentInfo mapToAspspPayment(@NotNull SpiPaymentInfo payment, SpiTransactionStatus transactionStatus) {
        return new AspspPaymentInfo(payment.getPaymentId(),
                                    spiPaymentMapper.mapToAspspTransactionStatus(transactionStatus),
                                    payment.getPaymentProduct(),
                                    payment.getPaymentType().name(),
                                    payment.getPaymentData()
        );
    }

    public SpiPaymentInfo mapToSpiPaymentInfo(@NotNull AspspPaymentInfo payment) {
        SpiPaymentInfo spiPayment = new SpiPaymentInfo(payment.getPaymentProduct());
        spiPayment.setPaymentId(payment.getPaymentId());
        spiPayment.setPaymentType(PaymentType.valueOf(payment.getPisPaymentType()));
        spiPayment.setStatus(TransactionStatus.getByValue(payment.getPaymentStatus().getName()));
        spiPayment.setPaymentData(payment.getPaymentData());
        return spiPayment;
    }

    public SpiPaymentInitiationResponse mapToSpiPaymentInitiationResponse(@NotNull AspspPaymentInfo payment) {
        SpiCommonPaymentInitiationResponse spi = new SpiCommonPaymentInitiationResponse();
        spi.setPaymentId(payment.getPaymentId());
        if (payment.getPaymentId() == null) {
            spi.setTransactionStatus(SpiTransactionStatus.RJCT);
        } else {
            spi.setTransactionStatus(SpiTransactionStatus.RCVD);
        }
        return spi;
    }
}
