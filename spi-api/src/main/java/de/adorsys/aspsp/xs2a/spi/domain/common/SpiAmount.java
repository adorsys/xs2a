package de.adorsys.aspsp.xs2a.spi.domain.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Currency;

@Data
@Builder
@AllArgsConstructor
public class SpiAmount {
    private final Currency currency;
    private final String content;
}
