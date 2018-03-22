package de.adorsys.aspsp.xs2a.spi.domain.common;

import lombok.Data;

import java.util.Currency;

@Data
public class Amount {
	private final Currency currency;
	private final String content;
}
