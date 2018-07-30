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

package de.adorsys.aspsp.aspspmockserver.service.mapper;

import de.adorsys.aspsp.xs2a.consent.api.pis.PisPaymentType;
import de.adorsys.aspsp.xs2a.spi.domain.payment.AspspPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PaymentMapper {
    public AspspPayment mapToAspspPayment(SpiSinglePayments singlePayments, PisPaymentType paymentType) {
        return Optional.ofNullable(singlePayments)
                   .map(s -> buildAspspPayment(s, paymentType))
                   .orElse(null);
    }

    public AspspPayment mapToAspspPayment(SpiPeriodicPayment periodicPayment, PisPaymentType paymentType) {
        return Optional.ofNullable(periodicPayment)
                   .map(s -> {
                       AspspPayment aspsp = buildAspspPayment(periodicPayment, paymentType);
                       aspsp.setStartDate(periodicPayment.getStartDate());
                       aspsp.setEndDate(periodicPayment.getEndDate());
                       aspsp.setExecutionRule(periodicPayment.getExecutionRule());
                       aspsp.setFrequency(periodicPayment.getFrequency());
                       aspsp.setDayOfExecution(periodicPayment.getDayOfExecution());
                       return aspsp;
                   })
                   .orElse(null);
    }

    private AspspPayment buildAspspPayment(SpiSinglePayments single, PisPaymentType paymentType) {
        AspspPayment aspsp = new AspspPayment(paymentType);
        aspsp.setEndToEndIdentification(single.getEndToEndIdentification());
        aspsp.setDebtorAccount(single.getDebtorAccount());
        aspsp.setUltimateDebtor(single.getUltimateDebtor());
        aspsp.setInstructedAmount(single.getInstructedAmount());
        aspsp.setCreditorAccount(single.getCreditorAccount());
        aspsp.setCreditorAgent(single.getCreditorAgent());
        aspsp.setCreditorName(single.getCreditorName());
        aspsp.setUltimateCreditor(single.getUltimateCreditor());
        aspsp.setPurposeCode(single.getPurposeCode());
        aspsp.setRequestedExecutionDate(single.getRequestedExecutionDate());
        aspsp.setRequestedExecutionTime(single.getRequestedExecutionTime());
        aspsp.setPaymentStatus(single.getPaymentStatus());
        aspsp.setRemittanceInformationStructured(single.getRemittanceInformationStructured());
        aspsp.setRemittanceInformationUnstructured(single.getRemittanceInformationUnstructured());
        return aspsp;
    }

    public SpiSinglePayments mapToSpiSinglePayments(AspspPayment aspspPayment) {
        return Optional.ofNullable(aspspPayment)
                   .map(aspsp -> {
                       SpiSinglePayments single = new SpiSinglePayments();
                       single.setPaymentId(aspsp.getPaymentId());
                       single.setEndToEndIdentification(aspsp.getEndToEndIdentification());
                       single.setDebtorAccount(aspsp.getDebtorAccount());
                       single.setUltimateDebtor(aspsp.getUltimateDebtor());
                       single.setInstructedAmount(aspsp.getInstructedAmount());
                       single.setCreditorAccount(aspsp.getCreditorAccount());
                       single.setCreditorAgent(aspsp.getCreditorAgent());
                       single.setCreditorName(aspsp.getCreditorName());
                       single.setUltimateCreditor(aspsp.getUltimateCreditor());
                       single.setPurposeCode(aspsp.getPurposeCode());
                       single.setRequestedExecutionDate(aspsp.getRequestedExecutionDate());
                       single.setRequestedExecutionTime(aspsp.getRequestedExecutionTime());
                       single.setPaymentStatus(aspspPayment.getPaymentStatus());
                       single.setRemittanceInformationStructured(aspsp.getRemittanceInformationStructured());
                       single.setRemittanceInformationUnstructured(aspsp.getRemittanceInformationUnstructured());
                       return single;
                   })
                   .orElse(null);
    }

    public SpiPeriodicPayment mapToSpiPeriodicPayment(AspspPayment aspspPayment) {
        return Optional.ofNullable(aspspPayment)
                   .map(aspsp -> {
                       SpiPeriodicPayment periodic = new SpiPeriodicPayment();
                       periodic.setPaymentId(aspsp.getPaymentId());
                       periodic.setEndToEndIdentification(aspsp.getEndToEndIdentification());
                       periodic.setDebtorAccount(aspsp.getDebtorAccount());
                       periodic.setUltimateDebtor(aspsp.getUltimateDebtor());
                       periodic.setInstructedAmount(aspsp.getInstructedAmount());
                       periodic.setCreditorAccount(aspsp.getCreditorAccount());
                       periodic.setCreditorAgent(aspsp.getCreditorAgent());
                       periodic.setCreditorName(aspsp.getCreditorName());
                       periodic.setUltimateCreditor(aspsp.getUltimateCreditor());
                       periodic.setPurposeCode(aspsp.getPurposeCode());
                       periodic.setRequestedExecutionDate(aspsp.getRequestedExecutionDate());
                       periodic.setRequestedExecutionTime(aspsp.getRequestedExecutionTime());
                       periodic.setStartDate(aspsp.getStartDate());
                       periodic.setEndDate(aspsp.getEndDate());
                       periodic.setExecutionRule(aspsp.getExecutionRule());
                       periodic.setFrequency(aspsp.getFrequency());
                       periodic.setDayOfExecution(aspsp.getDayOfExecution());
                       periodic.setPaymentStatus(aspspPayment.getPaymentStatus());
                       periodic.setRemittanceInformationStructured(aspsp.getRemittanceInformationStructured());
                       periodic.setRemittanceInformationUnstructured(aspsp.getRemittanceInformationUnstructured());
                       return periodic;
                   })
                   .orElse(null);
    }
}
