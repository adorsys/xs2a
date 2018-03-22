package de.adorsys.aspsp.xs2a.spi.domain.account;

import lombok.Data;

import de.adorsys.aspsp.xs2a.spi.domain.common.Amount;

import java.util.Date;

@Data
public class Transaction {
    private final String transactionId;
    private final String endToEndId;
    private final String mandateId;
    private final String creditorId;
    private final Date bookingDate;
    private final Date valueDate;
    private final Amount amount;
    private final String creditorName;
    private final AccountReference creditorAccount;
    private final String ultimateCreditor;
    private final String debtorName;
    private final AccountReference debtorAccount;
    private final String ultimateDebtor;
    private final String remittanceInformationUnstructured;
    private final String remittanceInformationStructured;
    private final String purposeCode;
    private final String bankTransactionCodeCode;

}
