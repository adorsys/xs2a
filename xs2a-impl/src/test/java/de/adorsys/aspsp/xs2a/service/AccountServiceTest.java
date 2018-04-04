package de.adorsys.aspsp.xs2a.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.spi.domain.account.*;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.aspsp.xs2a.web.AccountController;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountServiceTest {
    private final String ACCOUNT_ID = "11111-999999999";
    private final String TRANSACTION_ID = "Id-0001";
    private final Currency usd = Currency.getInstance("USD");
    private final String ACCOUNT_DETAILS_SOURCE = "/json/SpiAccountDetails.json";
    private final int maxNumberOfCharInTransactionJson = 1000;
    private final Charset UTF_8 = Charset.forName("utf-8");

    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountMapper accountMapper;

    @MockBean(name = "accountSpi")
    private AccountSpi accountSpi;

    @Before
    public void setUp() throws IOException {
        when(accountSpi.readTransactionsByPeriod(any(), any(), any(), anyBoolean())).thenReturn(getTransactionList());
        when(accountSpi.readBalances(any(), anyBoolean())).thenReturn(getBalances());
        when(accountSpi.readTransactionsById(any(), any(), anyBoolean())).thenReturn(getTransactionList());
        when(accountSpi.readAccountDetails(any(), eq(true), anyBoolean())).thenReturn(createSpiAccountDeatails());
    }

    @Test
    public void getAccountDetails_withBalanace() throws IOException {
        //Given:
        boolean withBalance = true;
        boolean psuInvolved = true;
        AccountDetails expectedResult = new Gson().fromJson(IOUtils.resourceToString(ACCOUNT_DETAILS_SOURCE, UTF_8), AccountDetails.class);

        //When:
        ResponseObject<AccountDetails> result = accountService.getAccountDetails(ACCOUNT_ID, withBalance, psuInvolved);


        //Then:
        assertThat(result.getData()).isEqualTo(expectedResult);
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldFail_getBalance_NoAccountId() {
        //Given:
        String accountId = "";
        boolean psuInvoilved = true;
        //When:
        accountService.getBalances(accountId, psuInvoilved);
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldFail_getTransactions_noTransactionIdNoPsuInvolved() {
        //Given:
        String transactionId = "";
        boolean psuInvolved = false;
        Date dateFrom = null;
        Date dateTo = null;
        //When
        accountService.getAccountReport(ACCOUNT_ID, dateFrom, dateTo, transactionId, psuInvolved, "both", false, false);
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldFail_getTransactions_noAccountId() {
        //Given:
        String accountId = "";
        String transactionId = "";
        Date dateFrom = null;
        Date dateTo = null;
        boolean psuInvolved = false;

        accountService.getAccountReport(accountId, dateFrom, dateTo, transactionId, psuInvolved, "both", false, false);
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
        Date dateFrom = addMonth(new Date(), -12);
        Date dateTo = addMonth(dateFrom, 12);
        boolean psuInvolved = false;
        AccountReport expectedResult = accountService.getAccountReportWithDownloadLink(ACCOUNT_ID);

        //When:
        AccountReport actualResult = accountService.getAccountReport(ACCOUNT_ID, dateFrom, dateTo, null, psuInvolved, "both", false, false).getData();

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
        AccountReport actualResult = accountService.getAccountReport(accountId, dateFrom, dateTo, null, psuInvolved, "both", false, false).getData();

        //Then:
        assertThat(actualResult).isEqualTo(expectedReport);
        assertThat(actualResult.get_links()).isEqualTo(expectedReport.get_links());
    }

    private void checkTransactionResultsByTransactionId(String accountId, String transactionId, boolean psuInvolved) {
        //Given:
        AccountReport expectedReport = getAccountReport(accountId);

        //When:
        AccountReport actualResult = accountService.getAccountReport(accountId, new Date(), new Date(), transactionId, psuInvolved, "both", false, false).getData();

        //Then:
        assertThat(actualResult).isEqualTo(expectedReport);
    }

    private void checkBalanceResults(String accountId, boolean psuInvolved) {
        //Given:
        Balances expectedResult = accountMapper.mapSpiBalances(getBalances());
        List<Balances> expectedResult = accountMapper.mapListSpiBalances(getBalances());
        //When:
        Balances actualResult = (Balances) accountService.getBalances(accountId, psuInvolved).getData();
        List<Balances> actualResult = accountService.getBalances(accountId, psuInvolved);
        //Then:
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    private void checkAccountResults(boolean withBalance, boolean psuInvolved) {
        List<SpiAccountDetails> list = accountSpi.readAccounts(withBalance, psuInvolved);
        List<AccountDetails> accountDetails = new ArrayList<>();
        for (SpiAccountDetails s : list) {
            accountDetails.add(accountMapper.mapSpiAccountDetailsToXs2aAccountDetails(s));
        }

        List<AccountDetails> expectedResult = accountsToAccountDetailsList(accountDetails);

        //When:
        List<AccountDetails> actualResponse = accountService.getAccountDetailsList(withBalance, psuInvolved).getData();

        //Then:
        assertThat(expectedResult).isEqualTo(actualResponse);
    }

    private List<AccountDetails> accountsToAccountDetailsList(List<AccountDetails> accountDetails) {
        String urlToAccount = linkTo(AccountController.class).toString();

        accountDetails
        .forEach(account -> account.setBalanceAndTransactionLinksByDefault(urlToAccount));
        return accountDetails;

    }

    private static Date addMonth(Date dateFrom, int months) {
        LocalDateTime localDateTimeFrom = LocalDateTime.ofInstant(dateFrom.toInstant(), ZoneId.systemDefault());
        LocalDateTime localDateTimeTo = localDateTimeFrom.plusMonths(months);
        return Date.from(localDateTimeTo.atZone(ZoneId.systemDefault()).toInstant());
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
        SpiAccountReference creditorAccount = new SpiAccountReference("11111-999999999", "cAccIban", "cAccBban", "cAccPan", "cAccMaskedPan", "cAccMsisdn", usd);
        SpiAccountReference debtorAccount = new SpiAccountReference("dAccId", "dAccIban", "dAccBban", "dAccPan", "dAccMaskedPan", "dAccMsisdn", usd);

        return new SpiTransaction("Id-0001", "id-0001", "m-0001", "c-0001", bookingDate, valueDate, spiAmount, "Creditor1", creditorAccount, "ultimateCreditor1", "DebitorName", debtorAccount, "UltimateDebtor1", "SomeInformation", "SomeStruturedInformation", "PurposeCode123", "TransactionCode");
    }

    private SpiTransaction getPendingTransaction() {
        Currency usd = Currency.getInstance("USD");
        //transaction 1:
        Date valueDate = getDateFromDateString("2018-20-08");
        SpiAmount spiAmount = new SpiAmount(usd, "15000");
        SpiAccountReference creditorAccount = new SpiAccountReference("11111-999999999", "cAccIban", "cAccBban", "cAccPan", "cAccMaskedPan", "cAccMsisdn", usd);
        SpiAccountReference debtorAccount = new SpiAccountReference("dAccId", "dAccIban", "dAccBban", "dAccPan", "dAccMaskedPan", "dAccMsisdn", usd);

        return new SpiTransaction("Id-0001", "id-0001", "m-0001", "c-0001", null, valueDate, spiAmount, "Creditor1", creditorAccount, "ultimateCreditor1", "DebitorName", debtorAccount, "UltimateDebtor1", "SomeInformation", "SomeStruturedInformation", "PurposeCode123", "TransactionCode");
    }

    private List<SpiBalances> getBalances() {
        SpiAccountBalance spiAccountBalance = getSpiAccountBalance("1000", "2016-12-12", "2018-23-02");

        List<SpiBalances> spiBalances = new ArrayList<SpiBalances>();
        for(SpiBalances spiBalancesItem: spiBalances){
            spiBalancesItem.setInterimAvailable(spiAccountBalance);
        }

        return spiBalances;
    }

    private SpiAccountBalance getSpiAccountBalance(String ammount, String date, String lastActionDate) {
        SpiAccountBalance acb = new SpiAccountBalance();
        acb.setSpiAmount(new SpiAmount(usd, ammount));
        acb.setDate(getDateFromDateString(date));
        acb.setLastActionDateTime(getDateFromDateString(lastActionDate));

        return acb;
    }

    private AccountReport getAccountReport(String accountId) {
        AccountReport accountReport = accountMapper.mapAccountReport(getTransactionList());
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

    private SpiAccountDetails createSpiAccountDeatails() throws IOException {
        return new Gson().fromJson(IOUtils.resourceToString(ACCOUNT_DETAILS_SOURCE, UTF_8), SpiAccountDetails.class);
    }
}
