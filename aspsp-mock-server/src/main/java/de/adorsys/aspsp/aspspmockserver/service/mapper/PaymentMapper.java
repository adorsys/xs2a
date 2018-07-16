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

import de.adorsys.aspsp.aspspmockserver.service.AccountService;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPayment;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPaymentType;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.payment.AspspPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

@Component
@AllArgsConstructor
public class PaymentMapper {
    private final AccountService accountService;

    public SpiSinglePayments mapToSpiSinglePayments(PisPayment pisPayment) {
        return Optional.ofNullable(pisPayment)
                   .map(pis -> {
                       SpiSinglePayments spiSinglePayments = new SpiSinglePayments();
                       spiSinglePayments.setEndToEndIdentification(pis.getEndToEndIdentification());
                       spiSinglePayments.setDebtorAccount(mapToSpiAccountReference(pis.getDebtorIban(), pis.getCurrency()));
                       spiSinglePayments.setUltimateDebtor(pis.getUltimateDebtor());
                       spiSinglePayments.setInstructedAmount(mapToSpiAmount(pis.getAmount(), pis.getCurrency()));
                       spiSinglePayments.setCreditorAccount(mapToSpiAccountReference(pis.getCreditorIban(), pis.getCurrency()));
                       spiSinglePayments.setCreditorAgent(pis.getCreditorAgent());
                       spiSinglePayments.setCreditorName(pis.getCreditorName());
                       spiSinglePayments.setUltimateCreditor(pis.getUltimateCreditor());
                       spiSinglePayments.setPurposeCode(pis.getPurposeCode());
                       spiSinglePayments.setRequestedExecutionDate(pis.getRequestedExecutionDate());
                       spiSinglePayments.setRequestedExecutionTime(pis.getRequestedExecutionTime());

                       return spiSinglePayments;
                   })
                   .orElse(null);
    }

    private SpiAmount mapToSpiAmount(BigDecimal amount, Currency currency) {
        return Optional.ofNullable(currency)
                   .map(curr -> new SpiAmount(currency, amount))
                   .orElse(null);
    }

    private SpiAccountReference mapToSpiAccountReference(String iban, Currency currency) {
        return accountService.getAccountsByIban(iban).stream()
                   .filter(accDet -> accDet.getCurrency() == currency)
                   .findFirst()
                   .map(acc -> new SpiAccountReference(iban, acc.getBban(), acc.getPan(), acc.getMaskedPan(), acc.getMsisdn(), currency))
                   .orElse(null);
    }

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
                       return periodic;
                   })
                   .orElse(null);
    }
}
