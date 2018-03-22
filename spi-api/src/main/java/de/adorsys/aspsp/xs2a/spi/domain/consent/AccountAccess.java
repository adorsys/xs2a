package de.adorsys.aspsp.xs2a.spi.domain.consent;

import lombok.Data;

import de.adorsys.aspsp.xs2a.spi.domain.account.AccountReference;

import java.util.List;

@Data
public class AccountAccess {

    private List<AccountReference> accounts;

    private List<AccountReference> balances;

    private List<AccountReference> transactions;

    private AccountAccessType availableAccounts;

    private AccountAccessType allPsd2;
}
