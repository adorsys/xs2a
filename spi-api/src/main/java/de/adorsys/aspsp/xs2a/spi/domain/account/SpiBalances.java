package de.adorsys.aspsp.xs2a.spi.domain.account;

import lombok.Data;

@Data
public class SpiBalances {

	private AccountBalance closingBooked;

	private AccountBalance expected;

	private AccountBalance authorised;

	private AccountBalance openingBooked;

	private AccountBalance closing_booked;

	private AccountBalance interimAvailable;
}
