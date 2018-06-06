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
import de.adorsys.aspsp.xs2a.domain.consent.AccountAccess;
import de.adorsys.aspsp.xs2a.domain.consent.AccountConsent;
import de.adorsys.aspsp.xs2a.domain.consent.ConsentStatus;
import de.adorsys.aspsp.xs2a.exception.MessageCategory;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.spi.domain.account.*;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ais.AccessAccountInfo;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ais.TypeAccess;
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

import static de.adorsys.aspsp.xs2a.domain.MessageCode.CONSENT_INVALID;
import static de.adorsys.aspsp.xs2a.domain.MessageCode.RESOURCE_UNKNOWN_404;
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
    private final String CONSENT_ID = "123456789";
    private final String CONSENT_ID_WB = "111222333";
    private final String CONSENT_ID_WOB = "333222111";
    private final String WRONG_CONSENT_ID = "Wromg consent id";
    private final String TRANSACTION_ID = "0001";
    private final String WRONG_TRANSACTION_ID = "Wrong transaction id";
    private final Date DATE = new Date(123456789L);

    @Autowired
    private AccountService accountService;

    @MockBean(name = "accountSpi")
    private AccountSpi accountSpi;
    @MockBean
    private ConsentService consentService;

    @Before
    public void setUp() {
        when(accountSpi.readAccountDetails(ACCOUNT_ID))
            .thenReturn(getSpiAccountDetails(ACCOUNT_ID, IBAN));
        when(accountSpi.readAccountDetails(ACCOUNT_ID_1))
            .thenReturn(getSpiAccountDetails(ACCOUNT_ID_1, IBAN_1));
        when(accountSpi.readAccountDetails(WRONG_ACCOUNT_ID))
            .thenReturn(null);
        when(accountSpi.readBalances(ACCOUNT_ID))
            .thenReturn(getSpiBalances());
        when(consentService.getIbansFromAccountReference(getAccountReferenceList()))
            .thenReturn(new HashSet<>(Collections.singletonList(IBAN)));
        when(accountSpi.readAccountDetailsByIban(IBAN))
            .thenReturn(Collections.singletonList(getSpiAccountDetails(ACCOUNT_ID, IBAN)));
        when(accountSpi.readTransactionsById(TRANSACTION_ID)).thenReturn(Collections.singletonList(getSpiTransaction()));
        when(accountSpi.readTransactionsByPeriod(ACCOUNT_ID,DATE,DATE,SpiBookingStatus.BOTH))
            .thenReturn(Collections.singletonList(getSpiTransaction()));

        //Check with consent
        when(consentService.checkValidityByConsent(CONSENT_ID_WB, Collections.singletonList(getAccountDetails(ACCOUNT_ID, IBAN)), TypeAccess.ACCOUNT, true))
            .thenReturn(getAccessMap(getAccountDetails(ACCOUNT_ID, IBAN), TypeAccess.ACCOUNT, true));
        when(consentService.checkValidityByConsent(CONSENT_ID_WOB, Collections.singletonList(getAccountDetails(ACCOUNT_ID, IBAN)), TypeAccess.ACCOUNT, true))
            .thenReturn(getAccessMap(getAccountDetails(ACCOUNT_ID, IBAN), TypeAccess.ACCOUNT, false));
        when(consentService.checkValidityByConsent(CONSENT_ID_WOB, Collections.singletonList(null), TypeAccess.ACCOUNT, true))
            .thenReturn(null);
        when(consentService.checkValidityByConsent(WRONG_CONSENT_ID, Collections.singletonList(getAccountDetails(ACCOUNT_ID, IBAN)), TypeAccess.ACCOUNT, true))
            .thenReturn(null);
        when(consentService.checkValidityByConsent(CONSENT_ID_WB, Collections.singletonList(getAccountDetails(ACCOUNT_ID_1, IBAN_1)), TypeAccess.BALANCE, false))
            .thenReturn(getAccessMap(getAccountDetails(ACCOUNT_ID_1, IBAN_1), TypeAccess.BALANCE, true));
        when(consentService.checkValidityByConsent(CONSENT_ID_WB, Collections.singletonList(getAccountDetails(ACCOUNT_ID, IBAN)), TypeAccess.TRANSACTION, false))
            .thenReturn(getAccessMap(getAccountDetails(ACCOUNT_ID, IBAN), TypeAccess.TRANSACTION, false));

        // Is valid for
        when(consentService.isValidAccountByAccess(IBAN, CURRENCY, TypeAccess.BALANCE, getAccessMap(getAccountDetails(ACCOUNT_ID, IBAN), TypeAccess.ACCOUNT, true)))
            .thenReturn(true);
        when(consentService.isValidAccountByAccess(IBAN, CURRENCY, TypeAccess.BALANCE, getAccessMap(getAccountDetails(ACCOUNT_ID, IBAN), TypeAccess.ACCOUNT, false)))
            .thenReturn(false);
        when(consentService.isValidAccountByAccess(IBAN, CURRENCY, TypeAccess.ACCOUNT, getAccessMap(getAccountDetails(ACCOUNT_ID, IBAN), TypeAccess.ACCOUNT, true)))
            .thenReturn(true);
        when(consentService.isValidAccountByAccess(IBAN, CURRENCY, TypeAccess.ACCOUNT, getAccessMap(getAccountDetails(ACCOUNT_ID, IBAN), TypeAccess.ACCOUNT, false)))
            .thenReturn(true);
        when(consentService.isValidAccountByAccess(IBAN_1, CURRENCY, TypeAccess.BALANCE, getAccessMap(getAccountDetails(ACCOUNT_ID_1, IBAN_1), TypeAccess.BALANCE, true)))
            .thenReturn(true);
        when(consentService.isValidAccountByAccess(IBAN, CURRENCY, TypeAccess.TRANSACTION, getAccessMap(getAccountDetails(ACCOUNT_ID, IBAN), TypeAccess.TRANSACTION, true)))
            .thenReturn(true);

        //getAccountsByConsent Success no balances
        when(consentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(ResponseObject.<AccountConsent>builder().body(getAccountConsent()).build());
        when(consentService.getIbanSetFromAccess(getAccountConsent(CONSENT_ID, false, false).getAccess()))
            .thenReturn(new HashSet<>(Collections.singletonList(getAccountDetails(ACCOUNT_ID, IBAN).getIban())));
        when(accountSpi.readAccountDetailsByIbans(new HashSet<>(Collections.singletonList(IBAN))))
            .thenReturn(Arrays.asList(getSpiAccountDetails(ACCOUNT_ID, IBAN)));
        //getAccountsByConsent Success withBalances
        when(consentService.getAccountConsentById(CONSENT_ID_WB))
            .thenReturn(ResponseObject.<AccountConsent>builder().body(getAccountConsent(CONSENT_ID_WB, true, true)).build());
        when(consentService.getIbanSetFromAccess(getAccountConsent(CONSENT_ID_WB, true, true).getAccess()))
            .thenReturn(new HashSet<>(Collections.singletonList(getAccountDetails(ACCOUNT_ID, IBAN).getIban())));
        //getAccountsByConsent Failure concent without Balances
        when(consentService.getAccountConsentById(CONSENT_ID_WOB))
            .thenReturn(ResponseObject.<AccountConsent>builder().body(getAccountConsent(CONSENT_ID_WOB, false, false)).build());
        when(accountSpi.readAccountDetailsByIbans(Collections.emptyList()))
            .thenReturn(Collections.emptyList());
        //getAccountsByConsent Failure wrong consentId
        when(consentService.getAccountConsentById(WRONG_CONSENT_ID))
            .thenReturn(ResponseObject.<AccountConsent>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageCode.RESOURCE_UNKNOWN_404))).build());
    }

    //Get Account By AccountId
    @Test
    public void getAccountDetailsByAccountId_WB_Success() {
        //When:
        ResponseObject<AccountDetails> response = accountService.getAccountDetails(CONSENT_ID_WB, ACCOUNT_ID, true, true);

        //Then:
        assertThat(response.getBody().getId()).isEqualTo(ACCOUNT_ID);
        assertThat(response.getBody().getBalances()).isEqualTo(getBalancesList());
    }

    @Test
    public void getAccountDetailsByAccountId_WB_partialSuccess() {
        //When:
        ResponseObject<AccountDetails> response = accountService.getAccountDetails(CONSENT_ID_WOB, ACCOUNT_ID, true, true);

        //Then:
        assertThat(response.getBody().getId()).isEqualTo(ACCOUNT_ID);
        assertThat(response.getBody().getBalances()).isEqualTo(null);

    }

    @Test
    public void getAccountDetailsByAccountId_Failure_wrongAccount() {
        //When:
        ResponseObject<AccountDetails> response = accountService.getAccountDetails(CONSENT_ID_WB, WRONG_ACCOUNT_ID, true, true);

        //Then:
        assertThat(response.hasError()).isEqualTo(true);
        assertThat(response.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
        assertThat(response.getError().getTppMessage().getCode()).isEqualTo(MessageCode.RESOURCE_UNKNOWN_404);
    }

    @Test
    public void getAccountDetailsByAccountId_Failure_wrongConsent() {
        //When:
        ResponseObject<AccountDetails> response = accountService.getAccountDetails(WRONG_CONSENT_ID, ACCOUNT_ID, true, true);

        //Then:
        assertThat(response.hasError()).isEqualTo(true);
        assertThat(response.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
        assertThat(response.getError().getTppMessage().getCode()).isEqualTo(MessageCode.CONSENT_INVALID);
    }

    //Get AccountsList By Consent
    @Test
    public void getAccountDetailsListByConsent_Success() {
        //When:
        ResponseObject<Map<String, List<AccountDetails>>> response = accountService.getAccountDetailsList(CONSENT_ID, false, false);
        AccountDetails respondedDetails = response.getBody().get("accountList").get(0);

        //Then:
        assertThat(respondedDetails.getId()).isEqualTo(ACCOUNT_ID);
        assertThat(respondedDetails.getBalances()).isEqualTo(null);
        assertThat(respondedDetails.getLinks()).isEqualTo(new Links());
    }

    @Test
    public void getAccountDetailsListByConsent_Success_WB() {
        //When:
        ResponseObject<Map<String, List<AccountDetails>>> response = accountService.getAccountDetailsList(CONSENT_ID_WB, true, false);
        AccountDetails respondedDetails = response.getBody().get("accountList").get(0);

        //Then:
        assertThat(respondedDetails.getId()).isEqualTo(ACCOUNT_ID);
        assertThat(respondedDetails.getLinks()).isEqualTo(getAccountDetails(ACCOUNT_ID, IBAN).getLinks());
    }

    @Test
    public void getAccountDetailsListByConsent_partialSuccess_WB_No_BalancesInConsent() {
        //When:
        ResponseObject<Map<String, List<AccountDetails>>> response = accountService.getAccountDetailsList(CONSENT_ID_WOB, true, false);
        AccountDetails respondedDetails = response.getBody().get("accountList").get(0);

        //Then:
        assertThat(respondedDetails.getId()).isEqualTo(ACCOUNT_ID);
        assertThat(respondedDetails.getBalances()).isEqualTo(null);
        assertThat(respondedDetails.getLinks()).isEqualTo(new Links());
    }

    @Test
    public void getAccountDetailsListByConsent_Failure_Wrong_Consent() {
        //When:
        ResponseObject<Map<String, List<AccountDetails>>> response = accountService.getAccountDetailsList(WRONG_CONSENT_ID, false, false);

        //Then:
        assertThat(response.hasError()).isEqualTo(true);
        assertThat(response.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
        assertThat(response.getError().getTppMessage().getCode()).isEqualTo(MessageCode.CONSENT_INVALID);
    }

    //Get Balances
    @Test
    public void getBalances_Success() {
        //When:
        ResponseObject<List<Balances>> responce = accountService.getBalances(CONSENT_ID_WB, ACCOUNT_ID_1, false);

        //Then:
        assertThat(responce.getBody()).isEqualTo(getBalancesList());
    }

    @Test
    public void getBalances_Failure_Wrong_Consent() {
        //When:
        ResponseObject<List<Balances>> response = accountService.getBalances(WRONG_CONSENT_ID, ACCOUNT_ID, false);

        //Then:
        assertThat(response.hasError()).isEqualTo(true);
        assertThat(response.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
        assertThat(response.getError().getTppMessage().getCode()).isEqualTo(MessageCode.CONSENT_INVALID);
    }

    @Test
    public void getBalances_Failure_Wrong_Account() {
        //When:
        ResponseObject<List<Balances>> responce = accountService.getBalances(CONSENT_ID_WB, WRONG_ACCOUNT_ID, false);

        //Then:
        assertThat(responce.hasError()).isEqualTo(true);
        assertThat(responce.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
        assertThat(responce.getError().getTppMessage().getCode()).isEqualTo(MessageCode.RESOURCE_UNKNOWN_404);
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
        ResponseObject<AccountReport> response = accountService.getAccountReport(CONSENT_ID_WB, ACCOUNT_ID, null, null, TRANSACTION_ID, false, BookingStatus.BOTH, false, false);

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
        assertThat(response.getError().getTppMessage().getCode()).isEqualTo(CONSENT_INVALID);
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
        ResponseObject<AccountReport> response = accountService.getAccountReport(CONSENT_ID_WB, ACCOUNT_ID, DATE, DATE, null, false, BookingStatus.BOTH, false, false);

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
        assertThat(response.getError().getTppMessage().getCode()).isEqualTo(MessageCode.RESOURCE_UNKNOWN_404);
    }

    @Test
    public void getAccountReport_ByPeriod_Failure_Wrong_Consent() {
        //When:
        ResponseObject response = accountService.getAccountReport(WRONG_CONSENT_ID, ACCOUNT_ID, DATE, DATE, null, false, BookingStatus.BOTH, false, false);

        //Then:
        assertThat(response.hasError()).isEqualTo(true);
        assertThat(response.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
        assertThat(response.getError().getTppMessage().getCode()).isEqualTo(MessageCode.CONSENT_INVALID);
    }

    //Test Stuff
    private Map<String, Set<AccessAccountInfo>> getAccessMap(AccountDetails accountDetails, TypeAccess typeAccess, boolean withBalance) {
        Set<AccessAccountInfo> set = new HashSet<>();
        set.add(new AccessAccountInfo(accountDetails.getCurrency().getCurrencyCode(), typeAccess));
        if (withBalance) {
            set.add(new AccessAccountInfo(accountDetails.getCurrency().getCurrencyCode(), TypeAccess.BALANCE));
        }
        Map<String, Set<AccessAccountInfo>> map = new HashMap<>();
        map.put(accountDetails.getIban(), set);
        return map;
    }

    private AccountConsent getAccountConsent(String consentId, boolean withBalance, boolean withTransactions) {
        return new AccountConsent(consentId,
            new AccountAccess(
                consentId.equals(WRONG_CONSENT_ID)
                    ? new ArrayList<AccountReference>()
                    : getAccountReferenceList(),
                withBalance
                    ? getAccountReferenceList()
                    : new ArrayList<AccountReference>(),
                withTransactions
                    ? getAccountReferenceList()
                    : new ArrayList<AccountReference>(),
                null, null),
            false, DATE, 4, null, ConsentStatus.VALID, false, true);
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

    private List<AccountReference> getAccountReferenceList() {
        List<AccountReference> list = new ArrayList<>();
        list.add(getAccountReference());

        return list;
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
        return new SpiAccountDetails(accountId, iban, "zz22", null, null, null, CURRENCY, "David Muller", null, null, null, getSpiBalances());
    }

    private List<SpiBalances> getSpiBalances() {
        SpiBalances balances = new SpiBalances();
        SpiAccountBalance sb = new SpiAccountBalance();
        SpiAmount amount = new SpiAmount(CURRENCY, BigDecimal.valueOf(1000));
        sb.setSpiAmount(amount);
        balances.setOpeningBooked(sb);
        return Collections.singletonList(new SpiBalances());
    }

    private AccountReport getAccountReportDummy() {

        return new AccountReport(new Transactions[]{getTransaction()}, new Transactions[]{});
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
        return Optional.ofNullable(reference).map(r->new SpiAccountReference(r.getIban(), r.getBban(), r.getPan(),
            r.getMaskedPan(), r.getMsisdn(), r.getCurrency())).orElse(null);
    }

    private AccountConsent getAccountConsent() {
        return new AccountConsent(
            CONSENT_ID,
            getAccountAccess(),
            false,
            DATE,
            4,
            null,
            ConsentStatus.VALID,
            false,
            false);
    }

    private AccountAccess getAccountAccess() {
        return new AccountAccess(
            getAccountReferenceList(),
            null,
            null,
            null,
            null);
    }
}
