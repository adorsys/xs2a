package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Message codes defined for AIS for HTTP Error code 403 (FORBIDDEN).
 */
public enum MessageCode403AIS {
  CONSENT_UNKNOWN("CONSENT_UNKNOWN"),
    SERVICE_BLOCKED("SERVICE_BLOCKED"),
    RESOURCE_UNKNOWN("RESOURCE_UNKNOWN"),
    RESOURCE_EXPIRED("RESOURCE_EXPIRED");

  private String value;

  MessageCode403AIS(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static MessageCode403AIS fromValue(String text) {
    for (MessageCode403AIS b : MessageCode403AIS.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
