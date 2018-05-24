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

package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.aspspmockserver.repository.TransactionRepository;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiTransaction;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TransactionServiceTest {
    private static Date DATE = new Date(1122334455);
    private static final String TRANSACTION_ID = "00001";
    private static final String WRONG_TRANSACTION_ID = "00002";
    private static final String IBAN = "DE12345";
    private static final String IBAN_2 = "DE54321";
    private static final Currency EUR = Currency.getInstance("EUR");

    @Autowired
    private TransactionService transactionService;
    @MockBean
    TransactionRepository transactionRepository;

    @Before
    public void setUp() {
        when(transactionRepository.findOne(TRANSACTION_ID))
            .thenReturn(getTransaction());
        when(transactionRepository.findOne(WRONG_TRANSACTION_ID))
            .thenReturn(null);
        when(transactionRepository.save(getTransaction()))
            .thenReturn(getTransaction());
        when(transactionRepository.findAllByDates(IBAN, EUR, DATE, DATE))
            .thenReturn(Collections.singletonList(getTransaction()));
    }

    @Test
    public void getTransactionById_Success() {
        //When
        Optional<SpiTransaction> respondedTransaction = transactionService.getTransactionById(TRANSACTION_ID);

        //Then
        assertThat(respondedTransaction.get()).isEqualTo(getTransaction());
    }

    @Test
    public void getTransactionById_Failure() {
        //When
        Optional<SpiTransaction> respondedTransaction = transactionService.getTransactionById(WRONG_TRANSACTION_ID);

        //Then
        assertThat(respondedTransaction).isEqualTo(Optional.empty());
    }

    @Test
    public void saveTransaction_Success() {
        //When
        Optional<String> respondedTransactionId = transactionService.saveTransaction(getTransaction());

        //Then
        assertThat(respondedTransactionId.get()).isEqualTo(TRANSACTION_ID);
    }

    @Test
    public void getTransactionsByAccountId() {
        //When
        List<SpiTransaction> transactionList = transactionService.getTransactionsByPeriod(IBAN, EUR, DATE, DATE);

        //Then
        assertThat(transactionList).isNotEmpty();
    }

    private SpiTransaction getTransaction() {
        return new SpiTransaction(TRANSACTION_ID, null, null, "Creditor_id", DATE, DATE, new SpiAmount(EUR, "1000"), "Creditor",
            new SpiAccountReference(IBAN, null, null, null, null, EUR), "Ult Creditor", "Debtor",
            new SpiAccountReference(IBAN_2, null, null, null, null, EUR), "Ult Debtor", null, null, "Purpose", "bankTrCode");
    }

}
