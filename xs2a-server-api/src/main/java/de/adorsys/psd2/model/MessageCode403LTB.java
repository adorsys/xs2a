package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Message codes defined for Trusted Beneficiaries for HTTP Error code 403 (FORBIDDEN).
 */
public enum MessageCode403LTB {
  CONSENT_UNKNOWN("CONSENT_UNKNOWN"),
    SERVICE_BLOCKED("SERVICE_BLOCKED"),
    RESOURCE_UNKNOWN("RESOURCE_UNKNOWN"),
    RESOURCE_EXPIRED("RESOURCE_EXPIRED");

  private String value;

  MessageCode403LTB(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static MessageCode403LTB fromValue(String text) {
    for (MessageCode403LTB b : MessageCode403LTB.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
