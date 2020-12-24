package de.adorsys.psd2.core.payment.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PaymentInitiationJson {
    private String endToEndIdentification;
    private String instructionIdentification;
    private String debtorName;
    private AccountReference debtorAccount;
    private String ultimateDebtor;
    private Amount instructedAmount;
    private AccountReference creditorAccount;
    private String creditorAgent;
    private String creditorAgentName;
    private String creditorName;
    private Address creditorAddress;
    private String creditorId;
    private String ultimateCreditor;
    private PurposeCode purposeCode;
    private ChargeBearer chargeBearer;
    private String remittanceInformationUnstructured;
    private RemittanceInformationStructured remittanceInformationStructured;
    private List<RemittanceInformationStructured> remittanceInformationStructuredArray;
    private LocalDate requestedExecutionDate;
}

