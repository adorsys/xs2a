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

package de.adorsys.aspsp.xs2a.domain.account;


import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.domain.Balance;
import de.adorsys.aspsp.xs2a.domain.BalanceType;
import de.adorsys.aspsp.xs2a.domain.CashAccountType;
import de.adorsys.aspsp.xs2a.domain.code.BankTransactionCode;
import de.adorsys.aspsp.xs2a.domain.code.PurposeCode;
import de.adorsys.psd2.model.*;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

import static de.adorsys.aspsp.xs2a.service.mapper.AccountModelMapper.*;

public class AccountModelMapperTest {

    @Test
    public void testBalanceMapping() {
        Balance balance = createBalance();

        de.adorsys.psd2.model.Balance result = mapToBalance(balance);

        Assert.assertEquals(balance.getBalanceAmount().getContent(), result.getBalanceAmount().getAmount());
        Assert.assertEquals(balance.getBalanceAmount().getCurrency().getCurrencyCode(), result.getBalanceAmount().getCurrency());
        Assert.assertEquals(balance.getBalanceType().name(), result.getBalanceType().name());
        List<ZoneOffset> validOffsets = ZoneId.systemDefault().getRules().getValidOffsets(balance.getLastChangeDateTime()); //TODO remove when OffsetDateTime is in xs2a
        Assert.assertEquals(balance.getLastChangeDateTime(), result.getLastChangeDateTime().atZoneSameInstant(validOffsets.get(0)).toLocalDateTime());
        Assert.assertEquals(balance.getLastCommittedTransaction(), result.getLastCommittedTransaction());
        Assert.assertEquals(balance.getReferenceDate(), result.getReferenceDate());
    }

    @Test
    public void testMapToAccountList() {
        List<AccountDetails> list = new ArrayList<>();
        Balance inputBalance = createBalance();

        list.add(new AccountDetails("1", "2", "3", "4", "5", "6", Currency.getInstance("EUR"), "8", "9", CashAccountType.CURRENT_ACCOUNT, "11", new ArrayList<Balance>()));
        list.add(new AccountDetails("x1", "x2", "x3", "x4", "x5", "x6", Currency.getInstance("EUR"), "x8", "x9", CashAccountType.CURRENT_ACCOUNT, "x11", Arrays.asList(inputBalance)));
        AccountDetails accountDetails = new AccountDetails("y1", "y2", "y3", "y4", "y5", "y6", Currency.getInstance("EUR"), "y8", "y9", CashAccountType.CURRENT_ACCOUNT, "y11", new ArrayList<Balance>());
        accountDetails.setLinks(createLinks());
        list.add(accountDetails);
        Map<String, List<AccountDetails>> map = Collections.singletonMap("TEST", list);
        AccountList result = mapToAccountList(map);

        de.adorsys.psd2.model.AccountDetails account = result.getAccounts().get(1);
        Assert.assertEquals("x2", account.getIban());
        de.adorsys.psd2.model.Balance balance = account.getBalances().get(0);
        Assert.assertEquals("4711", balance.getLastCommittedTransaction());
        Assert.assertEquals("EUR", balance.getBalanceAmount().getCurrency());
        Assert.assertEquals("1000", balance.getBalanceAmount().getAmount());
        List<ZoneOffset> validOffsets = ZoneId.systemDefault().getRules().getValidOffsets(inputBalance.getLastChangeDateTime()); //TODO remove when OffsetDateTime is in xs2a
        Assert.assertEquals(inputBalance.getLastChangeDateTime(), balance.getLastChangeDateTime().atZoneSameInstant(validOffsets.get(0)).toLocalDateTime());

        de.adorsys.psd2.model.AccountDetails account2 = result.getAccounts().get(2);
        Map<String, String> linksMap = account2.getLinks();
        Assert.assertEquals("http://scaOAuth.xx", linksMap.get("scaOAuth"));
        Assert.assertEquals("http://linkToStatus.xx", linksMap.get("status"));
        Assert.assertEquals("http://linkToSelf.xx", linksMap.get("self"));
    }

    @Test
    public void testMapToTransaction() {
        Transactions transactions = createTransactions();
        TransactionDetails transactionDetails = mapToTransaction(transactions);

        Amount amount = transactions.getAmount();
        de.adorsys.psd2.model.Amount amountTarget = transactionDetails.getTransactionAmount();

        Assert.assertNotNull(amountTarget);

        Assert.assertEquals(amount.getContent(), amountTarget.getAmount());
        Assert.assertEquals(amount.getCurrency().getCurrencyCode(), amountTarget.getCurrency());
        Assert.assertEquals(transactions.getBankTransactionCodeCode().getCode(), transactionDetails.getBankTransactionCode());
        Assert.assertEquals(transactions.getBookingDate(), transactionDetails.getBookingDate());
        Assert.assertEquals(transactions.getCreditorAccount().getIban(), ((AccountReferenceIban)transactionDetails.getCreditorAccount()).getIban());
        Assert.assertEquals(transactions.getCreditorAccount().getCurrency().getCurrencyCode(), ((AccountReferenceIban)transactionDetails.getCreditorAccount()).getCurrency());
        Assert.assertEquals(transactions.getCreditorId(), transactionDetails.getCreditorId());
        Assert.assertEquals(transactions.getCreditorName(), transactionDetails.getCreditorName());
        Assert.assertEquals(transactions.getDebtorAccount().getIban(), ((AccountReferenceIban)transactionDetails.getDebtorAccount()).getIban());
        Assert.assertEquals(transactions.getDebtorAccount().getCurrency().getCurrencyCode(), ((AccountReferenceIban) transactionDetails.getDebtorAccount()).getCurrency());
        Assert.assertEquals(transactions.getPurposeCode().getCode(), transactionDetails.getPurposeCode().name());
        Assert.assertEquals(transactions.getRemittanceInformationStructured(), transactionDetails.getRemittanceInformationStructured());
        Assert.assertEquals(transactions.getRemittanceInformationUnstructured(), transactionDetails.getRemittanceInformationUnstructured());
        Assert.assertEquals(transactions.getTransactionId(), transactionDetails.getTransactionId());
        Assert.assertEquals(transactions.getUltimateCreditor(), transactionDetails.getUltimateCreditor());
        Assert.assertEquals(transactions.getUltimateDebtor(), transactionDetails.getUltimateDebtor());
        Assert.assertEquals(transactions.getValueDate(), transactionDetails.getValueDate());
    }

    @Test
    public void testMapToAccountReport() {
        Transactions [] tx = { createTransactions(), createTransactions(), createTransactions()};
        Transactions [] tx1 = { createTransactions(), createTransactions()};
        AccountReport accountReport = new AccountReport(tx, tx1);
        accountReport.setLinks(createLinks());

        de.adorsys.psd2.model.AccountReport accountReport1 = mapToAccountReport(accountReport);
        //transactions mapping tested in testMapToTransaction
        Assert.assertEquals(accountReport.getBooked().length, accountReport1.getBooked().size());
        Assert.assertEquals(accountReport.getPending().length, accountReport1.getPending().size());
        Assert.assertEquals(accountReport.getLinks().getScaOAuth(), accountReport1.getLinks().get("scaOAuth"));
        Assert.assertEquals(3, accountReport1.getLinks().size());
    }

    private Balance createBalance() {
        Balance balance = new Balance();

        Amount amount = createAmount();

        balance.setBalanceAmount(amount);
        balance.setBalanceType(BalanceType.AUTHORISED);
        balance.setLastChangeDateTime(LocalDateTime.now());
        balance.setLastCommittedTransaction("4711");
        balance.setReferenceDate(LocalDate.now());
        return balance;
    }

    private Amount createAmount() {
        Amount amount = new Amount();
        amount.setCurrency(Currency.getInstance("EUR"));
        amount.setContent("1000");
        return amount;
    }

    private AccountReference createAccountReference() {
        AccountReference accountReference = new AccountReference();
        accountReference.setBban("bban");
        accountReference.setCurrency(Currency.getInstance("EUR"));
        accountReference.setIban("DE1234");
        accountReference.setMaskedPan("maskedPan");
        accountReference.setMsisdn("msisdn");
        accountReference.setPan("pan");
        return accountReference;
    }

    private Transactions createTransactions() {
        Transactions transactions = new Transactions();
        Amount amount = createAmount();
        transactions.setAmount(amount);
        transactions.setBankTransactionCodeCode(new BankTransactionCode("code"));
        transactions.setBookingDate(LocalDate.now());
        transactions.setCreditorAccount(createAccountReference());
        transactions.setCreditorId("creditorId");
        transactions.setCreditorName("Creditor Name");
        transactions.setDebtorAccount(createAccountReference());
        transactions.setDebtorName("Debtor Name");
        transactions.setEndToEndId("endToEndId");
        transactions.setMandateId("mandateId");
        transactions.setPurposeCode(new PurposeCode("BKDF"));
        transactions.setRemittanceInformationStructured("setRemittanceInformationStructured");
        transactions.setRemittanceInformationUnstructured("setRemittanceInformationUnstructured");
        transactions.setTransactionId("transactionId");
        transactions.setUltimateCreditor("ultimateCreditor");
        transactions.setUltimateDebtor("ultimateDebtor");
        transactions.setValueDate(LocalDate.now());
        return transactions;
    }

    private Links createLinks() {
        Links links = new Links();
        links.setScaOAuth("http://scaOAuth.xx");
        links.setStatus("http://linkToStatus.xx");
        links.setSelf("http://linkToSelf.xx");
        return links;
    }
}
