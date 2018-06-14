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

package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.account.AccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.account.AccountReport;
import de.adorsys.aspsp.xs2a.domain.consent.AccountAccess;
import de.adorsys.aspsp.xs2a.domain.consent.AccountAccessType;
import de.adorsys.aspsp.xs2a.exception.MessageCategory;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.spi.domain.account.*;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.*;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountServiceTest {
    private final String ACCOUNT_ID = "33333-999999999";
    private final String ACCOUNT_ID_1 = "77777-999999999";
    private final String WRONG_ACCOUNT_ID = "Wrong account";
    private final String IBAN = "DE123456789";
    private final String IBAN_1 = "DE987654321";
    private final Currency CURRENCY = Currency.getInstance("EUR");
    private final Currency CURRENCY_1 = Currency.getInstance("USD");
    private final String CONSENT_ID_WB = "111222333";
    private final String CONSENT_ID_WOB = "333222111";
    private final String CONSENT_ID_WT = "777999777";
    private final String WRONG_CONSENT_ID = "Wromg consent id";
    private final String TRANSACTION_ID = "0001";
    private final Date DATE = new Date(123456789L);

    @Autowired
    private AccountService accountService;

    @MockBean(name = "accountSpi")
    private AccountSpi accountSpi;
    @MockBean
    private ConsentService consentService;

    @Before
    public void setUp() {
        //getAccountDetailsByAccountId_WoB_Success
        when(accountSpi.readAccountDetails(ACCOUNT_ID)).thenReturn(getSpiAccountDetails(ACCOUNT_ID, IBAN));
        when(consentService.getValidatedConsent(CONSENT_ID_WOB)).thenReturn(getAccessResponse(getReferences(IBAN, IBAN_1), null, null, false, false));
        when(consentService.isValidAccountByAccess(IBAN, CURRENCY, getReferences(IBAN, IBAN_1))).thenReturn(true);
        //getAccountDetailsByAccountId_WB_Success
        when(consentService.getValidatedConsent(CONSENT_ID_WB)).thenReturn(getAccessResponse(getReferences(IBAN, IBAN_1), getReferences(IBAN, IBAN_1), null, false, false));
        when(consentService.isValidAccountByAccess(IBAN, CURRENCY, getReferences(IBAN, IBAN_1))).thenReturn(true);
        //getAccountDetailsByAccountId_Failure_wrongAccount
        when(accountSpi.readAccountDetails(WRONG_ACCOUNT_ID)).thenReturn(null);
        //getAccountDetailsByAccountId_Failure_wrongConsent
        when(consentService.getValidatedConsent(WRONG_CONSENT_ID)).thenReturn(ResponseObject.<AccountAccess>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.CONSENT_UNKNOWN_403))).build());

        //getAccountDetailsListByConsent_Success
        when(accountSpi.readAccountDetailsByIban(IBAN)).thenReturn(Collections.singletonList(getSpiAccountDetails(ACCOUNT_ID, IBAN)));
        when(accountSpi.readAccountDetailsByIban(IBAN_1)).thenReturn(Collections.singletonList(getSpiAccountDetails(ACCOUNT_ID_1, IBAN_1)));

        when(accountSpi.readAccountDetails(WRONG_ACCOUNT_ID)).thenReturn(null);

        //getAccountReport_ByTransactionId_Success
        when(consentService.getValidatedConsent(CONSENT_ID_WT)).thenReturn(getAccessResponse(getReferences(IBAN, IBAN_1), null, getReferences(IBAN, IBAN_1), false, false));
        when(accountSpi.readTransactionsById(TRANSACTION_ID)).thenReturn(Collections.singletonList(getSpiTransaction()));

        when(accountSpi.readTransactionsByPeriod(IBAN, CURRENCY, DATE, DATE, SpiBookingStatus.BOTH)).thenReturn(Collections.singletonList(getSpiTransaction()));
    }

    //Get Account By AccountId
    @Test
    public void getAccountDetailsByAccountId_WoB_Success() {
        //When:
        ResponseObject<AccountDetails> response = accountService.getAccountDetails(CONSENT_ID_WOB, ACCOUNT_ID, false, true);

        //Then:
        assertThat(response.getBody().getId()).isEqualTo(ACCOUNT_ID);
        assertThat(response.getBody().getBalances()).isEqualTo(null);
    }

    @Test
    public void getAccountDetailsByAccountId_WB_Success() {
        //When:
        ResponseObject<AccountDetails> response = accountService.getAccountDetails(CONSENT_ID_WB, ACCOUNT_ID, true, true);

        //Then:
        assertThat(response.getBody().getId()).isEqualTo(ACCOUNT_ID);
        assertThat(response.getBody().getBalances()).isEqualTo(getBalancesList());
    }

    @Test
    public void getAccountDetailsByAccountId_Failure_wrongAccount() {
        //When:
        ResponseObject<AccountDetails> response = accountService.getAccountDetails(CONSENT_ID_WB, WRONG_ACCOUNT_ID, true, true);

        //Then:
        assertThat(response.hasError()).isEqualTo(true);
        assertThat(response.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
        assertThat(response.getError().getTppMessage().getCode()).isEqualTo(RESOURCE_UNKNOWN_404);
    }

    @Test
    public void getAccountDetailsByAccountId_Failure_wrongConsent() {
        //When:
        ResponseObject<AccountDetails> response = accountService.getAccountDetails(WRONG_CONSENT_ID, ACCOUNT_ID, true, true);

        //Then:
        assertThat(response.hasError()).isEqualTo(true);
        assertThat(response.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
        assertThat(response.getError().getTppMessage().getCode()).isEqualTo(CONSENT_UNKNOWN_403);
    }

    //Get AccountsList By Consent
    @Test
    public void getAccountDetailsListByConsent_Success_WOB() {
        //When:
        ResponseObject<Map<String, List<AccountDetails>>> response = accountService.getAccountDetailsList(CONSENT_ID_WOB, false, false);

        //Then:
        assertThat(response.getBody().get("accountList").get(0).getId()).isEqualTo(ACCOUNT_ID);
        assertThat(response.getBody().get("accountList").get(1).getId()).isEqualTo(ACCOUNT_ID_1);
        assertThat(response.getBody().get("accountList").get(0).getBalances()).isEqualTo(null);
        assertThat(response.getBody().get("accountList").get(1).getBalances()).isEqualTo(null);
        assertThat(response.getBody().get("accountList").get(0).getLinks()).isEqualTo(new Links());
        assertThat(response.getBody().get("accountList").get(1).getLinks()).isEqualTo(new Links());
    }

    @Test
    public void getAccountDetailsListByConsent_Success_WB() {
        //When:
        ResponseObject<Map<String, List<AccountDetails>>> response = accountService.getAccountDetailsList(CONSENT_ID_WB, true, false);

        //Then:
        assertThat(response.getBody().get("accountList").get(0).getId()).isEqualTo(ACCOUNT_ID);
        assertThat(response.getBody().get("accountList").get(1).getId()).isEqualTo(ACCOUNT_ID_1);
        assertThat(response.getBody().get("accountList").get(0).getBalances()).isEqualTo(getBalancesList());
        assertThat(response.getBody().get("accountList").get(1).getBalances()).isEqualTo(getBalancesList());
        assertThat(response.getBody().get("accountList").get(0).getLinks()).isEqualTo(getAccountDetails(ACCOUNT_ID, IBAN).getLinks());
        assertThat(response.getBody().get("accountList").get(1).getLinks()).isEqualTo(getAccountDetails(ACCOUNT_ID_1, IBAN_1).getLinks());
    }

    @Test
    public void getAccountDetailsListByConsent_Failure_Wrong_Consent() {
        //When:
        ResponseObject<Map<String, List<AccountDetails>>> response = accountService.getAccountDetailsList(WRONG_CONSENT_ID, false, false);

        //Then:
        assertThat(response.hasError()).isEqualTo(true);
        assertThat(response.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
        assertThat(response.getError().getTppMessage().getCode()).isEqualTo(CONSENT_UNKNOWN_403);
    }

    //Get Balances
    @Test
    public void getBalances_Success_Consent_WB() {
        //When:
        ResponseObject<List<Balances>> response = accountService.getBalances(CONSENT_ID_WB, ACCOUNT_ID, false);

        //Then:
        assertThat(response.getBody()).isEqualTo(getBalancesList());
    }

    @Test
    public void getBalances_Failure_Consent_WOB() {
        //When:
        ResponseObject<List<Balances>> response = accountService.getBalances(CONSENT_ID_WOB, ACCOUNT_ID, false);

        //Then:
        assertThat(response.hasError()).isEqualTo(true);
        assertThat(response.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
        assertThat(response.getError().getTppMessage().getCode()).isEqualTo(CONSENT_INVALID);
    }

    @Test
    public void getBalances_Failure_Wrong_Consent() {
        //When:
        ResponseObject<List<Balances>> response = accountService.getBalances(WRONG_CONSENT_ID, ACCOUNT_ID, false);

        //Then:
        assertThat(response.hasError()).isEqualTo(true);
        assertThat(response.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
        assertThat(response.getError().getTppMessage().getCode()).isEqualTo(CONSENT_UNKNOWN_403);
    }

    @Test
    public void getBalances_Failure_Wrong_Account() {
        //When:
        ResponseObject<List<Balances>> response = accountService.getBalances(CONSENT_ID_WB, WRONG_ACCOUNT_ID, false);

        //Then:
        assertThat(response.hasError()).isEqualTo(true);
        assertThat(response.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
        assertThat(response.getError().getTppMessage().getCode()).isEqualTo(RESOURCE_UNKNOWN_404);
    }

    //Internal method test
    @Test
    public void getAccountBalancesByAccountReference_referenceIsNull() {
        // Given:
        AccountReference reference = null;

        //When:
        List<Balances> actualResult = accountService.getAccountBalancesByAccountReference(reference);

        //Then:
        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getAccountBalancesByAccountReference() {
        // Given:
        AccountReference reference = new AccountReference();
        reference.setIban(IBAN);
        reference.setCurrency(CURRENCY);

        List<Balances> expectedResult = getBalancesList();

        //When:
        List<Balances> actualResult = accountService.getAccountBalancesByAccountReference(reference);

        //Then:
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    public void getAccountBalancesByAccountReference_wrongCurrency() {
        // Given:
        AccountReference reference = new AccountReference();
        reference.setIban(IBAN);
        reference.setCurrency(Currency.getInstance("USD"));

        //When:
        List<Balances> actualResult = accountService.getAccountBalancesByAccountReference(reference);

        //Then:
        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getAccountBalancesByAccountReference_ibanIsNull() {
        // Given:
        AccountReference reference = new AccountReference();
        reference.setIban(null);
        reference.setCurrency(Currency.getInstance("USD"));

        //When:
        List<Balances> actualResult = accountService.getAccountBalancesByAccountReference(reference);

        //Then:
        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getAccountBalancesByAccountReference_currencyIsNull() {
        // Given:
        AccountReference reference = new AccountReference();
        reference.setIban(IBAN);
        reference.setCurrency(null);

        //When:
        List<Balances> actualResult = accountService.getAccountBalancesByAccountReference(reference);

        //Then:
        assertThat(actualResult).isEmpty();
    }

    //Get Transaction By TransactionId
    @Test
    public void getAccountReport_ByTransactionId_Success() {
        //When:
        ResponseObject<AccountReport> response = accountService.getAccountReport(CONSENT_ID_WT, ACCOUNT_ID, null, null, TRANSACTION_ID, false, BookingStatus.BOTH, false, false);

        //Then:
        assertThat(response.getError()).isEqualTo(null);
        assertThat(response.getBody().getBooked()[0].getTransactionId()).isEqualTo(getTransaction().getTransactionId());
    }

    @Test
    public void getAccountReport_ByTransactionId_WrongConsent_Failure() {
        //When:
        ResponseObject response = accountService.getAccountReport(WRONG_CONSENT_ID, ACCOUNT_ID, null, null, TRANSACTION_ID, false, BookingStatus.BOTH, false, false);

        //Then:
        assertThat(response.hasError()).isEqualTo(true);
        assertThat(response.getError().getTppMessage().getCode()).isEqualTo(CONSENT_UNKNOWN_403);
    }

    @Test
    public void getAccountReport_ByTransactionId_AccountMismatch_Failure() {
        //When:
        ResponseObject response = accountService.getAccountReport(CONSENT_ID_WOB, WRONG_ACCOUNT_ID, null, null, TRANSACTION_ID, false, BookingStatus.BOTH, false, false);

        //Then:
        assertThat(response.hasError()).isEqualTo(true);
        assertThat(response.getError().getTppMessage().getCode()).isEqualTo(RESOURCE_UNKNOWN_404);
    }

    //Get Transactions By Period
    @Test
    public void getAccountReport_ByPeriod_Success() {
        //When:
        ResponseObject<AccountReport> response = accountService.getAccountReport(CONSENT_ID_WT, ACCOUNT_ID, DATE, DATE, null, false, BookingStatus.BOTH, false, false);

        //Then:
        assertThat(response.getError()).isEqualTo(null);
        assertThat(response.getBody().getBooked()[0].getTransactionId()).isEqualTo(getTransaction().getTransactionId());
    }

    @Test
    public void getAccountReport_ByPeriod_Failure_Wrong_Account() {
        //When:
        ResponseObject response = accountService.getAccountReport(CONSENT_ID_WB, WRONG_ACCOUNT_ID, DATE, DATE, null, false, BookingStatus.BOTH, false, false);

        //Then:
        assertThat(response.hasError()).isEqualTo(true);
        assertThat(response.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
        assertThat(response.getError().getTppMessage().getCode()).isEqualTo(RESOURCE_UNKNOWN_404);
    }

    @Test
    public void getAccountReport_ByPeriod_Failure_Wrong_Consent() {
        //When:
        ResponseObject response = accountService.getAccountReport(WRONG_CONSENT_ID, ACCOUNT_ID, DATE, DATE, null, false, BookingStatus.BOTH, false, false);

        //Then:
        assertThat(response.hasError()).isEqualTo(true);
        assertThat(response.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
        assertThat(response.getError().getTppMessage().getCode()).isEqualTo(CONSENT_UNKNOWN_403);
    }

    //Test Stuff
    private ResponseObject<AccountAccess> getAccessResponse(List<AccountReference> accounts, List<AccountReference> balances, List<AccountReference> transactions, boolean allAccounts, boolean allPsd2) {
        return ResponseObject.<AccountAccess>builder().body(getAccessForMock(accounts, balances, transactions, allAccounts, allPsd2)).build();
    }

    private AccountAccess getAccessForMock(List<AccountReference> accounts, List<AccountReference> balances, List<AccountReference> transactions, boolean allAccounts, boolean allPsd2) {
        return new AccountAccess(accounts, balances, transactions, allAccounts ? AccountAccessType.ALL_ACCOUNTS : null, allPsd2 ? AccountAccessType.ALL_ACCOUNTS : null);
    }

    private AccountReference getAccountReference() {
        AccountDetails details = getAccountDetails(ACCOUNT_ID, IBAN);
        AccountReference rf = new AccountReference();
        rf.setCurrency(details.getCurrency());
        rf.setIban(details.getIban());
        rf.setPan(details.getPan());
        rf.setMaskedPan(details.getMaskedPan());
        rf.setMsisdn(details.getMsisdn());
        rf.setBban(details.getBban());
        return rf;
    }

    private AccountDetails getAccountDetails(String accountId, String iban) {
        AccountDetails details = new AccountDetails(accountId, iban, "zz22", null, null, null, CURRENCY, "David Muller", null, null, null, getBalancesList());
        return details;
    }

    private List<Balances> getBalancesList() {
        Balances balances = new Balances();
        SingleBalance sb = new SingleBalance();
        Amount amount = new Amount();
        amount.setCurrency(CURRENCY);
        amount.setContent("1000");
        sb.setAmount(amount);
        balances.setOpeningBooked(sb);
        return Collections.singletonList(new Balances());
    }

    private SpiAccountDetails getSpiAccountDetails(String accountId, String iban) {
        return new SpiAccountDetails(accountId, iban, "zz22", null, null, null, iban.equals(IBAN) ? CURRENCY : CURRENCY_1, "David Muller", null, null, null, getSpiBalances());
    }

    private List<SpiBalances> getSpiBalances() {
        SpiBalances balances = new SpiBalances();
        SpiAccountBalance sb = new SpiAccountBalance();
        SpiAmount amount = new SpiAmount(CURRENCY, BigDecimal.valueOf(1000));
        sb.setSpiAmount(amount);
        balances.setOpeningBooked(sb);
        return Collections.singletonList(new SpiBalances());
    }

    private Transactions getTransaction() {
        Transactions transaction = new Transactions();
        transaction.setTransactionId(TRANSACTION_ID);
        transaction.setBookingDate(DATE);
        transaction.setValueDate(DATE);
        transaction.setCreditorAccount(getAccountReference());
        Amount amount = new Amount();
        amount.setContent("1000");
        amount.setCurrency(CURRENCY);
        transaction.setAmount(amount);
        return transaction;
    }

    private SpiTransaction getSpiTransaction() {
        Transactions t = getTransaction();
        return new SpiTransaction(t.getTransactionId(), null, null, null, t.getBookingDate(),
            t.getValueDate(), new SpiAmount(t.getAmount().getCurrency(), new BigDecimal(t.getAmount().getContent())), null,
            mapToSpiAccountRef(t.getCreditorAccount()), null, null,
            mapToSpiAccountRef(t.getDebtorAccount()), null, null,
            null, null, null);
    }

    private SpiAccountReference mapToSpiAccountRef(AccountReference reference) {
        return Optional.ofNullable(reference).map(r -> new SpiAccountReference(r.getIban(), r.getBban(), r.getPan(),
            r.getMaskedPan(), r.getMsisdn(), r.getCurrency())).orElse(null);
    }

    private List<AccountReference> getReferences(String iban, String iban1) {
        return Arrays.asList(getReference(iban), getReference(iban1));
    }

    private AccountReference getReference(String iban) {
        AccountReference reference = new AccountReference();
        reference.setIban(iban);
        reference.setCurrency(iban.equals(IBAN) ? CURRENCY : CURRENCY_1);
        return reference;
    }
}
