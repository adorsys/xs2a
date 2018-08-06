package de.adorsys.aspsp.xs2a.domain;

import de.adorsys.aspsp.xs2a.domain.account.AccountReference;

import java.util.Set;

public interface AccountReferenceCollector {
    Set<AccountReference> getAccountReferences();
}
