package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.spi.domain.Account;
import de.adorsys.aspsp.xs2a.spi.domain.AccountReport;
import de.adorsys.aspsp.xs2a.spi.domain.Balances;
import de.adorsys.aspsp.xs2a.spi.domain.ais.AccountResponse;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.aspsp.xs2a.web.AccountController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Service
public class AccountService {

	@Autowired
	private AccountSpi accountSpi;

	public List<AccountResponse> getAccountResponses(boolean withBalance, boolean psuInvolved) {

		List<Account> accounts = accountSpi.readAccounts(withBalance, psuInvolved);
		String urlToAccount = linkTo(AccountController.class).toString();

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

		String link = linkTo(AccountController.class).slash(accountId).toString();
		accountReport.get_links().setAccount_link(link);

		return accountReport;
	}
}
