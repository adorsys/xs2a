package de.adorsys.psd2.model;

import jakarta.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonCreator;


/**
 * See document \\\"openFinance API Framework Data Dictionary\\\", section \\\"Service Unspecific HTTP Error Codes\\\" for more details.
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public enum MessageCodeServiceUnspecific400 {

  FORMAT_ERROR("FORMAT_ERROR"),

  PARAMETER_NOT_CONSISTENT("PARAMETER_NOT_CONSISTENT"),

  PARAMETER_NOT_SUPPORTED("PARAMETER_NOT_SUPPORTED"),

  SERVICE_INVALID("SERVICE_INVALID"),

  CONSENT_UNKNOWN("CONSENT_UNKNOWN"),

  RESOURCE_UNKNOWN("RESOURCE_UNKNOWN"),

  RESOURCE_EXPIRED("RESOURCE_EXPIRED"),

  RESOURCE_BLOCKED("RESOURCE_BLOCKED"),

  TIMESTAMP_INVALID("TIMESTAMP_INVALID"),

  PERIOD_INVALID("PERIOD_INVALID"),

  SCA_METHOD_UNKNOWN("SCA_METHOD_UNKNOWN"),

  SCA_INVALID("SCA_INVALID");

  private final String value;

  MessageCodeServiceUnspecific400(String value) {
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
  public static MessageCodeServiceUnspecific400 fromValue(String value) {
    for (MessageCodeServiceUnspecific400 b : MessageCodeServiceUnspecific400.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

