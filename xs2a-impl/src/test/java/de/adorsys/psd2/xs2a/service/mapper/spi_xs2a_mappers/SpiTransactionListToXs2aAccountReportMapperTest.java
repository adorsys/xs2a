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
import de.adorsys.psd2.xs2a.domain.Transactions;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountReport;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiTransaction;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpiTransactionListToXs2aAccountReportMapper.class, SpiToXs2aTransactionMapperImpl.class,
    SpiToXs2aAmountMapperImpl.class, SpiToXs2aBalanceMapperImpl.class, SpiToXs2aExchangeRateMapperImpl.class, SpiToXs2aAccountReferenceMapperImpl.class})
class SpiTransactionListToXs2aAccountReportMapperTest {
    private static final byte[] RAW_TRANSACTIONS = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};

    private final JsonReader jsonReader = new JsonReader();

    @Autowired
    private SpiTransactionListToXs2aAccountReportMapper spiTransactionListToXs2aAccountReportMapper;

    @Test
    void mapToXs2aAccountReport_shouldReturnEmptyOptional() {
        //When
        Optional<Xs2aAccountReport> accountReport = spiTransactionListToXs2aAccountReportMapper.mapToXs2aAccountReport(BookingStatus.BOOKED, null, null);

        //Then
        assertThat(accountReport).isEmpty();
    }

    @Test
    void mapToXs2aAccountReport_shouldReturnOptionalContainsRawTransactions() {
        //Given
        Xs2aAccountReport expected = jsonReader
            .getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-account-report-only-rawTransactions.json", Xs2aAccountReport.class);

        //When
        Optional<Xs2aAccountReport> actual = spiTransactionListToXs2aAccountReportMapper.mapToXs2aAccountReport(BookingStatus.BOOKED, null, RAW_TRANSACTIONS);

        //Then
        assertThat(actual)
            .isPresent()
            .contains(expected);
    }

    @Test
    void mapToXs2aAccountReport_shouldReturnOnlyPending() {
        //Given
        List<SpiTransaction> transactions = jsonReader
            .getListFromFile("json/service/mapper/spi_xs2a_mappers/spi-transactions.json", SpiTransaction.class);
        Xs2aAccountReport expected = jsonReader
            .getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-account-report-only-pending.json", Xs2aAccountReport.class);

        //When
        Optional<Xs2aAccountReport> actual = spiTransactionListToXs2aAccountReportMapper
            .mapToXs2aAccountReport(BookingStatus.PENDING, transactions, null);

        //Then
        assertThat(actual)
            .isNotEmpty()
            .contains(expected);
    }

    @Test
    void mapToXs2aAccountReport_shouldReturnOnlyBooked() {
        //Given
        List<SpiTransaction> transactions = jsonReader
            .getListFromFile("json/service/mapper/spi_xs2a_mappers/spi-transactions.json", SpiTransaction.class);
        Xs2aAccountReport expected = jsonReader
            .getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-account-report-only-booked.json", Xs2aAccountReport.class);

        //When
        Optional<Xs2aAccountReport> actual = spiTransactionListToXs2aAccountReportMapper
            .mapToXs2aAccountReport(BookingStatus.BOOKED, transactions, null);

        //Then
        assertThat(actual)
            .isNotEmpty()
            .contains(expected);
    }

    @Test
    void mapToXs2aAccountReport_shouldReturnBoth() {
        //Given
        List<SpiTransaction> transactions = jsonReader
            .getListFromFile("json/service/mapper/spi_xs2a_mappers/spi-transactions.json", SpiTransaction.class);
        Xs2aAccountReport expected = jsonReader
            .getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-account-report-both.json", Xs2aAccountReport.class);

        //When
        Optional<Xs2aAccountReport> actual = spiTransactionListToXs2aAccountReportMapper
            .mapToXs2aAccountReport(BookingStatus.BOTH, transactions, null);

        //Then
        assertThat(actual)
            .isNotEmpty()
            .contains(expected);
    }

    @Test
    void mapToXs2aAccountReport_bookingStatusIsInformation() {
        //Given
        List<SpiTransaction> transactions = jsonReader
            .getListFromFile("json/service/mapper/spi_xs2a_mappers/spi-transactions.json", SpiTransaction.class);
        List<Transactions> expected = jsonReader
            .getListFromFile("json/service/mapper/spi_xs2a_mappers/transactions-list-expected.json", Transactions.class);

        //When
        Optional<Xs2aAccountReport> actual = spiTransactionListToXs2aAccountReportMapper
            .mapToXs2aAccountReport(BookingStatus.INFORMATION, transactions, null);

        //Then
        assertThat(actual)
            .isPresent()
            .get()
            .extracting(Xs2aAccountReport::getInformation)
            .asList()
            .hasSize(2)
            .isEqualTo(expected);
    }
}
