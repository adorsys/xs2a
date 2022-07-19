package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Message codes defined for AIS for HTTP Error code 405 (METHOD NOT ALLOWED).
 */
public enum MessageCode405AIS {
  SERVICE_INVALID("SERVICE_INVALID");

  private String value;

  MessageCode405AIS(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static MessageCode405AIS fromValue(String text) {
    for (MessageCode405AIS b : MessageCode405AIS.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
