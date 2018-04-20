package de.adorsys.aspsp.xs2a.spi.domain.common;

import lombok.Value;

import java.util.Currency;

@Value
public class SpiAmount {
    private final Currency currency;
    private final String content;
}
