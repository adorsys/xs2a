package de.adorsys.aspsp.xs2a.spi.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by aro on 23.11.17.
 */
@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public enum TransactionStatus {

    ACCP("AcceptedCustomerProfile", "Preceding check of technical validation was successful. Customer profile check was also successful."),
    ACSC("AcceptedSettlementCompleted", "Settlement on th’ debtor's account has been completed. Usage : this can be used by the first agent to report to the debtor that the transaction has been completed. Warning : this status is provided for transaction status reasons, not for financial information. It can only be used after bilateral agreement"),
    ACSP("AcceptedSettlementInProcess", "All preceding checks such as technical validation and customer profile were successful and therefore the payment initiation has been accepted for execution."),
    ACTC("AcceptedTechnicalValidation", "Authentication and syntactical and semantical validation are successful"),
    ACWC("AcceptedWithChange", "Instruction is accepted but a change will be made, such as date or remittance not sent."),
    ACWP("AcceptedWithoutPosting", "Payment instruction included in the credit transfer is accepted without being posted to the creditor customer’s account."),
    RCVD("Received", "Payment initiation has been received by the receiving agent."),
    PDNG("Pending", "Payment initiation or individual transaction included in the payment initiation is pending. Further checks and status update will be performed."),
    RJCT("Rejected", "Payment initiation or individual transaction included in the payment initiation has been rejected.");

    TransactionStatus(String name, String definition) {
        this.name = name;
        this.definition = definition;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    private String name;
    private String definition;
}
