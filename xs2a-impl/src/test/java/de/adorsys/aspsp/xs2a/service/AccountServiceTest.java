package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.AccountDetails;
import de.adorsys.aspsp.xs2a.domain.AccountReport;
import de.adorsys.aspsp.xs2a.domain.Balances;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.aspsp.xs2a.web.AccountController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountServiceTest {
    private final String ACCOUNT_ID = "33333-999999999";
    private final String TRANSACTION_ID = "1234578";

    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountSpi accountSpi;

    @Test
    public void getAccountDetails_withBalanceNoPsuInvolved() throws IOException {
        //Given:
        boolean withBalance = true;
        boolean psuInvolved = false;
        checkAccountResults(withBalance, psuInvolved);
    }

    @Test
    public void getAccountDetails_noBalanceNoPsuInvolved() throws IOException {
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

    @Test(expected = ConstraintViolationException.class)
    public void shouldFail_getBalances_emptyAccountWithBalanceAndPsuInvolved() {
        //Given:
        String accountId = "";
        boolean psuInvolved = true;
        checkBalanceResults(accountId, psuInvolved);
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
        AccountReport actualResult = accountService.getAccountReport(ACCOUNT_ID, dateFrom, dateTo, null, psuInvolved);

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

    @Test(expected = ConstraintViolationException.class)
    public void shouldFail_getTransactionsNoAccountId() {
        //Given:
        boolean psuInvolved = false;
        String accountId = "";
        checkTransactionResultsByTransactionId(accountId, TRANSACTION_ID, psuInvolved);
    }

    private void checkTransactionResultsByPeriod(String accountId, Date dateFrom, Date dateTo, boolean psuInvolved) {
        //Given:
        //TODO #58 get rid of dependencies in Unit Test
        AccountReport expectedResult = accountSpi.readTransactionsByPeriod(accountId, dateFrom, dateTo, psuInvolved);
        String link = linkTo(AccountController.class).slash(accountId).toString();
        expectedResult.get_links().setViewAccount(link);

        //When:
        AccountReport actualResult = accountService.getAccountReport(accountId, dateFrom, dateTo, null, psuInvolved);

        //Then:
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    private void checkTransactionResultsByTransactionId(String accountId, String transactionId, boolean psuInvolved) {
        //Given:
        //TODO #58 get rid of dependencies in Unit Test
        AccountReport expectedResult = accountSpi.readTransactionsById(accountId, transactionId, psuInvolved);
        String link = linkTo(AccountController.class).slash(accountId).toString();
        expectedResult.get_links().setViewAccount(link);

        //When:
        AccountReport actualResult = accountService.getAccountReport(accountId, new Date(), new Date(), transactionId, psuInvolved);


        //Then:
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    private void checkBalanceResults(String accountId, boolean psuInvolved) {
        //Given:
        //TODO #58 get rid of dependencies in Unit Test
        Balances expectedResult = accountSpi.readBalances(accountId, psuInvolved);

        //When:
        Balances actualResult = accountService.getBalances(accountId, psuInvolved);

        //Then:
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    private void checkAccountResults(boolean withBalance, boolean psuInvolved) {
        //TODO #58 get rid of dependencies in Unit Test
        List<AccountDetails> accountDetails = accountSpi.readAccounts(withBalance, psuInvolved);
        List<AccountDetails> expectedResult = accountsToAccountDetailsList(accountDetails);

        //When:
        List<AccountDetails> actualResponse = accountService.getAccountDetailsList(withBalance, psuInvolved);

        //Then:
        assertThat(expectedResult).isEqualTo(actualResponse);
    }

    private List<AccountDetails> accountsToAccountDetailsList(List<AccountDetails> accountDetails) {
        String urlToAccount = linkTo(AccountController.class).toString();

        accountDetails
        .forEach(account -> account.setBalanceAndTransactionLinksDyDefault(urlToAccount));
        return accountDetails;

    }

    private static Date addMonth(Date dateFrom, int months) {
        LocalDateTime localDateTimeFrom = LocalDateTime.ofInstant(dateFrom.toInstant(), ZoneId.systemDefault());
        LocalDateTime localDateTimeTo = localDateTimeFrom.plusMonths(months);
        return Date.from(localDateTimeTo.atZone(ZoneId.systemDefault()).toInstant());
    }
}
