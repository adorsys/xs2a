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

import de.adorsys.psd2.xs2a.domain.EntryDetails;
import de.adorsys.psd2.xs2a.domain.TransactionInfo;
import de.adorsys.psd2.xs2a.domain.Transactions;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAdditionalInformationStructured;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiTransaction;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {SpiToXs2aTransactionMapperImpl.class, SpiToXs2aAmountMapperImpl.class, SpiToXs2aBalanceMapperImpl.class,
        SpiToXs2aExchangeRateMapperImpl.class, SpiToXs2aAccountReferenceMapperImpl.class})
class SpiToXs2aTransactionMapperTest {
    private final JsonReader jsonReader = new JsonReader();

    @Autowired
    private SpiToXs2aTransactionMapper mapper;

    @Test
    void mapToXs2aTransaction() {
        //Given
        Transactions expected = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/transactions.json", Transactions.class);

        //When
        Transactions actual = mapper.mapToXs2aTransaction(
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-transaction.json", SpiTransaction.class));

        //Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void mapToXs2aTransaction_nullValue() {
        //When
        Transactions actual = mapper.mapToXs2aTransaction(null);

        //Then
        assertThat(actual).isNull();
    }

    @Test
    void mapToXs2aTransactionList() {
        //Given
        Transactions expected = jsonReader
            .getObjectFromFile("json/service/mapper/spi_xs2a_mappers/transactions.json", Transactions.class);
        List<SpiTransaction> spiTransactions = Collections
            .singletonList(jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-transaction.json", SpiTransaction.class));

        //When
        List<Transactions> actual = mapper.mapToXs2aTransactionList(spiTransactions);

        //Then
        assertThat(actual)
            .hasSize(1)
            .contains(expected);
    }

    @Test
    void mapToXs2aTransactionList_nullValue() {
        //When
        List<Transactions> actual = mapper.mapToXs2aTransactionList(null);

        //Then
        assertThat(actual).isNull();
    }

    @Test
    void mapToEntryDetails_nullInput() {
        //When
        EntryDetails actual = mapper.mapToEntryDetails(null);

        //Then
        assertThat(actual).isNull();
    }

    @Test
    void mapToEntryDetailsList_nullInput() {
        //When
        List<EntryDetails> actual = mapper.mapToEntryDetailsList(null);

        //Then
        assertThat(actual).isNull();
    }

    @Test
    void mapToTransactionInfo_nullInput() {
        //When
        TransactionInfo actual = mapper.mapToTransactionInfo(null);

        //Then
        assertThat(actual).isNull();
    }

    @Test
    void mapToXs2aTransaction_withAdditionalInfoStructured() {
        //Given
        Xs2aAdditionalInformationStructured expected = jsonReader
            .getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-additional-info-structured-expected.json", Xs2aAdditionalInformationStructured.class);

        //When
        Xs2aAdditionalInformationStructured actual = mapper
            .mapToXs2aTransaction(getTestSpiTransaction_additionalInfo()).getAdditionalInformationStructured();

        //Then
        assertThat(actual)
            .isNotNull()
            .isEqualTo(expected);
    }

    @Test
    void mapToXs2aTransaction_withAdditionalInfoStructuredNullStandOrderDetails() {
        //When
        Transactions actual = mapper.mapToXs2aTransaction(getTestSpiTransaction_additionalInfo_nullStandingOrderDetails());

        //Then
        assertThat(actual.getAdditionalInformationStructured().getStandingOrderDetails()).isNull();
    }

    private SpiTransaction getTestSpiTransaction_additionalInfo() {
        return jsonReader
            .getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-transaction-additional-info-struct.json", SpiTransaction.class);
    }

    private SpiTransaction getTestSpiTransaction_additionalInfo_nullStandingOrderDetails() {
        return jsonReader
            .getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-transaction-additional-info-struct-stand-order-details-null.json", SpiTransaction.class);
    }
}
