/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SpiCardTransactionListToXs2aAccountReportMapper {
    private static final Predicate<SpiCardTransaction> BOOKED_PREDICATE = SpiCardTransaction::isBookedTransaction;
    private static final Predicate<SpiCardTransaction> PENDING_PREDICATE = SpiCardTransaction::isPendingTransaction;

    private final SpiToXs2aCardTransactionMapper toXs2aCardTransactionMapper;

    public Xs2aCardAccountReport mapToXs2aCardAccountReport(BookingStatus bookingStatus, List<SpiCardTransaction> spiCardTransactions, byte[] rawTransactionsResponse) {
        if (ArrayUtils.isNotEmpty(rawTransactionsResponse)) {
            return new Xs2aCardAccountReport(null, null, null, rawTransactionsResponse);
        }

        List<CardTransaction> booked = null;
        List<CardTransaction> pending = null;

        switch (bookingStatus) {
            case BOOKED:
                booked = filterTransaction(spiCardTransactions, BOOKED_PREDICATE);
                break;
            case PENDING:
                pending = filterTransaction(spiCardTransactions, PENDING_PREDICATE);
                break;
            case BOTH:
                booked = filterTransaction(spiCardTransactions, BOOKED_PREDICATE);
                pending = filterTransaction(spiCardTransactions, PENDING_PREDICATE);
                break;
            default:
                throw new IllegalArgumentException("This Booking Status is not supported: " + bookingStatus);
        }

        return new Xs2aCardAccountReport(booked, pending, null, null);
    }

    @NotNull
    private List<CardTransaction> filterTransaction(List<SpiCardTransaction> spiTransactions, Predicate<SpiCardTransaction> predicate) {
        if (CollectionUtils.isEmpty(spiTransactions)) {
            return Collections.emptyList();
        }
        return spiTransactions
                   .stream()
                   .filter(predicate)
                   .map(toXs2aCardTransactionMapper::mapToXs2aCardTransaction)
                   .collect(Collectors.toList());
    }
}
