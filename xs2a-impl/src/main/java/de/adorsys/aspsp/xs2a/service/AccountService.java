package de.adorsys.aspsp.xs2a.service;

import com.google.gson.Gson;
import de.adorsys.aspsp.xs2a.spi.domain.Account;
import de.adorsys.aspsp.xs2a.spi.domain.AccountReport;
import de.adorsys.aspsp.xs2a.spi.domain.Balances;
import de.adorsys.aspsp.xs2a.spi.domain.Links;
import de.adorsys.aspsp.xs2a.spi.domain.ais.AccountResponse;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.aspsp.xs2a.web.AccountController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Service
public class AccountService {
	@Value("${application.ais.transaction.max-length}")
	private int MAX_LENGTH;

	@Autowired
	private AccountSpi accountSpi;

	public List<AccountResponse> getAccountResponses(boolean withBalance, boolean psuInvolved) {

		List<Account> accounts = accountSpi.readAccounts(withBalance, psuInvolved);
		String urlToAccount = linkTo(AccountController.class).toUriComponentsBuilder().build().getPath();

		return accounts.stream()
				.map(account -> new AccountResponse(account, urlToAccount))
				.collect(Collectors.toList());
	}

	public Balances getBalances(String accountId, boolean psuInvolved) {
		return accountSpi.readBalances(accountId, psuInvolved);
	}

	public AccountReport getAccountReport(String accountId, Date dateFrom, Date dateTo, String transactionId,
	                                      boolean psuInvolved) {
		AccountReport accountReport;

		if (transactionId == null || transactionId.isEmpty()) {
			accountReport = accountSpi.readTransactionsByPeriod(accountId, dateFrom, dateTo, psuInvolved);
		} else {
			accountReport = accountSpi.readTransactionsById(accountId, transactionId, psuInvolved);
		}

		return getReportAccordingMaxSize(accountReport, accountId);
	}

	private AccountReport getReportAccordingMaxSize(AccountReport accountReport, String accountId) {

		String jsonReport = new Gson().toJson(accountReport);

		if (jsonReport.length() > MAX_LENGTH) {
			return getAccountReportWithDownloadLink(accountId);
		}

		String urlToAccount = linkTo(AccountController.class).slash(accountId).toString();
		accountReport.get_links().setAccount_link(urlToAccount);
		return accountReport;
	}

	public AccountReport getAccountReportWithDownloadLink(String accountId) {
		// todo further we should implement real flow for download file
		String urlToDownload = linkTo(AccountController.class).slash(accountId).slash("transactions/download").toString();
		Links downloadLink = new Links();
		downloadLink.setDownload(urlToDownload);
		return new AccountReport(null, null, downloadLink);
	}
}
