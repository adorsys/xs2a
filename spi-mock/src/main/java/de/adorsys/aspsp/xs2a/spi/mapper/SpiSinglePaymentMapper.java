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

import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountReference;
import de.adorsys.psd2.aspsp.mock.api.payment.AspspSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiSinglePaymentInitiationResponse;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SpiSinglePaymentMapper {
    private final SpiPaymentMapper spiPaymentMapper;

    public AspspSinglePayment mapToAspspSinglePayment(@NotNull SpiSinglePayment payment, SpiTransactionStatus transactionStatus) {
        AspspSinglePayment single = new AspspSinglePayment();
        single.setPaymentId(payment.getPaymentId());
        single.setEndToEndIdentification(payment.getEndToEndIdentification());
        single.setDebtorAccount(spiPaymentMapper.mapToAspspAccountReference(payment.getDebtorAccount()));
        single.setInstructedAmount(spiPaymentMapper.mapToAspspAmount(payment.getInstructedAmount()));
        single.setCreditorAccount(spiPaymentMapper.mapToAspspAccountReference(payment.getCreditorAccount()));
        single.setCreditorAgent(payment.getCreditorAgent());
        single.setCreditorName(payment.getCreditorName());
        single.setCreditorAddress(spiPaymentMapper.mapToAspspAddress(payment.getCreditorAddress()));
        single.setRemittanceInformationUnstructured(payment.getRemittanceInformationUnstructured());
        single.setPaymentStatus(spiPaymentMapper.mapToAspspTransactionStatus(transactionStatus));
        single.setRequestedExecutionTime(Optional.ofNullable(payment.getRequestedExecutionTime()).map(OffsetDateTime::toLocalDateTime).orElse(null));
        single.setRequestedExecutionDate(payment.getRequestedExecutionDate());
        return single;
    }

    public SpiSinglePayment mapToSpiSinglePayment(@NotNull AspspSinglePayment payment, String paymentProduct) {
        SpiSinglePayment single = new SpiSinglePayment(paymentProduct);
        single.setPaymentId(payment.getPaymentId());
        single.setEndToEndIdentification(payment.getEndToEndIdentification());
        single.setDebtorAccount(spiPaymentMapper.mapToSpiAccountReference(payment.getDebtorAccount()));
        single.setInstructedAmount(spiPaymentMapper.mapToSpiAmount(payment.getInstructedAmount()));
        single.setCreditorAccount(spiPaymentMapper.mapToSpiAccountReference(payment.getCreditorAccount()));
        single.setCreditorAgent(payment.getCreditorAgent());
        single.setCreditorName(payment.getCreditorName());
        single.setCreditorAddress(spiPaymentMapper.mapToSpiAddress(payment.getCreditorAddress()));
        single.setRemittanceInformationUnstructured(payment.getRemittanceInformationUnstructured());
        single.setPaymentStatus(spiPaymentMapper.mapToSpiTransactionStatus(payment.getPaymentStatus()));
        single.setRequestedExecutionTime(Optional.ofNullable(payment.getRequestedExecutionTime()).map(t -> t.atOffset(ZoneOffset.UTC)).orElse(null));
        single.setRequestedExecutionDate(payment.getRequestedExecutionDate());
        return single;
    }

    public SpiSinglePaymentInitiationResponse mapToSpiSinglePaymentResponse(@NotNull AspspSinglePayment payment) {
        SpiSinglePaymentInitiationResponse spi = new SpiSinglePaymentInitiationResponse();
        spi.setAspspAccountId(Optional.ofNullable(payment.getDebtorAccount())
                                  .map(AspspAccountReference::getAccountId)
                                  .orElse(null));
        spi.setPaymentId(payment.getPaymentId());
        spi.setMultilevelScaRequired(CollectionUtils.size(payment.getPsuDataList()) > 1);
        if (payment.getPaymentId() == null) {
            spi.setTransactionStatus(SpiTransactionStatus.RJCT);
        } else {
            spi.setTransactionStatus(SpiTransactionStatus.RCVD);
        }
        return spi;
    }
}
