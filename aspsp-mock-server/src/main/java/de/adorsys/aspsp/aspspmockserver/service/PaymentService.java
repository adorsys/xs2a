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

package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.aspspmockserver.repository.PaymentRepository;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public Optional<SpiSinglePayments> addPayment(@NotNull SpiSinglePayments payment) {
        return Optional.ofNullable(paymentRepository.save(payment));
    }

    public Optional<SpiPeriodicPayment> addPeriodicPayment(@NotNull SpiPeriodicPayment payment) {
        return Optional.ofNullable(paymentRepository.save(payment));
    }

    public boolean isPaymentExist(String paymentId) {
        return paymentRepository.exists(paymentId);
    }

    public List<SpiSinglePayments> addBulkPayments(List<SpiSinglePayments> payments) {
        return paymentRepository.save(payments);
    }

    public BigDecimal calculateAmountToBeCharged(String accountId) {
        return paymentRepository.findAll().stream()
                   .filter(paym -> getDebtorAccountIdFromPayment(paym).equals(accountId))
                   .map(this::getAmountFromPayment)
                   .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String getDebtorAccountIdFromPayment(SpiSinglePayments payment) {
        return Optional.ofNullable(payment.getDebtorAccount())
                   .map(SpiAccountReference::getIban)
                   .orElse("");
    }

    private BigDecimal getAmountFromPayment(SpiSinglePayments payment) {
        return Optional.ofNullable(payment)
                   .map(paym -> getContentFromAmount(payment.getInstructedAmount()))
                   .orElse(BigDecimal.ZERO);
    }

    private BigDecimal getContentFromAmount(SpiAmount amount) {
        return Optional.ofNullable(amount)
                   .map(SpiAmount::getContent)
                   .orElse(BigDecimal.ZERO);
    }
}
