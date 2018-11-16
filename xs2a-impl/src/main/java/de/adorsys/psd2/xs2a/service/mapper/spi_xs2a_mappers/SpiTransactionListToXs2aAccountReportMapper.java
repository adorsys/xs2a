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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.domain.Transactions;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountReport;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SpiTransactionListToXs2aAccountReportMapper {
    private final SpiToXs2aTransactionMapper toXs2aTransactionMapper;

    public Optional<Xs2aAccountReport> mapToXs2aAccountReport(List<SpiTransaction> spiTransactions, String rawTransactionsResponse) {
        return Optional.ofNullable(rawTransactionsResponse)
            .map(s -> Optional.of(new Xs2aAccountReport(null, null, rawTransactionsResponse)))
            .orElseGet(() -> {
                if (spiTransactions.isEmpty()) {
                    return Optional.empty();
                }

                List<Transactions> booked = spiTransactions
                    .stream()
                    .filter(transaction -> transaction.getBookingDate() != null)
                    .map(toXs2aTransactionMapper::mapToXs2aTransaction)
                    .collect(Collectors.toList());

                List<Transactions> pending = spiTransactions
                    .stream()
                    .filter(transaction -> transaction.getBookingDate() == null)
                    .map(toXs2aTransactionMapper::mapToXs2aTransaction)
                    .collect(Collectors.toList());

                return Optional.of(new Xs2aAccountReport(booked, pending, rawTransactionsResponse));
            });
    }
}
