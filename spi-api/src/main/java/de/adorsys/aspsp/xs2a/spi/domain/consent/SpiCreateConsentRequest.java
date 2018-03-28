package de.adorsys.aspsp.xs2a.spi.domain.consent;

import lombok.Data;

import java.util.Date;

@Data
public class SpiCreateConsentRequest {

    private final SpiAccountAccess access;

    private final boolean recurringIndicator;

    private final Date validUntil;

    private final Integer frequencyPerDay;

    private final boolean combinedServiceIndicator;
}
