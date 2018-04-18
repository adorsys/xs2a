package de.adorsys.aspsp.xs2a.spi.domain.account;

import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SpiAccountConsent {
    private String id;
    private SpiAccountAccess access;
    private boolean recurringIndicator;
    private Date validUntil;
    private int frequencyPerDay;
    private Date lastActionDate;
    private SpiTransactionStatus spiTransactionStatus;
    private SpiConsentStatus spiConsentStatus;
    private boolean withBalance;
    private boolean tppRedirectPreferred;
}
