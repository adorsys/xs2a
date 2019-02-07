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

    public AspspPaymentInfo mapToAspspPayment(@NotNull SpiPaymentInfo payment, TransactionStatus transactionStatus) {
        AspspPaymentInfo info = new AspspPaymentInfo();
        info.setPaymentId(payment.getPaymentId());
        info.setPaymentStatus(spiPaymentMapper.mapToAspspTransactionStatus(transactionStatus));
        info.setPaymentProduct(payment.getPaymentProduct());
        info.setPisPaymentType(payment.getPaymentType().name());
        info.setPaymentData(payment.getPaymentData());
        return info;
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
        spi.setMultilevelScaRequired(false);
        spi.setAspspAccountId(payment.getAspspAccountId());
        if (payment.getPaymentId() == null) {
            spi.setTransactionStatus(TransactionStatus.RJCT);
        } else {
            spi.setTransactionStatus(TransactionStatus.RCVD);
        }
        return spi;
    }
}
