package de.adorsys.psd2.model;

import java.util.Objects;
import io.swagger.annotations.ApiModel;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * The transaction status is filled with codes of the ISO 20022 data table. Only the codes RCVD, PATC, ACTC, ACWC and RJCT are used: - 'ACSP': 'AcceptedSettlementInProcess' -    All preceding checks such as technical validation and customer profile were successful and therefore the payment initiation has been accepted for execution. - 'ACTC': 'AcceptedTechnicalValidation' -    Authentication and syntactical and semantical validation are successful. - 'ACWC': 'AcceptedWithChange' -    Instruction is accepted but a change will be made, such as date or remittance not sent. - 'RCVD': 'Received' -    Payment initiation has been received by the receiving agent. - 'RJCT': 'Rejected' -    Payment initiation or individual transaction included in the payment initiation has been rejected. 
 */
public enum TransactionStatusSB {
  
  ACSC("ACSC"),
  
  ACTC("ACTC"),
  
  ACWC("ACWC"),
  
  RCVD("RCVD"),
  
  RJCT("RJCT");

  private String value;

  TransactionStatusSB(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static TransactionStatusSB fromValue(String text) {
    for (TransactionStatusSB b : TransactionStatusSB.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}

