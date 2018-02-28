package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.spi.domain.Account;
import de.adorsys.aspsp.xs2a.spi.domain.AccountReport;
import de.adorsys.aspsp.xs2a.spi.domain.Balances;
import de.adorsys.aspsp.xs2a.spi.domain.Links;
import de.adorsys.aspsp.xs2a.spi.domain.aic.AccountResponse;
import de.adorsys.aspsp.xs2a.spi.impl.AccountSPIImpl;
import de.adorsys.aspsp.xs2a.spi.service.AccountSPI;
import de.adorsys.aspsp.xs2a.web.AccountController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * Created by aro on 27.11.17.
 */
@Service
public class AccountService {

    @Autowired
    private AccountSPIImpl bankServiceProvider = new AccountSPIImpl();
    private AccountSPI accountSPI = (AccountSPI) bankServiceProvider;

    public List<AccountResponse> getAccountResponses(boolean withBalance, boolean psuInvolved) {

        List<Account> accounts = getAccounts(withBalance, psuInvolved);

        return accounts.stream()
                .map(this::convertAccountToAccountResponse)
                .collect(Collectors.toList());
    }

    public Balances getBalances(String accountId, boolean psuInvolved) {
        return accountSPI.readBalances(accountId, psuInvolved);
    }

    public AccountReport getAccountReport(String accountId, Date dateFrom, Date dateTo, String transactionId,
                                          boolean psuInvolved) {
        AccountReport accountReport;

        if (transactionId == null || transactionId.isEmpty()) {
            accountReport = accountSPI.readTransactionsByPeriod(accountId, dateFrom, dateTo, psuInvolved);
        } else {
            accountReport = accountSPI.readTransactionsById(accountId, transactionId, psuInvolved);
        }

        String link = linkTo(AccountController.class).slash(accountId).toString();
        accountReport.get_links().setAccount_link(link);

        return accountReport;
    }

    public List<Account> getAccounts(boolean withBalance, boolean psuInvolved) {
        return accountSPI.readAccounts(withBalance, psuInvolved);
    }

    public AccountResponse convertAccountToAccountResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getIban(),
                account.getAccount_type(),
                account.getCurrency(),
                account.getBalances(),
                getChangedLinksByAccountId(account.get_links(), account.getId()));
    }

    private Links getChangedLinksByAccountId(Links links, String accountId) {
        String balancesLink = linkTo(AccountController.class).slash(accountId).toString() + "/balances";
        String transactionsLink = linkTo(AccountController.class).slash(accountId).toString() + "/transactions";
        links.setBalances(balancesLink);
        links.setTransactions(transactionsLink);
        return links;
    }
}
