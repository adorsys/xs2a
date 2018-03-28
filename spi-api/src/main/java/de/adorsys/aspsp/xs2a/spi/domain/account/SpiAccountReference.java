package de.adorsys.aspsp.xs2a.spi.domain.account;

import lombok.Data;

import java.util.Currency;

@Data
public class SpiAccountReference {
    private final String accountId;
    private final String iban;
    private final String bban;
    private final String pan;
    private final String maskedPan;
    private final String msisdn;
    private final Currency currency;
}
