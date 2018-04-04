package de.adorsys.aspsp.xs2a.spi.domain.payment;

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;

import java.util.Date;

public class SpiSinglePayment {
    private String endToEndIdentification;
    private SpiAccountReference debtorAccount;
    private String ultimateDebtor;
    private SpiAmount instructedAmount;
    private SpiAccountReference creditorAccount;
    private String creditorAgent;
    private String creditorName;
    private String creditorAddress;
    private String ultimateCreditor;
    private String purposeCode;
    private String remittanceInformationUnstructured;
    private String remittanceInformationStructured;
    private Date requestedExecutionDate;

}
