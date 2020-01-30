/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.domain.CardTransaction;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardAccountReport;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiCardTransaction;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SpiCardTransactionListToXs2aAccountReportMapper {
    private static final Predicate<SpiCardTransaction> BOOKED_PREDICATE = SpiCardTransaction::isBookedTransaction;
    private static final Predicate<SpiCardTransaction> PENDING_PREDICATE = SpiCardTransaction::isPendingTransaction;

    private final SpiToXs2aCardTransactionMapper toXs2aCardTransactionMapper;

    public Optional<Xs2aCardAccountReport> mapToXs2aCardAccountReport(BookingStatus bookingStatus, List<SpiCardTransaction> spiCardTransactions, byte[] rawTransactionsResponse) {
        if (ArrayUtils.isNotEmpty(rawTransactionsResponse)) {
            return Optional.of(new Xs2aCardAccountReport(null, null, null, rawTransactionsResponse));
        }
        if (CollectionUtils.isEmpty(spiCardTransactions)) {
            return Optional.empty();
        }

        if (bookingStatus == BookingStatus.INFORMATION) {
            return Optional.of(new Xs2aCardAccountReport(null, null, toXs2aCardTransactionMapper.mapToXs2aCardTransactionList(spiCardTransactions), null));
        }

        List<CardTransaction> booked = Collections.emptyList();
        List<CardTransaction> pending = Collections.emptyList();


        if (bookingStatus != BookingStatus.PENDING) {
            booked = filterTransaction(spiCardTransactions, BOOKED_PREDICATE);
        }

        if (bookingStatus != BookingStatus.BOOKED) {
            pending = filterTransaction(spiCardTransactions, PENDING_PREDICATE);
        }

        return Optional.of(new Xs2aCardAccountReport(booked, pending, null, null));
    }

    @NotNull
    private List<CardTransaction> filterTransaction(List<SpiCardTransaction> spiTransactions, Predicate<SpiCardTransaction> predicate) {
        return spiTransactions
                   .stream()
                   .filter(predicate)
                   .map(toXs2aCardTransactionMapper::mapToXs2aCardTransaction)
                   .collect(Collectors.toList());
    }
}
