package de.adorsys.aspsp.xs2a.domain;

import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReference;

import java.util.Set;

public interface AccountReferenceCollector {
    Set<Xs2aAccountReference> getAccountReferences();
}
