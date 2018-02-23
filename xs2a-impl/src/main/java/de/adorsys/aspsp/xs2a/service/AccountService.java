package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.spi.domain.Account;
import de.adorsys.aspsp.xs2a.spi.domain.AccountReport;
import de.adorsys.aspsp.xs2a.spi.domain.Balances;
import de.adorsys.aspsp.xs2a.spi.domain.aic.AccountResponse;
import de.adorsys.aspsp.xs2a.spi.impl.AccountSPIImpl;
import de.adorsys.aspsp.xs2a.spi.service.AccountSPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


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
        List<AccountResponse> accountResponses = new ArrayList<>();
        for (Account account : accounts) {
            AccountResponse accountResponse = new AccountResponse();
            accountResponse.setId(account.getId());
            accountResponse.setIban(account.getIban());
            accountResponse.setCurrency(account.getCurrency());
            accountResponse.setAccount_type(account.getAccount_type());
            accountResponse.set_links(account.get_links());
            accountResponses.add(accountResponse);
        }
        return accountResponses;
    }

    public Balances getBalances(String accountId, Boolean psuInvolved) {
        return accountSPI.readBalances(accountId, psuInvolved);
    }

    public AccountReport getTransactionsForAccount(String accountId, String dateFROM,
                                                   String dateTo,
                                                   Boolean psuInvolved) {
        return accountSPI.readTransactions(accountId, dateFROM, dateTo, psuInvolved);

    }

    private List<Account> readAccounts(Boolean withBalance, Boolean psuInvolved) {
        return accountSPI.readAccounts(withBalance, psuInvolved);
    }
}
