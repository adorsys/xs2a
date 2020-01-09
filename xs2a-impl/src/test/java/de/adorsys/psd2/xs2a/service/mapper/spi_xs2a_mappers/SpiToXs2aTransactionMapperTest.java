/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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
import de.adorsys.psd2.xs2a.spi.domain.account.SpiTransaction;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@ContextConfiguration(
    classes = {SpiToXs2aTransactionMapperImpl.class, SpiToXs2aAmountMapperImpl.class, SpiToXs2aBalanceMapperImpl.class,
        SpiToXs2aExchangeRateMapperImpl.class, SpiToXs2aAccountReferenceMapperImpl.class})
public class SpiToXs2aTransactionMapperTest {
    private JsonReader jsonReader = new JsonReader();

    @Autowired
    private SpiToXs2aTransactionMapper mapper;

    @Test
    public void mapToXs2aTransaction() {
        Transactions transactions = mapper.mapToXs2aTransaction(
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-transaction.json", SpiTransaction.class));

        Transactions expectedTransactions = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/transactions.json", Transactions.class);
        assertEquals(expectedTransactions, transactions);
    }

    @Test
    public void mapToXs2aTransaction_nullValue() {
        assertNull(mapper.mapToXs2aTransaction(null));
    }
}
