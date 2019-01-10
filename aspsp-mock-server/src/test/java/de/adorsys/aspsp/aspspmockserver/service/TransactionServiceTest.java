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
import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountDetails;
import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountReference;
import de.adorsys.psd2.aspsp.mock.api.account.AspspTransaction;
import de.adorsys.psd2.aspsp.mock.api.common.AspspAmount;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransactionServiceTest {
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static LocalDate DATE = LocalDate.parse("2019-03-03");
    private static final String TRANSACTION_ID = "00001";
    private static final String WRONG_TRANSACTION_ID = "00002";
    private final String ACCOUNT_ID = "123456789";
    private final String WRONG_ACCOUNT_ID = "WRONG_ACC_ID";
    private static final String IBAN = "DE12345";
    private static final String IBAN_2 = "DE54321";
    private static final Currency EUR = Currency.getInstance("EUR");

    @InjectMocks
    private TransactionService transactionService;
    @Mock
    TransactionRepository transactionRepository;
    @Mock
    AccountService accountService;

    @Before
    public void setUp() {
        when(transactionRepository.findOneByTransactionIdAndAccount(IBAN, EUR, TRANSACTION_ID))
            .thenReturn(getTransaction());
        when(transactionRepository.findOneByTransactionIdAndAccount(IBAN, EUR, WRONG_TRANSACTION_ID))
            .thenReturn(null);
        when(transactionRepository.save(getTransaction()))
            .thenReturn(getTransaction());
        when(transactionRepository.findAllByDates(IBAN, EUR, DATE, DATE))
            .thenReturn(Collections.singletonList(getTransaction()));
        when(accountService.getAccountById(ACCOUNT_ID))
            .thenReturn(Optional.of(getDetails()));
        when(accountService.getAccountById(WRONG_ACCOUNT_ID))
            .thenReturn(Optional.empty());
    }

    @Test
    public void getTransactionById_Success() {
        //When
        Optional<AspspTransaction> respondedTransaction = transactionService.getTransactionById(TRANSACTION_ID, ACCOUNT_ID);

        //Then
        assertThat(respondedTransaction.get()).isEqualTo(getTransaction());
    }

    @Test
    public void getTransactionById_Failure_Wrong_TrId() {
        //When
        Optional<AspspTransaction> respondedTransaction = transactionService.getTransactionById(WRONG_TRANSACTION_ID, ACCOUNT_ID);

        //Then
        assertThat(respondedTransaction).isEqualTo(Optional.empty());
    }

    @Test
    public void getTransactionById_Failure_Wrong_AccId() {
        //When
        Optional<AspspTransaction> respondedTransaction = transactionService.getTransactionById(TRANSACTION_ID, WRONG_ACCOUNT_ID);

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
    public void getTransactionsByPeriod() {
        //When
        List<AspspTransaction> transactionList = transactionService.getTransactionsByPeriod(ACCOUNT_ID, DATE, DATE);

        //Then
        assertThat(transactionList).isNotEmpty();
    }

    @Test
    public void getTransactionByPeriod_Failure_Wrong_AccId() {
        //When
        List<AspspTransaction> respondedTransaction = transactionService.getTransactionsByPeriod(WRONG_ACCOUNT_ID, DATE, DATE);

        //Then
        assertThat(respondedTransaction).isEmpty();
    }

    private AspspTransaction getTransaction() {
        return new AspspTransaction(TRANSACTION_ID, null, null, "Creditor_id", null, null, DATE, DATE, new AspspAmount(EUR, BigDecimal.valueOf(1000)), null, "Creditor",
                                    new AspspAccountReference(null, IBAN, null, null, null, null, EUR), "Ult Creditor", "Debtor",
                                    new AspspAccountReference(null, IBAN_2, null, null, null, null, EUR), "Ult Debtor", null, null, "Purpose", "bankTrCode", "propBankTrCode");
    }

    private AspspAccountDetails getDetails() {
        return new AspspAccountDetails(ASPSP_ACCOUNT_ID, ACCOUNT_ID, IBAN, null, null, null, null, EUR, null, null, null, null, null, null, null, null, null);
    }

}
