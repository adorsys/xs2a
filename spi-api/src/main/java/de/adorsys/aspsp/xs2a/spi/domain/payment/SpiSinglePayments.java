package de.adorsys.aspsp.xs2a.spi.domain.payment;

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import lombok.Data;

import java.util.Date;

@Data
public class SpiSinglePayments {
    private final String endToEndIdentification;
    private final SpiAccountReference debtorAccount;
    private final String ultimateDebtor;
    private final SpiAmount instructedAmount;
    private final SpiAccountReference creditorAccount;
    private final String creditorAgent;
    private final String creditorName;
    private final String creditorAddress;
    private final String ultimateCreditor;
    private final String purposeCode;
    private final String remittanceInformationUnstructured;
    private final String remittanceInformationStructured;
    private final Date requestedExecutionDate;
    private final Date requestedExecutionTime;
}
