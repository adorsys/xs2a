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

        List<SpiCardTransaction> spiCardTransactions = jsonReader.getObjectFromFile("json/SpiCardTransactions.json", new TypeReference<List<SpiCardTransaction>>() {
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
}
