package de.adorsys.psd2.model;


import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * This is the bulk status as defined in the related bulk status endpoint. The following definitions and values are dervied from ISO20022: - 'ACCP' AcceptedCustomerProfile: Preceding check of technical validation was successful. Customer profile check was also successful. - 'ACCC' AcceptedSettlementCompletedCreditorAccount: Settlement on the creditor's account has been completed. - 'ACSC' AcceptedSettlementCompletedDebitorAccount:  Settlement on the debtor's account has been completed.   Usage: this can be used by the first agent to report to the debtor that the transaction has been completed.    Warning: this status is provided for transaction status reasons, not for financial information. It can only be used after bilateral agreement - 'ACSP' AcceptedSettlementInProcess: All preceding checks such as technical validation and customer profile were successful and therefore the payment initiation has been accepted for execution. - 'ACTC' AcceptedTechnicalValidation: Authentication and syntactical and semantical validation are successful - 'ACWC' AcceptedWithChange: Instruction is accepted but a change will be made, such as date or remittance not sent. - 'PART' PartiallyAccepted: A number of transactions have been accepted, whereas another number of transactions have not yet achieved 'accepted' status. - 'PDNG' Pending: Payment initiation or individual transaction included in the payment initiation is pending. Further checks and status update will be performed. - 'RCVD' Received: Payment initiation has been received by the receiving agent - 'RJCT' Rejected: Payment initiation or individual transaction included in the payment initiation has been rejected.
 */
public enum GroupStatus {
  ACCC("ACCC"),
    ACCP("ACCP"),
    ACSC("ACSC"),
    ACSP("ACSP"),
    ACTC("ACTC"),
    ACWC("ACWC"),
    RCVD("RCVD"),
    PDNG("PDNG"),
    RJCT("RJCT"),
    PART("PART");

  private String value;

  GroupStatus(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static GroupStatus fromValue(String text) {
    for (GroupStatus b : GroupStatus.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
