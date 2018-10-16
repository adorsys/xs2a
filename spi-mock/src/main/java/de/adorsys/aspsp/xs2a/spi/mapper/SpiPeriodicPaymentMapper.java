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

import de.adorsys.psd2.xs2a.spi.domain.code.SpiFrequencyCode;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentProduct;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPeriodicPaymentInitiationResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class SpiPeriodicPaymentMapper {

    public de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment mapToAspspSpiPeriodicPayment(@NotNull SpiPeriodicPayment payment) {
        de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment periodic = new de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment();
        periodic.setEndToEndIdentification(payment.getEndToEndIdentification());
        periodic.setDebtorAccount(payment.getDebtorAccount());
        periodic.setInstructedAmount(payment.getInstructedAmount());
        periodic.setCreditorAccount(payment.getCreditorAccount());
        periodic.setCreditorAgent(payment.getCreditorAgent());
        periodic.setCreditorName(payment.getCreditorName());
        periodic.setCreditorAddress(payment.getCreditorAddress());
        periodic.setRemittanceInformationUnstructured(payment.getRemittanceInformationUnstructured());
        periodic.setPaymentStatus(SpiTransactionStatus.RCVD);
        periodic.setStartDate(payment.getStartDate());
        periodic.setEndDate(payment.getEndDate());
        periodic.setExecutionRule(payment.getExecutionRule());
        periodic.setFrequency(payment.getFrequency().name());
        periodic.setDayOfExecution(payment.getDayOfExecution());
        return periodic;
    }

    public SpiPeriodicPayment mapToSpiPeriodicPayment(@NotNull de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment payment, SpiPaymentProduct paymentProduct) {
        SpiPeriodicPayment periodic = new SpiPeriodicPayment(paymentProduct);
        periodic.setEndToEndIdentification(payment.getEndToEndIdentification());
        periodic.setDebtorAccount(payment.getDebtorAccount());
        periodic.setInstructedAmount(payment.getInstructedAmount());
        periodic.setCreditorAccount(payment.getCreditorAccount());
        periodic.setCreditorAgent(payment.getCreditorAgent());
        periodic.setCreditorName(payment.getCreditorName());
        periodic.setCreditorAddress(payment.getCreditorAddress());
        periodic.setRemittanceInformationUnstructured(payment.getRemittanceInformationUnstructured());
        periodic.setPaymentStatus(SpiTransactionStatus.RCVD);
        periodic.setStartDate(payment.getStartDate());
        periodic.setEndDate(payment.getEndDate());
        periodic.setExecutionRule(payment.getExecutionRule());
        periodic.setFrequency(SpiFrequencyCode.valueOf(payment.getFrequency()));
        periodic.setDayOfExecution(payment.getDayOfExecution());
        return periodic;
    }

    public SpiPeriodicPaymentInitiationResponse mapToSpiPeriodicPaymentResponse(@NotNull de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment payment) {
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
