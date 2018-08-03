package de.adorsys.aspsp.xs2a.spi.domain;

import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import lombok.Value;

@Value
public class SpiResponse<T> {
    private T payload;
    private AspspConsentData aspspConsentData;
}
