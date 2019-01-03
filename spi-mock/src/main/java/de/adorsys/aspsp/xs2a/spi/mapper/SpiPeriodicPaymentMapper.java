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

import de.adorsys.psd2.aspsp.mock.api.payment.AspspDayOfExecution;
import de.adorsys.psd2.aspsp.mock.api.payment.AspspExecutionRule;
import de.adorsys.psd2.aspsp.mock.api.payment.AspspPeriodicPayment;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.PisExecutionRule;
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
        periodic.setExecutionRule(mapToAspspExecutionRule(payment.getExecutionRule()).orElse(null));
        periodic.setFrequency(payment.getFrequency().name());
        periodic.setDayOfExecution(mapToAspspDayOfExecution(payment.getDayOfExecution()).orElse(null));
        periodic.setRequestedExecutionTime(Optional.ofNullable(payment.getRequestedExecutionTime()).map(OffsetDateTime::toLocalDateTime).orElse(null));
        periodic.setRequestedExecutionDate(payment.getRequestedExecutionDate());
        return periodic;
    }

    public SpiPeriodicPayment mapToSpiPeriodicPayment(@NotNull AspspPeriodicPayment payment, String paymentProduct) {
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
        periodic.setExecutionRule(mapToPisExecutionRule(payment.getExecutionRule()).orElse(null));
        periodic.setFrequency(SpiFrequencyCode.valueOf(payment.getFrequency()));
        periodic.setDayOfExecution(mapToPisDayOfExecution(payment.getDayOfExecution()).orElse(null));
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

    private Optional<PisDayOfExecution> mapToPisDayOfExecution(AspspDayOfExecution dayOfExecution) {
        return Optional.ofNullable(dayOfExecution)
                   .map(AspspDayOfExecution::toString)
                   .flatMap(PisDayOfExecution::getByValue);
    }

    private Optional<PisExecutionRule> mapToPisExecutionRule(AspspExecutionRule rule) {
        return Optional.ofNullable(rule)
                   .map(AspspExecutionRule::toString)
                   .flatMap(PisExecutionRule::getByValue);
    }

    private Optional<AspspDayOfExecution> mapToAspspDayOfExecution(PisDayOfExecution dayOfExecution) {
        return Optional.ofNullable(dayOfExecution)
                   .map(PisDayOfExecution::toString)
                   .flatMap(AspspDayOfExecution::getByValue);
    }

    private Optional<AspspExecutionRule> mapToAspspExecutionRule(PisExecutionRule rule) {
        return Optional.ofNullable(rule)
                   .map(PisExecutionRule::toString)
                   .flatMap(AspspExecutionRule::getByValue);
    }
}
