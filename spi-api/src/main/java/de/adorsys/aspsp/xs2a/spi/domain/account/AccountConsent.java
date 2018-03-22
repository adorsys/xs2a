package de.adorsys.aspsp.xs2a.spi.domain.account;

import lombok.Data;

import de.adorsys.aspsp.xs2a.spi.domain.common.TransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ConsentStatus;

import java.util.Date;

@Data
public class AccountConsent {
    private final String id;

    private final AccountAccess access;

    private final boolean recurringIndicator;

    private final Date validUntil;

    private final int frequencyPerDay;

    private final Date lastActionDate;

    private final TransactionStatus transactionStatus;

    private final ConsentStatus consentStatus;

    private final boolean withBalance;

    private final boolean tppRedirectPreferred;
}
