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

package de.adorsys.aspsp.aspspmockserver.web;

import de.adorsys.aspsp.aspspmockserver.service.TransactionService;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiTransaction;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransactionControllerTest {
    private static LocalDate DATE =  LocalDate.parse("2019-03-03");
    private static final String TRANSACTION_ID = "00001";
    private static final String WRONG_TRANSACTION_ID = "00002";
    private final String ACCOUNT_ID = "123456789";
    private final String WRONG_ACCOUNT_ID = "WRONG_ACC_ID";
    private static final String IBAN = "DE12345";
    private static final String IBAN_2 = "DE54321";
    private static final Currency EUR = Currency.getInstance("EUR");

    @InjectMocks
    private TransactionController transactionController;

    @Mock
    private TransactionService transactionService;

    @Before
    public void setUp() {
        when(transactionService.getAllTransactions()).thenReturn(Collections.singletonList(getTransaction()));
        when(transactionService.getTransactionById(TRANSACTION_ID, ACCOUNT_ID)).thenReturn(Optional.of(getTransaction()));
        when(transactionService.getTransactionById(WRONG_TRANSACTION_ID, ACCOUNT_ID)).thenReturn(Optional.empty());
        when(transactionService.saveTransaction(getTransaction())).thenReturn(Optional.of(TRANSACTION_ID));
        when(transactionService.getTransactionsByPeriod(ACCOUNT_ID, DATE, DATE)).thenReturn(Collections.singletonList(getTransaction()));
        when(transactionService.getTransactionsByPeriod(WRONG_ACCOUNT_ID, DATE, DATE)).thenReturn(Collections.emptyList());
    }

    @Test
    public void readTransactionByIdTest_Success() {
        //When:
        ResponseEntity expectedResponse = transactionController.readTransactionById(TRANSACTION_ID, ACCOUNT_ID);

        //Then:
        assertThat(expectedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(expectedResponse.getBody()).isEqualTo(getTransaction());
    }

    @Test
    public void readTransactionByIdTest_Failure() {
        //When:
        ResponseEntity expectedResponse = transactionController.readTransactionById(WRONG_TRANSACTION_ID, ACCOUNT_ID);

        //Then:
        assertThat(expectedResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(expectedResponse.getBody()).isEqualTo(null);
    }

    @Test
    public void createTransactionTest() {
        //When:
        ResponseEntity expectedResponse = transactionController.createTransaction(getTransaction());

        //Then:
        assertThat(expectedResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(expectedResponse.getBody()).isEqualTo(TRANSACTION_ID);
    }

    @Test
    public void readTransactionsByDates() {
        //When:
        ResponseEntity expectedResponse = transactionController.readTransactionsByPeriod(ACCOUNT_ID, DATE, DATE);

        //Then:
        assertThat(expectedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(expectedResponse.getBody()).isEqualTo(Collections.singletonList(getTransaction()));
    }

    @Test
    public void readTransactionsByDates_Failure_AccId() {
        //When:
        ResponseEntity expectedResponse = transactionController.readTransactionsByPeriod(WRONG_ACCOUNT_ID, DATE, DATE);

        //Then:
        assertThat(expectedResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private SpiTransaction getTransaction() {
        return new SpiTransaction(TRANSACTION_ID, null, null, "Creditor_id", DATE, DATE, new SpiAmount(EUR, BigDecimal.valueOf(1000)), "Creditor",
            new SpiAccountReference(IBAN, null, null, null, null, EUR), "Ult Creditor", "Debtor",
            new SpiAccountReference(IBAN_2, null, null, null, null, EUR), "Ult Debtor", null, null, "Purpose", "bankTrCode");
    }
}
