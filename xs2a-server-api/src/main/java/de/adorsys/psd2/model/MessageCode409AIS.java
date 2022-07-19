package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Message codes defined for AIS for HTTP Error code 409 (CONFLICT).
 */
public enum MessageCode409AIS {
  STATUS_INVALID("STATUS_INVALID");

  private String value;

  MessageCode409AIS(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static MessageCode409AIS fromValue(String text) {
    for (MessageCode409AIS b : MessageCode409AIS.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
