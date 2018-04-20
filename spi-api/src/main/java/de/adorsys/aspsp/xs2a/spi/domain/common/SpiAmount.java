package de.adorsys.aspsp.xs2a.spi.domain.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Currency;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpiAmount {
    private Currency currency;
    private String content;
}
