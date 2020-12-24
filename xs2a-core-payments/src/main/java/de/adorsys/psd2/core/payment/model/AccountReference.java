package de.adorsys.psd2.core.payment.model;

import lombok.Data;

@Data
public class AccountReference {
    private String iban;
    private String bban;
    private String pan;
    private String maskedPan;
    private String msisdn;
    private String currency;
    private String otherAccountIdentification;
}

