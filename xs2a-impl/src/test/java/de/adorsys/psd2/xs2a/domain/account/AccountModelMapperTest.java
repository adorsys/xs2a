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

package de.adorsys.psd2.xs2a.domain.account;

import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.BalanceType;
import de.adorsys.psd2.xs2a.domain.CashAccountType;
import de.adorsys.psd2.xs2a.domain.*;
import de.adorsys.psd2.xs2a.domain.code.BankTransactionCode;
import de.adorsys.psd2.xs2a.domain.code.Xs2aPurposeCode;
import de.adorsys.psd2.xs2a.service.mapper.AccountModelMapper;
import de.adorsys.psd2.xs2a.service.mapper.AmountModelMapper;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import de.adorsys.psd2.xs2a.web.mapper.HrefLinkMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccountModelMapperTest {
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final String HREF = "href";

    private JsonReader jsonReader = new JsonReader();

    @Mock
    private AmountModelMapper amountModelMapper;
    @Mock
    private HrefLinkMapper hrefLinkMapper;

    @InjectMocks
    private AccountModelMapper accountModelMapper;

    @Before
    public void setUp() {
        when(hrefLinkMapper.mapToLinksMap(createLinks())).thenReturn(createHrefLinkMap());
        when(amountModelMapper.mapToAmount(createXs2aAmount())).thenReturn(createAmount());
    }

    @Test
    public void testBalanceMapping() {
        Xs2aBalance balance = createBalance();

        Balance result = accountModelMapper.mapToBalance(balance);
        assertNotNull(result);

        Xs2aAmount expectedBalanceAmount = balance.getBalanceAmount();
        Amount actualBalanceAmount = result.getBalanceAmount();
        assertNotNull(expectedBalanceAmount);

        assertEquals(expectedBalanceAmount.getAmount(), actualBalanceAmount.getAmount());
        assertEquals(expectedBalanceAmount.getCurrency().getCurrencyCode(), actualBalanceAmount.getCurrency());
        assertEquals(balance.getBalanceType().name(), result.getBalanceType().name());

        LocalDateTime expectedLastChangeDateTime = balance.getLastChangeDateTime();
        assertNotNull(expectedLastChangeDateTime);

        OffsetDateTime actualLastChangeDateTime = result.getLastChangeDateTime();
        assertNotNull(actualLastChangeDateTime);

        List<ZoneOffset> validOffsets = ZoneId.systemDefault().getRules().getValidOffsets(expectedLastChangeDateTime); //TODO remove when OffsetDateTime is in xs2a
        assertEquals(expectedLastChangeDateTime, actualLastChangeDateTime.atZoneSameInstant(validOffsets.get(0)).toLocalDateTime());
        assertEquals(balance.getLastCommittedTransaction(), result.getLastCommittedTransaction());
        assertEquals(balance.getReferenceDate(), result.getReferenceDate());
    }

    @Test
    public void testMapToAccountList() {
        List<Xs2aAccountDetails> accountDetailsList = new ArrayList<>();
        Xs2aBalance inputBalance = createBalance();

        accountDetailsList.add(new Xs2aAccountDetails(ASPSP_ACCOUNT_ID, "1", "2", "3", "4",
                                                      "5", "6", Currency.getInstance("EUR"), "8", "9", CashAccountType.CACC,
                                                      AccountStatus.ENABLED, "11", "linked", Xs2aUsageType.PRIV, "details", new ArrayList<>()));
        accountDetailsList.add(new Xs2aAccountDetails(ASPSP_ACCOUNT_ID, "x1", "x2", "x3", "x4",
                                                      "x5", "x6", Currency.getInstance("EUR"), "x8", "x9", CashAccountType.CACC,
                                                      AccountStatus.ENABLED, "x11", "linked2", Xs2aUsageType.ORGA, "details2", Arrays.asList(inputBalance)));
        Xs2aAccountDetails accountDetails = new Xs2aAccountDetails(ASPSP_ACCOUNT_ID, "y1", "y2", "y3", "y4",
                                                                   "y5", "y6", Currency.getInstance("EUR"), "y8", "y9", CashAccountType.CACC,
                                                                   AccountStatus.ENABLED, "y11", "linked3", Xs2aUsageType.PRIV, "details3", new ArrayList<>());
        accountDetails.setLinks(createLinks());
        accountDetailsList.add(accountDetails);

        Xs2aAccountListHolder xs2aAccountListHolder = new Xs2aAccountListHolder(accountDetailsList, null);
        AccountList result = accountModelMapper.mapToAccountList(xs2aAccountListHolder);
        assertNotNull(result);

        List<AccountDetails> accounts = result.getAccounts();
        assertNotNull(accounts);

        AccountDetails secondAccount = accounts.get(1);
        assertNotNull(secondAccount);

        assertEquals("x2", secondAccount.getIban());

        BalanceList balances = secondAccount.getBalances();
        assertNotNull(balances);

        Balance balance = balances.get(0);
        assertNotNull(balance);

        assertEquals("4711", balance.getLastCommittedTransaction());

        Amount balanceAmount = balance.getBalanceAmount();
        assertNotNull(balanceAmount);

        assertEquals("EUR", balanceAmount.getCurrency());
        assertEquals("1000", balanceAmount.getAmount());

        LocalDateTime expectedLastChangeDateTime = inputBalance.getLastChangeDateTime();
        assertNotNull(expectedLastChangeDateTime);

        OffsetDateTime actualLastChangeDateTime = balance.getLastChangeDateTime();
        assertNotNull(actualLastChangeDateTime);

        List<ZoneOffset> validOffsets = ZoneId.systemDefault().getRules().getValidOffsets(expectedLastChangeDateTime); //TODO remove when OffsetDateTime is in xs2a
        assertEquals(expectedLastChangeDateTime, actualLastChangeDateTime.atZoneSameInstant(validOffsets.get(0)).toLocalDateTime());

        AccountDetails thirdAccount = accounts.get(2);
        assertNotNull(thirdAccount);

        Map links = thirdAccount.getLinks();
        assertNotNull(links);

        assertEquals("{" + HREF + "=http://scaOAuth.xx}", links.get("scaOAuth").toString());
        assertEquals("{" + HREF + "=http://linkToStatus.xx}", links.get("status").toString());
        assertEquals("{" + HREF + "=http://linkToSelf.xx}", links.get("self").toString());

        assertEquals("CACC", secondAccount.getCashAccountType());
        assertEquals("details2", secondAccount.getDetails());
        assertEquals("linked2", secondAccount.getLinkedAccounts());
        assertEquals("x6", secondAccount.getMsisdn());
        assertEquals("x8", secondAccount.getName());
        assertEquals("x9", secondAccount.getProduct());
        assertEquals(de.adorsys.psd2.model.AccountStatus.ENABLED, secondAccount.getStatus());
        assertEquals(AccountDetails.UsageEnum.ORGA, secondAccount.getUsage());
    }

    @Test
    public void testMapToTransaction() {
        Transactions transactions = createTransactions();
        TransactionDetails transactionDetails = accountModelMapper.mapToTransaction(transactions);
        assertNotNull(transactionDetails);

        Xs2aAmount amount = transactions.getAmount();
        Amount amountTarget = transactionDetails.getTransactionAmount();
        assertNotNull(amountTarget);

        assertEquals(amount.getAmount(), amountTarget.getAmount());
        assertEquals(amount.getCurrency().getCurrencyCode(), amountTarget.getCurrency());

        BankTransactionCode bankTransactionCodeCode = transactions.getBankTransactionCodeCode();
        assertNotNull(bankTransactionCodeCode);

        assertEquals(bankTransactionCodeCode.getCode(), transactionDetails.getBankTransactionCode());
        assertEquals(transactions.getBookingDate(), transactionDetails.getBookingDate());

        assertEquals(transactions.getCheckId(), transactionDetails.getCheckId());

        AccountReference expectedCreditorAccount = transactions.getCreditorAccount();
        assertNotNull(expectedCreditorAccount);

        de.adorsys.psd2.model.AccountReference actualCreditorAccount = transactionDetails.getCreditorAccount();
        assertNotNull(actualCreditorAccount);

        assertEquals(expectedCreditorAccount.getIban(), actualCreditorAccount.getIban());
        assertEquals(expectedCreditorAccount.getCurrency().getCurrencyCode(), actualCreditorAccount.getCurrency());
        assertEquals(transactions.getCreditorId(), transactionDetails.getCreditorId());
        assertEquals(transactions.getCreditorName(), transactionDetails.getCreditorName());

        AccountReference expectedDebtorAccount = transactions.getDebtorAccount();
        assertNotNull(expectedDebtorAccount);

        de.adorsys.psd2.model.AccountReference actualDebtorAccount = transactionDetails.getDebtorAccount();
        assertNotNull(actualDebtorAccount);

        assertEquals(expectedDebtorAccount.getIban(), actualDebtorAccount.getIban());
        assertEquals(expectedDebtorAccount.getCurrency().getCurrencyCode(), actualDebtorAccount.getCurrency());

        Xs2aPurposeCode expectedPurposeCode = transactions.getPurposeCode();
        assertNotNull(expectedPurposeCode);

        PurposeCode actualPurposeCode = transactionDetails.getPurposeCode();
        assertNotNull(actualPurposeCode);

        assertEquals(expectedPurposeCode.getCode(), actualPurposeCode.name());
        assertEquals(transactions.getRemittanceInformationStructured(), transactionDetails.getRemittanceInformationStructured());
        assertEquals(transactions.getRemittanceInformationUnstructured(), transactionDetails.getRemittanceInformationUnstructured());
        assertEquals(transactions.getTransactionId(), transactionDetails.getTransactionId());
        assertEquals(transactions.getUltimateCreditor(), transactionDetails.getUltimateCreditor());
        assertEquals(transactions.getUltimateDebtor(), transactionDetails.getUltimateDebtor());
        assertEquals(transactions.getValueDate(), transactionDetails.getValueDate());

        assertEquals(transactions.getProprietaryBankTransactionCode(),
                     transactionDetails.getProprietaryBankTransactionCode());
        assertEquals(transactions.getMandateId(), transactionDetails.getMandateId());
        assertEquals(transactions.getEntryReference(), transactionDetails.getEntryReference());
        assertEquals(transactions.getEndToEndId(), transactionDetails.getEndToEndId());
    }

    @Test
    public void testMapToAccountReport() {
        List<Transactions> bookedTransactions = Arrays.asList(createTransactions(), createTransactions(), createTransactions());
        List<Transactions> pendingTransactions = Arrays.asList(createTransactions(), createTransactions());
        Xs2aAccountReport accountReport = new Xs2aAccountReport(bookedTransactions, pendingTransactions, null);
        accountReport.setLinks(createLinks());

        AccountReport result = accountModelMapper.mapToAccountReport(accountReport);
        assertNotNull(result);

        //transactions mapping tested in testMapToTransaction
        assertEquals(accountReport.getBooked().size(), result.getBooked().size());

        List<Transactions> expectedPending = accountReport.getPending();
        assertNotNull(expectedPending);

        TransactionList actualPending = result.getPending();
        assertNotNull(actualPending);

        assertEquals(expectedPending.size(), actualPending.size());

        Map links = result.getLinks();
        assertEquals("{" + HREF + "=" + accountReport.getLinks().getScaOAuth() + "}", links.get("scaOAuth").toString());
    }

    @Test
    public void mapToTransactionsResponse200Json_withEmptyTransactionsInAccountReport_shouldReturnNullLists() {
        // Given
        when(hrefLinkMapper.mapToLinksMap(null))
            .thenReturn(null);

        Xs2aAccountReport xs2aAccountReport = jsonReader.getObjectFromFile("json/service/mapper/account/xs2a-account-report-empty-transactions.json", Xs2aAccountReport.class);
        AccountReport emptyAccountReport = new AccountReport();

        // When
        AccountReport actualAccountReport = accountModelMapper.mapToAccountReport(xs2aAccountReport);

        // Then
        assertEquals(emptyAccountReport, actualAccountReport);
    }

    @Test
    public void mapToTransactionsResponse200Json_withNullPendingTransactionInAccountReport_shouldReturnNullList() {
        // Given
        when(hrefLinkMapper.mapToLinksMap(null))
            .thenReturn(null);

        Xs2aAccountReport xs2aAccountReport = jsonReader.getObjectFromFile("json/service/mapper/account/xs2a-account-report-null-pending-transactions.json", Xs2aAccountReport.class);
        AccountReport accountReportWithBookedTransaction = jsonReader.getObjectFromFile("json/service/mapper/account/account-report-booked-transaction.json", AccountReport.class);

        // When
        AccountReport actualAccountReport = accountModelMapper.mapToAccountReport(xs2aAccountReport);

        // Then
        assertEquals(accountReportWithBookedTransaction, actualAccountReport);
    }

    private Xs2aBalance createBalance() {
        Xs2aBalance balance = new Xs2aBalance();

        Xs2aAmount amount = createXs2aAmount();

        balance.setBalanceAmount(amount);
        balance.setBalanceType(BalanceType.AUTHORISED);
        balance.setLastChangeDateTime(LocalDateTime.now());
        balance.setLastCommittedTransaction("4711");
        balance.setReferenceDate(LocalDate.now());
        return balance;
    }

    private Amount createAmount() {
        Amount amount = new Amount();
        amount.setCurrency("EUR");
        amount.setAmount("1000");
        return amount;
    }

    private Xs2aAmount createXs2aAmount() {
        Xs2aAmount amount = new Xs2aAmount();
        amount.setCurrency(Currency.getInstance("EUR"));
        amount.setAmount("1000");
        return amount;
    }

    private AccountReference createAccountReference() {
        AccountReference xs2aAccountReference = new AccountReference();
        xs2aAccountReference.setBban("bban");
        xs2aAccountReference.setCurrency(Currency.getInstance("EUR"));
        xs2aAccountReference.setIban("DE1234");
        xs2aAccountReference.setMaskedPan("maskedPan");
        xs2aAccountReference.setMsisdn("msisdn");
        xs2aAccountReference.setPan("pan");
        return xs2aAccountReference;
    }

    private Transactions createTransactions() {
        Transactions transactions = new Transactions();
        Xs2aAmount amount = createXs2aAmount();
        transactions.setAmount(amount);
        transactions.setBankTransactionCodeCode(new BankTransactionCode("code"));
        transactions.setBookingDate(LocalDate.now());
        transactions.setCheckId("Check id");
        transactions.setCreditorAccount(createAccountReference());
        transactions.setCreditorId("creditorId");
        transactions.setCreditorName("Creditor Name");
        transactions.setDebtorAccount(createAccountReference());
        transactions.setDebtorName("Debtor Name");
        transactions.setEndToEndId("endToEndId");
        transactions.setEntryReference("Entry reference");
        transactions.setMandateId("mandateId");
        transactions.setProprietaryBankTransactionCode("Proprietary code");
        transactions.setPurposeCode(new Xs2aPurposeCode("BKDF"));
        transactions.setRemittanceInformationStructured("setRemittanceInformationStructured");
        transactions.setRemittanceInformationUnstructured("setRemittanceInformationUnstructured");
        transactions.setTransactionId("transactionId");
        transactions.setUltimateCreditor("ultimateCreditor");
        transactions.setUltimateDebtor("ultimateDebtor");
        transactions.setValueDate(LocalDate.now());

        Xs2aExchangeRate exchangeRate = createExchangeRate();
        transactions.setExchangeRate(Collections.singletonList(exchangeRate));

        return transactions;
    }

    private Links createLinks() {
        Links links = new Links();
        links.setScaOAuth("http://scaOAuth.xx");
        links.setStatus("http://linkToStatus.xx");
        links.setSelf("http://linkToSelf.xx");
        return links;
    }

    private Map createHrefLinkMap() {
        Map<String, Map> hrefLinkMap = new HashMap<>();
        hrefLinkMap.put("scaOAuth", Collections.singletonMap(HREF, "http://scaOAuth.xx"));
        hrefLinkMap.put("status", Collections.singletonMap(HREF, "http://linkToStatus.xx"));
        hrefLinkMap.put("self", Collections.singletonMap(HREF, "http://linkToSelf.xx"));
        return hrefLinkMap;
    }

    private Xs2aExchangeRate createExchangeRate() {
        Xs2aExchangeRate exchangeRate = new Xs2aExchangeRate();
        exchangeRate.setUnitCurrency("unit currency");
        exchangeRate.setExchangeRate("rate");
        exchangeRate.setSourceCurrency("source currency");
        exchangeRate.setQuotationDate(LocalDate.of(2017, 1, 1));
        exchangeRate.setContractIdentification("Rate contract");
        return exchangeRate;
    }
}
