package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Message codes for HTTP Error codes 2XX.
 */
public enum MessageCode2XX {

  WARNING("WARNING");

  private String value;

  MessageCode2XX(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static MessageCode2XX fromValue(String text) {
    for (MessageCode2XX b : MessageCode2XX.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}

