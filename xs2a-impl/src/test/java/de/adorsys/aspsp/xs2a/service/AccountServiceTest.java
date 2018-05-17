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
import de.adorsys.aspsp.xs2a.domain.consent.AccountAccessType;
import de.adorsys.aspsp.xs2a.domain.consent.AccountConsent;
import de.adorsys.aspsp.xs2a.domain.consent.ConsentStatus;
import de.adorsys.aspsp.xs2a.service.mapper.AccountMapper;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountBalance;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalances;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.aspsp.xs2a.web.AccountController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.Charset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountServiceTest {
    private final String ACCOUNT_ID = "33333-999999999";
    private final String IBAN = "DE123456789";
    private final Currency CURRENCY = Currency.getInstance("EUR");
    private final String TRANSACTION_ID = "Id-0001";
    private final Currency usd = Currency.getInstance("USD");
    private final String ACCOUNT_DETAILS_SOURCE = "/json/AccountDetails.json";
    private final String SPI_ACCOUNT_DETAILS_SOURCE = "/json/SpiAccountDetails.json";
    private final int maxNumberOfCharInTransactionJson = 1000;
    private final Charset UTF_8 = Charset.forName("utf-8");
    private final String CONSENT_ID = "123456789";
    private final String WRONG_CONSENT_ID = "Wromg consent id";
    private final Date DATE = new Date(123456789L);

    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountMapper accountMapper;

    @MockBean(name = "accountSpi")
    private AccountSpi accountSpi;
    @MockBean
    private ConsentService consentService;

    @Before
    public void setUp() {
        when(accountSpi.readAccountDetails(ACCOUNT_ID)).thenReturn(getSpiAccountDetails());
        when(accountSpi.readAccountDetailsByIbans(new HashSet<>(Collections.singletonList(getAccountDetails().getIban())))).thenReturn(Collections.singletonList(getSpiAccountDetails()));
        when(accountSpi.readBalances(ACCOUNT_ID)).thenReturn(getSpiBalances());
        when(consentService.getAccountConsentsById(CONSENT_ID)).thenReturn(ResponseObject.<AccountConsent>builder().body(getAccountConsent(CONSENT_ID)).build());
        when(consentService.getIbanSetFromAccess(getAccountConsent(CONSENT_ID).getAccess())).thenReturn(new HashSet<>(Collections.singletonList(getAccountDetails().getIban())));

       /* when(accountSpi.readTransactionsByPeriod(any(), any(), any()))
            .thenReturn(getTransactionList());
        when(accountSpi.readBalances(any()))
            .thenReturn(getBalances());
        when(accountSpi.readTransactionsById(any(), any()))
            .thenReturn(getTransactionList());
        when(accountSpi.readAccountDetailsByIban(anyString()))
            .thenReturn(Collections.singletonList(createSpiAccountDetails()));
        when(consentService.getAccountConsentsById(CONSENT_ID))
            .thenReturn(ResponseObject.<AccountConsent>builder().body(getAccountConsent(CONSENT_ID)).build());
        when(consentService.getAccountConsentsById(WRONG_CONSENT_ID))
            .thenReturn(ResponseObject.<AccountConsent>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageCode.CONSENT_UNKNOWN_403))).build());
        when(consentService.getIbanSetFromAccess(getAccountConsent(CONSENT_ID).getAccess()))
            .thenReturn(new HashSet<String>(Collections.singletonList(getAccountReference().getIban())));*/
    }

    @Test
    public void getAccountDetailsByAccountId_WB_Success() {
        //When:
        ResponseObject<AccountDetails> response = accountService.getAccountDetails(ACCOUNT_ID, true, true);

        //Then:
        assertThat(response.getBody().getId()).isEqualTo(ACCOUNT_ID);
    }

    @Test
    public void getAccountDetailsListByConsent_Success() {
        //When:
        ResponseObject<Map<String, List<AccountDetails>>> response = accountService.getAccountDetailsList(CONSENT_ID, true, false);
        AccountDetails respondedDetails = response.getBody().get("accountList").get(0);

        //Then:
        assertThat(respondedDetails.getId()).isEqualTo(ACCOUNT_ID);
        assertThat(respondedDetails.getLinks()).isEqualTo(getAccountDetails().getLinks());
    }

    @Test
    public void getBalances() {
        //When:
        ResponseObject response = accountService.getBalances(ACCOUNT_ID, false);

        //Then:
        assertThat(response.getBody()).isEqualTo(getBalancesList());
    }

    @Test
    public void getAccountReport() {
        //When:
        ResponseObject response = accountService.getAccountReport(ACCOUNT_ID, DATE, DATE, null, false, "both", false, false);

        //Then:
        assertThat(response.getBody()).isEqualTo(getAccountReportDummy());
    }

/*
    @Test //TODO Global test review
    public void getAccountDetails_withBalance() throws IOException {
        //Given:
        boolean withBalance = true;
        boolean psuInvolved = true;
        AccountDetails expectedResult = new Gson().fromJson(IOUtils.resourceToString(ACCOUNT_DETAILS_SOURCE, UTF_8), AccountDetails.class);

        //When:
        ResponseObject<AccountDetails> result = accountService.getAccountDetails(ACCOUNT_ID, withBalance, psuInvolved);

        //Then:
        AccountDetails actualResult = result.getBody();
        assertThat(actualResult.getAccountType()).isEqualTo(expectedResult.getAccountType());
        assertThat(actualResult.getId()).isEqualTo(expectedResult.getId());
        assertThat(actualResult.getIban()).isEqualTo(expectedResult.getIban());
        assertThat(actualResult.getCurrency()).isEqualTo(expectedResult.getCurrency());
        assertThat(actualResult.getName()).isEqualTo(expectedResult.getName());
        assertThat(actualResult.getAccountType()).isEqualTo(expectedResult.getAccountType());
        assertThat(actualResult.getBic()).isEqualTo(expectedResult.getBic());
    }

    @Test
    public void getAccountDetails_withBalanceNoPsuInvolved() {
        //Given:
        boolean withBalance = true;
        boolean psuInvolved = false;
        checkAccountResults(withBalance, psuInvolved);
    }

    @Test
    public void getAccountDetails_noBalanceNoPsuInvolved() {
        //Given:
        boolean withBalance = true;
        boolean psuInvolved = false;
        checkAccountResults(withBalance, psuInvolved);
    }

    @Test
    public void getBalances_noPsuInvolved() {
        //Given:
        boolean psuInvolved = false;
        checkBalanceResults(ACCOUNT_ID, psuInvolved);
    }

    @Test
    public void getBalances_withPsuInvolved() {
        //Given:
        boolean psuInvolved = true;
        checkBalanceResults(ACCOUNT_ID, psuInvolved);
    }

    @Test
    public void getTransactions_onlyTransaction() {
        //Given:
        boolean psuInvolved = false;
        String accountId = "11111-999999999";
        checkTransactionResultsByTransactionId(accountId, TRANSACTION_ID, psuInvolved);
    }

    @Test
    public void getTransactions_onlyByPeriod() {
        //Given:
        Date dateFrom = new Date();
        Date dateTo = new Date();
        boolean psuInvolved = false;
        String accountId = "11111-999999999";
        checkTransactionResultsByPeriod(accountId, dateFrom, dateTo, psuInvolved);
    }

    @Test
    public void getTransactions_jsonBiggerLimitSize_returnDownloadLink() {
        //Given:
        Date dateFrom = getDateFromDateString("2015-12-12");
        Date dateTo = getDateFromDateString("2018-12-12");
        boolean psuInvolved = false;
        AccountReport expectedResult = accountService.getAccountReportWithDownloadLink(ACCOUNT_ID);

        //When:
        AccountReport actualResult = accountService.getAccountReport(ACCOUNT_ID, dateFrom, dateTo, null, psuInvolved, "both", true, false).getBody();

        //Then:
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    public void getTransactions_withPeriodAndTransactionIdNoPsuInvolved() {
        //Given:
        Date dateFrom = new Date();
        Date dateTo = new Date();
        boolean psuInvolved = false;
        String accountId = "11111-999999999";

        checkTransactionResultsByPeriod(accountId, dateFrom, dateTo, psuInvolved);
        checkTransactionResultsByTransactionId(accountId, TRANSACTION_ID, psuInvolved);
    }

    private void checkTransactionResultsByPeriod(String accountId, Date dateFrom, Date dateTo, boolean psuInvolved) {
        //Given:
        AccountReport expectedReport = getAccountReport(accountId);
        //When:
        AccountReport actualResult = accountService.getAccountReport(accountId, dateFrom, dateTo, null, psuInvolved, "both", false, false).getBody();

        //Then:
        assertThat(actualResult).isEqualTo(expectedReport);
        assertThat(actualResult.getLinks()).isEqualTo(expectedReport.getLinks());
    }

    private void checkTransactionResultsByTransactionId(String accountId, String transactionId, boolean psuInvolved) {
        //Given:
        AccountReport expectedReport = getAccountReport(accountId);

        //When:
        AccountReport actualResult = accountService.getAccountReport(accountId, new Date(), new Date(), transactionId, psuInvolved, "both", false, false).getBody();

        //Then:
        assertThat(actualResult).isEqualTo(expectedReport);
    }

    private void checkBalanceResults(String accountId, boolean psuInvolved) {
        //Given:
        List<Balances> expectedResult = accountMapper.mapFromSpiBalancesList(getBalances());
        //When:
        List<Balances> actualResult = accountService.getBalances(accountId, psuInvolved).getBody();
        //Then:
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    private void checkAccountResults(boolean withBalance, boolean psuInvolved) {
        List<SpiAccountDetails> list = accountSpi.readAccountDetailsByIban("id");
        List<AccountDetails> accountDetails = new ArrayList<>();
        for (SpiAccountDetails s : list) {
            accountDetails.add(accountMapper.mapFromSpiAccountDetails(s));
        }

        List<AccountDetails> expectedResult = accountsToAccountDetailsList(accountDetails);

        //When:
        List<AccountDetails> actualResponse = accountService.getAccountDetailsList("id", withBalance, psuInvolved).getBody().get("accountList");

        //Then:
        assertThat(expectedResult).isEqualTo(actualResponse);
    }

    private List<AccountDetails> accountsToAccountDetailsList(List<AccountDetails> accountDetails) {
        String urlToAccount = linkTo(AccountController.class).toString();

        accountDetails
            .forEach(account -> account.setBalanceAndTransactionLinksByDefault(urlToAccount));
        return accountDetails;

    }

    private List<SpiTransaction> getTransactionList() {
        List<SpiTransaction> testData = new ArrayList<>();
        testData.add(getBookedTransaction());
        testData.add(getPendingTransaction());

        return testData;
    }

    private static Date getDateFromDateString(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(ApiDateConstants.DATE_PATTERN);
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }

    private SpiTransaction getBookedTransaction() {
        Currency usd = Currency.getInstance("USD");
        //transaction 1:
        Date bookingDate = getDateFromDateString("2017-11-07");
        Date valueDate = getDateFromDateString("2018-20-08");
        SpiAmount spiAmount = new SpiAmount(usd, "15000");
        SpiAccountReference creditorAccount = new SpiAccountReference("cAccIban", "cAccBban", "cAccPan", "cAccMaskedPan", "cAccMsisdn", usd);
        SpiAccountReference debtorAccount = new SpiAccountReference("dAccIban", "dAccBban", "dAccPan", "dAccMaskedPan", "dAccMsisdn", usd);

        return new SpiTransaction("Id-0001", "id-0001", "m-0001", "c-0001", bookingDate, valueDate, spiAmount, "Creditor1", creditorAccount, "ultimateCreditor1", "DebitorName", debtorAccount, "UltimateDebtor1", "SomeInformation", "SomeStruturedInformation", "PurposeCode123", "TransactionCode");
    }

    private SpiTransaction getPendingTransaction() {
        Currency usd = Currency.getInstance("USD");
        //transaction 1:
        Date valueDate = getDateFromDateString("2018-20-08");
        SpiAmount spiAmount = new SpiAmount(usd, "15000");
        SpiAccountReference creditorAccount = new SpiAccountReference("cAccIban", "cAccBban", "cAccPan", "cAccMaskedPan", "cAccMsisdn", usd);
        SpiAccountReference debtorAccount = new SpiAccountReference("dAccIban", "dAccBban", "dAccPan", "dAccMaskedPan", "dAccMsisdn", usd);

        return new SpiTransaction("Id-0001", "id-0001", "m-0001", "c-0001", null, valueDate, spiAmount, "Creditor1", creditorAccount, "ultimateCreditor1", "DebitorName", debtorAccount, "UltimateDebtor1", "SomeInformation", "SomeStruturedInformation", "PurposeCode123", "TransactionCode");
    }

    private List<SpiBalances> getBalances() {
        SpiAccountBalance spiAccountBalance = getSpiAccountBalance("1000", "2016-12-12", "2018-23-02");

        List<SpiBalances> spiBalances = new ArrayList<SpiBalances>();
        for (SpiBalances spiBalancesItem : spiBalances) {
            spiBalancesItem.setInterimAvailable(spiAccountBalance);
        }

        return spiBalances;
    }

    private SpiAccountBalance getSpiAccountBalance(String amount, String date, String lastActionDate) {
        SpiAccountBalance acb = new SpiAccountBalance();
        acb.setSpiAmount(new SpiAmount(usd, amount));
        acb.setDate(getDateFromDateString(date));
        acb.setLastActionDateTime(getDateFromDateString(lastActionDate));

        return acb;
    }

    private AccountReport getAccountReport(String accountId) {
        Optional<AccountReport> aR = accountMapper.mapFromSpiAccountReport(getTransactionList());
        AccountReport accountReport;
        accountReport = aR.orElseGet(() -> new AccountReport(new Transactions[]{}, new Transactions[]{}, new Links()));
        String jsonReport = null;

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            jsonReport = objectMapper.writeValueAsString(accountReport);
        } catch (JsonProcessingException e) {
            System.out.println("Error converting object {} to json" + accountReport.toString());
        }

        if (jsonReport.length() > maxNumberOfCharInTransactionJson) {
            String urlToDownload = linkTo(AccountController.class).slash(accountId).slash("transactions/download").toString();
            Links downloadLink = new Links();
            downloadLink.setDownload(urlToDownload);
            return new AccountReport(null, null, downloadLink);
        } else {
            return accountReport;
        }
    }

    private SpiAccountDetails createSpiAccountDetails() throws IOException {
        return new Gson().fromJson(IOUtils.resourceToString(SPI_ACCOUNT_DETAILS_SOURCE, UTF_8), SpiAccountDetails.class);
    }*/

    private AccountConsent getAccountConsent(String consentId) {
        return new AccountConsent(consentId,
            new AccountAccess(new AccountReference[]{getAccountReference()}, new AccountReference[]{getAccountReference()}, new AccountReference[]{getAccountReference()}, AccountAccessType.ALL_ACCOUNTS, AccountAccessType.ALL_ACCOUNTS),
            false, DATE, 4, null, TransactionStatus.ACCP, ConsentStatus.VALID, true, true);
    }

    private AccountReference getAccountReference() {
        AccountReference rf = new AccountReference();
        rf.setCurrency(CURRENCY);
        rf.setIban(getAccountDetails().getIban());
        rf.setPan(getAccountDetails().getPan());
        rf.setMaskedPan(getAccountDetails().getMaskedPan());
        rf.setMsisdn(getAccountDetails().getMsisdn());
        rf.setBban(getAccountDetails().getBban());
        return new AccountReference();
    }

    private AccountDetails getAccountDetails() {
        AccountDetails details = new AccountDetails(ACCOUNT_ID, IBAN, "zz22", null, null, null, CURRENCY, "David Muller", null, null, null, getBalancesList());
        details.setBalanceAndTransactionLinksByDefault(linkTo(AccountController.class).toUriComponentsBuilder().build().getPath());
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

    private SpiAccountDetails getSpiAccountDetails() {
        return new SpiAccountDetails(ACCOUNT_ID, IBAN, "zz22", null, null, null, CURRENCY, "David Muller", null, null, null, getSpiBalances());
    }

    private List<SpiBalances> getSpiBalances() {
        SpiBalances balances = new SpiBalances();
        SpiAccountBalance sb = new SpiAccountBalance();
        SpiAmount amount = new SpiAmount(CURRENCY, "1000");
        sb.setSpiAmount(amount);
        balances.setOpeningBooked(sb);
        return Collections.singletonList(new SpiBalances());
    }

    private AccountReport getAccountReportDummy() {
        Links lnk = new Links();
        lnk.setViewAccount("http://localhost/api/v1/accounts/33333-999999999");
        AccountReport report = new AccountReport(new Transactions[]{}, new Transactions[]{}, lnk);
        return report;
    }
}
