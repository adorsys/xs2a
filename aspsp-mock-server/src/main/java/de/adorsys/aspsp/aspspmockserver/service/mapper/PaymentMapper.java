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
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
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
}
