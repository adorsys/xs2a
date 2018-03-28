package de.adorsys.aspsp.xs2a.spi.domain.account;

import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccess;
import lombok.Data;

import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;

import java.util.Date;

@Data
public class SpiAccountConsent {
    private final String id;

    private final SpiAccountAccess access;

    private final boolean recurringIndicator;

    private final Date validUntil;

    private final int frequencyPerDay;

    private final Date lastActionDate;

    private final SpiTransactionStatus spiTransactionStatus;

    private final SpiConsentStatus spiConsentStatus;

    private final boolean withBalance;

    private final boolean tppRedirectPreferred;
}
