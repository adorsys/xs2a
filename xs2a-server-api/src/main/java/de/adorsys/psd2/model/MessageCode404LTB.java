package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Message codes defined for Trusted Beneficiaries for HTTP Error code 404 (NOT FOUND).
 */
public enum MessageCode404LTB {
  RESOURCE_UNKNOWN("RESOURCE_UNKNOWN");

  private String value;

  MessageCode404LTB(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static MessageCode404LTB fromValue(String text) {
    for (MessageCode404LTB b : MessageCode404LTB.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
