package de.adorsys.aspsp.xs2a.spi.domain.account;

import lombok.Data;

@Data
public class SpiBalances {
	private SpiAccountBalance closingBooked;
	private SpiAccountBalance expected;
	private SpiAccountBalance authorised;
	private SpiAccountBalance openingBooked;
	private SpiAccountBalance interimAvailable;
}
