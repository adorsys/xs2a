package de.adorsys.aspsp.xs2a.spi.domain.account;

import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import lombok.Data;

import java.util.Date;

@Data
public class SpiTransaction {
    private final String transactionId;
    private final String endToEndId;
    private final String mandateId;
    private final String creditorId;
    private final Date bookingDate;
    private final Date valueDate;
    private final SpiAmount spiAmount;
    private final String creditorName;
    private final SpiAccountReference creditorAccount;
    private final String ultimateCreditor;
    private final String debtorName;
    private final SpiAccountReference debtorAccount;
    private final String ultimateDebtor;
    private final String remittanceInformationUnstructured;
    private final String remittanceInformationStructured;
    private final String purposeCode;
    private final String bankTransactionCodeCode;

}
