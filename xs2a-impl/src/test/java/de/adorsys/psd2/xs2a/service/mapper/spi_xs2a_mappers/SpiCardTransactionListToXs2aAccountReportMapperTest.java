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

import com.fasterxml.jackson.core.type.TypeReference;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardAccountReport;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiCardTransaction;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpiCardTransactionListToXs2aAccountReportMapper.class, SpiToXs2aCardTransactionMapperImpl.class,
    SpiToXs2aAmountMapperImpl.class, Xs2aToSpiAddressMapper.class, SpiToXs2aExchangeRateMapperImpl.class})
class SpiCardTransactionListToXs2aAccountReportMapperTest {
    @Autowired
    private SpiCardTransactionListToXs2aAccountReportMapper spiCardTransactionListToXs2aAccountReportMapper;

    private JsonReader jsonReader = new JsonReader();

    @Test
    void mapToXs2aCardAccountReport() {
        //Given
        BookingStatus bookingStatus = BookingStatus.BOTH;

        List<SpiCardTransaction> spiCardTransactions = jsonReader.getObjectFromFile("json/SpiCardTransactions.json", new TypeReference<>() {
        });
        Xs2aCardAccountReport expectedXs2aCardAccountReport = jsonReader.getObjectFromFile("json/Xs2aCardAccountReport.json", Xs2aCardAccountReport.class);

        //When
        Optional<Xs2aCardAccountReport> xs2aCardAccountReport = spiCardTransactionListToXs2aAccountReportMapper.mapToXs2aCardAccountReport(bookingStatus, spiCardTransactions, null);

        //Then
        assertTrue(xs2aCardAccountReport.isPresent());
        assertEquals(expectedXs2aCardAccountReport, xs2aCardAccountReport.get());
    }

    @Test
    void mapToXs2aCardAccountReport_rawTransactionsResponse() {
        //Given
        byte[] rawData = "some raw data".getBytes();
        Xs2aCardAccountReport expectedReport= new Xs2aCardAccountReport(null, null, null, rawData);
        //When
        Optional<Xs2aCardAccountReport> xs2aCardAccountReport = spiCardTransactionListToXs2aAccountReportMapper.mapToXs2aCardAccountReport(BookingStatus.BOTH, Collections.emptyList(), rawData);

        //Then
        assertTrue(xs2aCardAccountReport.isPresent());
        assertEquals(expectedReport, xs2aCardAccountReport.get());
    }

    @Test
    void mapToXs2aCardAccountReport_emptyTransactions() {
        //Given
        //When
        Optional<Xs2aCardAccountReport> xs2aCardAccountReport = spiCardTransactionListToXs2aAccountReportMapper.mapToXs2aCardAccountReport(BookingStatus.BOTH, Collections.emptyList(), null);

        //Then
        assertFalse(xs2aCardAccountReport.isPresent());
    }

    @Test
    void mapToXs2aCardAccountReport_bookingStatusInformation() {
        //Given
        BookingStatus bookingStatus = BookingStatus.INFORMATION;

        List<SpiCardTransaction> spiCardTransactions = jsonReader.getObjectFromFile("json/SpiCardTransactions.json", new TypeReference<>() {
        });
        Xs2aCardAccountReport expectedXs2aCardAccountReport = jsonReader.getObjectFromFile("json/Xs2aCardAccountReport_information.json", Xs2aCardAccountReport.class);
        System.out.println("expectedXs2aCardAccountReport = " + expectedXs2aCardAccountReport);

        //When
        Optional<Xs2aCardAccountReport> xs2aCardAccountReport = spiCardTransactionListToXs2aAccountReportMapper.mapToXs2aCardAccountReport(bookingStatus, spiCardTransactions, null);

        //Then
        assertTrue(xs2aCardAccountReport.isPresent());
        assertEquals(expectedXs2aCardAccountReport, xs2aCardAccountReport.get());
    }
}
