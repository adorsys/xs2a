package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.spi.domain.Account;
import de.adorsys.aspsp.xs2a.spi.domain.AccountReport;
import de.adorsys.aspsp.xs2a.spi.domain.Balances;
import de.adorsys.aspsp.xs2a.spi.domain.ais.AccountResponse;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.aspsp.xs2a.web.AccountController;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountServiceTest {
	private final String ACCOUNT_ID = "33333-999999999";
	private final String TRANSACTION_ID = "1234578";

	@Autowired
	AccountService accountService;
	@Autowired
	AccountSpi accountSpi;

	@Test
	public void getAccountResponses_withBalanceNoPsuInvolved() throws IOException {
		//Given:
		boolean withBalance = true;
		boolean psuInvolved = false;

		checkAccountResults(withBalance, psuInvolved);
	}

	@Test
	public void getAccountResponses_noBalanceNoPsuInvolved() throws IOException {
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
	@Ignore
	public void shouldFail_getBalances_emptyAccountWithBalanceAndPsuInvolved() {
		//Given:
		String accountId = "";
		boolean psuInvolved = true;
		//todo we need make error check for empty accountId
		checkBalanceResults(accountId, psuInvolved);
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
		Date dateFrom = new Date();
		Date dateTo = new Date();
		boolean psuInvolved = false;
		AccountReport expectedResult = accountService.getAccountReportWithDownloadLink(ACCOUNT_ID);

		//When:
		AccountReport actualResult = accountService.getAccountReport(ACCOUNT_ID, dateFrom, dateTo, null, psuInvolved);

		//Then:
		assertThat(actualResult).isEqualTo(expectedResult);
	}


	@Test
	public void getTransactions_onlyTransaction() {
		//Given:
		boolean psuInvolved = false;

		checkTransactionResultsByTransactionId(ACCOUNT_ID, TRANSACTION_ID, psuInvolved);
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

	@Test
	@Ignore
	public void shouldFail_getTransactions_noAccountId() {
		//Given:
		boolean psuInvolved = false;

		//todo we need make error check for empty transactionId and empty period
		checkTransactionResultsByTransactionId(ACCOUNT_ID, TRANSACTION_ID, psuInvolved);
	}

	@Test
	@Ignore
	public void shouldFail_getTransactions_noTransactionIdNoPsuInvolved() {
		//Given:
		boolean psuInvolved = false;

		//todo we need make error check for empty transactionId and empty period
		checkTransactionResultsByTransactionId(ACCOUNT_ID, TRANSACTION_ID, psuInvolved);
	}

	private void checkTransactionResultsByPeriod(String accountId, Date dateFrom, Date dateTo, boolean psuInvolved) {
		//Given:
		AccountReport expectedResult = accountSpi.readTransactionsByPeriod(accountId, dateFrom, dateTo, psuInvolved);
		String link = linkTo(AccountController.class).slash(accountId).toString();
		expectedResult.get_links().setAccount_link(link);

		//When:
		AccountReport actualResult = accountService.getAccountReport(accountId, dateFrom, dateTo, null, psuInvolved);

		//Then:
		assertThat(actualResult).isEqualTo(expectedResult);
	}

	private void checkTransactionResultsByTransactionId(String accountId, String transactionId, boolean psuInvolved) {
		//Given:
		AccountReport expectedResult = accountSpi.readTransactionsById(accountId, transactionId, psuInvolved);
		String link = linkTo(AccountController.class).slash(accountId).toString();
		expectedResult.get_links().setAccount_link(link);

		//When:
		AccountReport actualResult = accountService.getAccountReport(accountId, null, null, transactionId, psuInvolved);


		//Then:
		assertThat(actualResult).isEqualTo(expectedResult);
	}

	private void checkBalanceResults(String accountId, boolean psuInvolved) {
		//Given:
		Balances expectedResult = accountSpi.readBalances(accountId, psuInvolved);

		//When:
		Balances actualResult = accountService.getBalances(accountId, psuInvolved);

		//Then:
		assertThat(actualResult).isEqualTo(expectedResult);
	}

	private void checkAccountResults(boolean withBalance, boolean psuInvolved) {
		List<Account> accounts = accountSpi.readAccounts(withBalance, psuInvolved);
		List<AccountResponse> expectedResult = accountsToAccountResponseList(accounts);

		//When:
		List<AccountResponse> actualResponse = accountService.getAccountResponses(withBalance, psuInvolved);

		//Then:
		assertThat(expectedResult).isEqualTo(actualResponse);
	}

	private List<AccountResponse> accountsToAccountResponseList(List<Account> accounts) {
		String urlToAccount = linkTo(AccountController.class).toString();

		return accounts.stream()
				.map(account -> new AccountResponse(account, urlToAccount))
				.collect(Collectors.toList());
	}
}
