package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Message codes for HTTP Codes 201 to a Payment Initiation Request.
 */
public enum MessageCode201PaymentInitiation {

  WARNING("WARNING"),

  BENEFICIARY_WHITELISTING_REQUIRED("BENEFICIARY_WHITELISTING_REQUIRED");

  private String value;

  MessageCode201PaymentInitiation(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static MessageCode201PaymentInitiation fromValue(String text) {
    for (MessageCode201PaymentInitiation b : MessageCode201PaymentInitiation.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}

