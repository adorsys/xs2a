package de.adorsys.psd2.model;

import jakarta.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonCreator;


/**
 * See document \\\"openFinance API Framework Data Dictionary\\\", section \\\"Service Unspecific HTTP Error Codes\\\" for more details.
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public enum MessageCodeServiceUnspecific409 {

  STATUS_INVALID("STATUS_INVALID"),

  CHANGE_INVALID("CHANGE_INVALID");

  private final String value;

  MessageCodeServiceUnspecific409(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static MessageCodeServiceUnspecific409 fromValue(String value) {
    for (MessageCodeServiceUnspecific409 b : MessageCodeServiceUnspecific409.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

