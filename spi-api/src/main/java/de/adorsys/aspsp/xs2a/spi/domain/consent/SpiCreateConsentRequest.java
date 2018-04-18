package de.adorsys.aspsp.xs2a.spi.domain.consent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpiCreateConsentRequest {
    private SpiAccountAccess access;
    private boolean recurringIndicator;
    private Date validUntil;
    private Integer frequencyPerDay;
    private boolean combinedServiceIndicator;
}
