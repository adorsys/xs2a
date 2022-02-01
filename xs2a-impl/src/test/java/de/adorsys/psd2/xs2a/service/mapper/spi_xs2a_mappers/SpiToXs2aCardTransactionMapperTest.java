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
import de.adorsys.psd2.xs2a.domain.CardTransaction;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiCardTransaction;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpiToXs2aCardTransactionMapperImpl.class, SpiToXs2aAmountMapperImpl.class, Xs2aToSpiAddressMapper.class, SpiToXs2aExchangeRateMapperImpl.class})
class SpiToXs2aCardTransactionMapperTest {
    @Autowired
    private SpiToXs2aCardTransactionMapper spiToXs2aCardTransactionMapper;

    private JsonReader jsonReader = new JsonReader();

    @Test
    void mapToXs2aCardTransaction() {
        //Given
        List<SpiCardTransaction> spiCardTransactions = jsonReader.getObjectFromFile("json/SpiCardTransactions.json", new TypeReference<List<SpiCardTransaction>>() {
        });
        SpiCardTransaction spiCardTransaction = spiCardTransactions.get(0);

        CardTransaction expectedCardTransaction = jsonReader.getObjectFromFile("json/CardAccountTransaction.json", CardTransaction.class);

        //When
        CardTransaction actualCardTransaction = spiToXs2aCardTransactionMapper.mapToXs2aCardTransaction(spiCardTransaction);

        //Then
        assertEquals(expectedCardTransaction, actualCardTransaction);
    }

    @Test
    void mapToXs2aCardTransactionList() {
        //Given
        List<SpiCardTransaction> spiCardTransactions = jsonReader.getObjectFromFile("json/SpiCardTransactions.json", new TypeReference<List<SpiCardTransaction>>() {
        });

        List<CardTransaction> expectedCardTransactions = jsonReader.getObjectFromFile("json/CardAccountTransactions.json",  new TypeReference<List<CardTransaction>>() {
        });

        //When
        List<CardTransaction>  actualCardTransactions = spiToXs2aCardTransactionMapper.mapToXs2aCardTransactionList(spiCardTransactions);

        //Then
        assertEquals(expectedCardTransactions, actualCardTransactions);
    }

    @Test
    void mapToXs2aCardTransaction_null() {
        //When
        CardTransaction actualCardTransaction = spiToXs2aCardTransactionMapper.mapToXs2aCardTransaction(null);

        //Then
        assertNull(actualCardTransaction);
    }

    @Test
    void mapToXs2aCardTransactionList_null() {
        //When
        List<CardTransaction>  actualCardTransactions = spiToXs2aCardTransactionMapper.mapToXs2aCardTransactionList(null);

        //Then
        assertNull(actualCardTransactions);
    }
}
