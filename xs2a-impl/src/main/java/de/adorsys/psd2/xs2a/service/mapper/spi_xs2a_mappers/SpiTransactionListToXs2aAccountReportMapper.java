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
import de.adorsys.psd2.xs2a.domain.Transactions;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountReport;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiTransaction;
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
public class SpiTransactionListToXs2aAccountReportMapper {
    private static final Predicate<SpiTransaction> BOOKED_PREDICATE = SpiTransaction::isBookedTransaction;
    private static final Predicate<SpiTransaction> PENDING_PREDICATE = SpiTransaction::isPendingTransaction;
    private static final Predicate<SpiTransaction> INFORMATION_PREDICATE = SpiTransaction::isInformationTransaction;

    private final SpiToXs2aTransactionMapper toXs2aTransactionMapper;

    public Xs2aAccountReport mapToXs2aAccountReport(BookingStatus bookingStatus, List<SpiTransaction> spiTransactions, byte[] rawTransactionsResponse) {
        if (ArrayUtils.isNotEmpty(rawTransactionsResponse)) {
            return new Xs2aAccountReport(null, null, null, rawTransactionsResponse);
        }

        List<Transactions> booked = null;
        List<Transactions> pending = null;
        List<Transactions> information = null;

        switch (bookingStatus) {
            case INFORMATION:
                information = filterTransaction(spiTransactions, INFORMATION_PREDICATE);
                break;
            case BOOKED:
                booked = filterTransaction(spiTransactions, BOOKED_PREDICATE);
                break;
            case PENDING:
                pending = filterTransaction(spiTransactions, PENDING_PREDICATE);
                break;
            case BOTH:
                booked = filterTransaction(spiTransactions, BOOKED_PREDICATE);
                pending = filterTransaction(spiTransactions, PENDING_PREDICATE);
                break;
            case ALL:
                information = filterTransaction(spiTransactions, INFORMATION_PREDICATE);
                booked = filterTransaction(spiTransactions, BOOKED_PREDICATE);
                pending = filterTransaction(spiTransactions, PENDING_PREDICATE);
                break;
            default:
                throw new IllegalArgumentException("This Booking Status is not supported: " + bookingStatus);
        }
        return new Xs2aAccountReport(booked, pending, information, null);
    }

    @NotNull
    private List<Transactions> filterTransaction(List<SpiTransaction> spiTransactions, Predicate<SpiTransaction> predicate) {
        if (CollectionUtils.isEmpty(spiTransactions)) {
            return Collections.emptyList();
        }
        return spiTransactions
                   .stream()
                   .filter(predicate)
                   .map(toXs2aTransactionMapper::mapToXs2aTransaction)
                   .collect(Collectors.toList());
    }
}
