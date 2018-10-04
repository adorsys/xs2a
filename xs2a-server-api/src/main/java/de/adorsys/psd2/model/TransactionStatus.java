package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The transaction status is filled with codes of the ISO 20022 data table: - 'ACCP': 'AcceptedCustomerProfile' -    Preceding check of technical validation was successful.    Customer profile check was also successful. - 'ACSC': 'AcceptedSettlementCompleted' -    Settlement on the debtor’s account has been completed.      **Usage:** this can be used by the first agent to report to the debtor that the transaction has been completed.       **Warning:** this status is provided for transaction status reasons, not for financial information.    It can only be used after bilateral agreement. - 'ACSP': 'AcceptedSettlementInProcess' -    All preceding checks such as technical validation and customer profile were successful and therefore the payment initiation has been accepted for execution. - 'ACTC': 'AcceptedTechnicalValidation' -    Authentication and syntactical and semantical validation are successful. - 'ACWC': 'AcceptedWithChange' -    Instruction is accepted but a change will be made, such as date or remittance not sent. - 'ACWP': 'AcceptedWithoutPosting' -    Payment instruction included in the credit transfer is accepted without being posted to the creditor customer’s account. - 'RCVD': 'Received' -    Payment initiation has been received by the receiving agent. - 'PDNG': 'Pending' -    Payment initiation or individual transaction included in the payment initiation is pending.    Further checks and status update will be performed. - 'RJCT': 'Rejected' -    Payment initiation or individual transaction included in the payment initiation has been rejected. - 'CANC': 'Cancelled'   Payment initiation has been cancelled before execution      **Remark:** *Change Request to ISO20022 is still needed.*
 */
public enum TransactionStatus {

    ACCP("ACCP"),

    ACSC("ACSC"),

    ACSP("ACSP"),

    ACTC("ACTC"),

    ACWC("ACWC"),

    ACWP("ACWP"),

    RCVD("RCVD"),

    PDNG("PDNG"),

    RJCT("RJCT"),

    CANC("CANC");

    private String value;

    TransactionStatus(String value) {
        this.value = value;
    }

    @JsonCreator
    public static TransactionStatus fromValue(String text) {
        for (TransactionStatus b : TransactionStatus.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }
}

