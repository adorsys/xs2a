package de.adorsys.aspsp.xs2a.spi.domain;

import lombok.Value;

@Value
public class SpiResponse<T> {
    private T payload;
    private byte[] aspspConsentData;
}
