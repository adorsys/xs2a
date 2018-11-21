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

import de.adorsys.psd2.aspsp.mock.api.payment.AspspPeriodicPayment;
import de.adorsys.psd2.xs2a.core.profile.PaymentProduct;
import de.adorsys.psd2.xs2a.spi.domain.code.SpiFrequencyCode;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPeriodicPaymentInitiationResponse;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Component
@AllArgsConstructor
public class SpiPeriodicPaymentMapper {
    private final SpiPaymentMapper spiPaymentMapper;

    public AspspPeriodicPayment mapToAspspPeriodicPayment(@NotNull SpiPeriodicPayment payment, SpiTransactionStatus transactionStatus) {
        AspspPeriodicPayment periodic = new AspspPeriodicPayment();
        periodic.setPaymentId(payment.getPaymentId());
        periodic.setEndToEndIdentification(payment.getEndToEndIdentification());
        periodic.setDebtorAccount(spiPaymentMapper.mapToAspspAccountReference(payment.getDebtorAccount()));
        periodic.setInstructedAmount(spiPaymentMapper.mapToAspspAmount(payment.getInstructedAmount()));
        periodic.setCreditorAccount(spiPaymentMapper.mapToAspspAccountReference(payment.getCreditorAccount()));
        periodic.setCreditorAgent(payment.getCreditorAgent());
        periodic.setCreditorName(payment.getCreditorName());
        periodic.setCreditorAddress(spiPaymentMapper.mapToAspspAddress(payment.getCreditorAddress()));
        periodic.setRemittanceInformationUnstructured(payment.getRemittanceInformationUnstructured());
        periodic.setPaymentStatus(spiPaymentMapper.mapToAspspTransactionStatus(transactionStatus));
        periodic.setStartDate(payment.getStartDate());
        periodic.setEndDate(payment.getEndDate());
        periodic.setExecutionRule(payment.getExecutionRule());
        periodic.setFrequency(payment.getFrequency().name());
        periodic.setDayOfExecution(payment.getDayOfExecution());
        periodic.setRequestedExecutionTime(Optional.ofNullable(payment.getRequestedExecutionTime()).map(OffsetDateTime::toLocalDateTime).orElse(null));
        periodic.setRequestedExecutionDate(payment.getRequestedExecutionDate());
        return periodic;
    }

    public SpiPeriodicPayment mapToSpiPeriodicPayment(@NotNull AspspPeriodicPayment payment, PaymentProduct paymentProduct) {
        SpiPeriodicPayment periodic = new SpiPeriodicPayment(paymentProduct);
        periodic.setPaymentId(payment.getPaymentId());
        periodic.setEndToEndIdentification(payment.getEndToEndIdentification());
        periodic.setDebtorAccount(spiPaymentMapper.mapToSpiAccountReference(payment.getDebtorAccount()));
        periodic.setInstructedAmount(spiPaymentMapper.mapToSpiAmount(payment.getInstructedAmount()));
        periodic.setCreditorAccount(spiPaymentMapper.mapToSpiAccountReference(payment.getCreditorAccount()));
        periodic.setCreditorAgent(payment.getCreditorAgent());
        periodic.setCreditorName(payment.getCreditorName());
        periodic.setCreditorAddress(spiPaymentMapper.mapToSpiAddress(payment.getCreditorAddress()));
        periodic.setRemittanceInformationUnstructured(payment.getRemittanceInformationUnstructured());
        periodic.setPaymentStatus(spiPaymentMapper.mapToSpiTransactionStatus(payment.getPaymentStatus()));
        periodic.setStartDate(payment.getStartDate());
        periodic.setEndDate(payment.getEndDate());
        periodic.setExecutionRule(payment.getExecutionRule());
        periodic.setFrequency(SpiFrequencyCode.valueOf(payment.getFrequency()));
        periodic.setDayOfExecution(payment.getDayOfExecution());
        periodic.setRequestedExecutionTime(Optional.ofNullable(payment.getRequestedExecutionTime()).map(t -> t.atOffset(ZoneOffset.UTC)).orElse(null));
        periodic.setRequestedExecutionDate(payment.getRequestedExecutionDate());
        return periodic;
    }

    public SpiPeriodicPaymentInitiationResponse mapToSpiPeriodicPaymentResponse(@NotNull AspspPeriodicPayment payment) {
        SpiPeriodicPaymentInitiationResponse spi = new SpiPeriodicPaymentInitiationResponse();
        spi.setPaymentId(payment.getPaymentId());
        if (payment.getPaymentId() == null) {
            spi.setTransactionStatus(SpiTransactionStatus.RJCT);
        } else {
            spi.setTransactionStatus(SpiTransactionStatus.RCVD);
        }
        return spi;
    }
}
