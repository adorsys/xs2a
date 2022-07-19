package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Message codes for HTTP Error code 429 (TOO MANY REQUESTS).
 */
public enum MessageCode429AIS {
  ACCESS_EXCEEDED("ACCESS_EXCEEDED");

  private String value;

  MessageCode429AIS(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static MessageCode429AIS fromValue(String text) {
    for (MessageCode429AIS b : MessageCode429AIS.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
