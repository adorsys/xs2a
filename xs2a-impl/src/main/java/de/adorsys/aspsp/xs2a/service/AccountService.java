package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.spi.domain.Account;
import de.adorsys.aspsp.xs2a.spi.domain.AccountReport;
import de.adorsys.aspsp.xs2a.spi.domain.Balances;
import de.adorsys.aspsp.xs2a.spi.domain.aic.AccountResponse;
import de.adorsys.aspsp.xs2a.spi.impl.AccountSPIImpl;
import de.adorsys.aspsp.xs2a.spi.service.AccountSPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Created by aro on 27.11.17.
 */
@Service
public class AccountService {

    @Autowired
    private AccountSPIImpl bankServiceProvider = new AccountSPIImpl();
    private AccountSPI accountSPI = (AccountSPI) bankServiceProvider;

    public List<AccountResponse> getAccounts(Boolean withBalance, Boolean psuInvolved) {

        List<Account> accounts = readAccounts(withBalance, psuInvolved);

        return accounts.stream()
                .map(account -> new AccountResponse(
                        account.getId(),
                        account.getIban(),
                        account.getAccount_type(),
                        account.getCurrency(),
                        account.getBalances(),
                        account.get_links()))
                .collect(Collectors.toList());
    }

    public Balances getBalances(String accountId, Boolean psuInvolved) {
        return accountSPI.readBalances(accountId, psuInvolved);
    }

    public AccountReport getTransactionsForAccount(String accountId, String dateFrom,
                                                   String dateTo,
                                                   Boolean psuInvolved) {
        return accountSPI.readTransactions(accountId, dateFrom, dateTo, psuInvolved);
    }

    private List<Account> readAccounts(Boolean withBalance, Boolean psuInvolved) {
        return accountSPI.readAccounts(withBalance, psuInvolved);
    }
}
