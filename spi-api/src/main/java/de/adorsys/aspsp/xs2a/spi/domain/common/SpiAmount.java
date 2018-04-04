package de.adorsys.aspsp.xs2a.spi.domain.common;

import lombok.Data;

import java.util.Currency;

@Data
public class SpiAmount {
    private final Currency currency;
    private final String content;
}
