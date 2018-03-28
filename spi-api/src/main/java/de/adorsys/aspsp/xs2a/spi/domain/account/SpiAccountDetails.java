package de.adorsys.aspsp.xs2a.spi.domain.account;

import lombok.Data;

import java.util.Currency;

@Data
public class SpiAccountDetails {

    private final String id;

    private final String iban;

    private final String bban;

    private final String pan;

    private final String maskedPan;

    private final String msisdn;

    private final Currency currency;

    private final String name;

    private final String accountType;

    private final SpiAccountType cashSpiAccountType;

    private final String bic;

    private final SpiBalances balances;
}
