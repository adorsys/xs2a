package de.adorsys.aspsp.xs2a.spi.domain.payment;

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Data
public class SpiSinglePayments {

    @Id
    private String paymentId;
    private String endToEndIdentification;
    private SpiAccountReference debtorAccount;
    private String ultimateDebtor;
    private SpiAmount instructedAmount;
    private SpiAccountReference creditorAccount;
    private String creditorAgent;
    private String creditorName;
    private SpiAddress creditorAddress;
    private String ultimateCreditor;
    private String purposeCode;
    private String remittanceInformationUnstructured;
    private SpiRemittance remittanceInformationStructured;
    private Date requestedExecutionDate;
    private Date requestedExecutionTime;

}
