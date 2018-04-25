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
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public Optional<SpiSinglePayments> addPayment(@NotNull SpiSinglePayments payment) {
        return Optional.ofNullable(paymentRepository.save(payment));
    }

    public double calculateAmountToBeCharged(String accountId) {
        return paymentRepository.findAll().stream()
                   .filter(a -> a.getDebtorAccount().getAccountId().equals(accountId))
                   .mapToDouble(a -> Optional.of(Double.parseDouble(a.getInstructedAmount().getContent())).orElse(0.0))
                   .sum();
    }

    public boolean isPaymentExist(String paymentId) {
        return paymentRepository.exists(paymentId);
    }
}
