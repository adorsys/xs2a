package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Message codes for HTTP codes 200 to a Payment Initiation Status Request.
 */
public enum MessageCode200InitiationStatus {

  AVAILABLE("FUNDS_NOT_AVAILABLE");

  private String value;

  MessageCode200InitiationStatus(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static MessageCode200InitiationStatus fromValue(String text) {
    for (MessageCode200InitiationStatus b : MessageCode200InitiationStatus.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}

