package de.adorsys.psd2.model;

import jakarta.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonCreator;


/**
 * See document \\\"openFinance API Framework Data Dictionary\\\", section \\\"Authentication Type\\\" for more details.
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public enum AuthenticationType {

  SMS_OTP("SMS_OTP"),

  CHIP_OTP("CHIP_OTP"),

  PHOTO_OTP("PHOTO_OTP"),

  PUSH_OTP("PUSH_OTP"),

  SMTP_OTP("SMTP_OTP");

  private final String value;

  AuthenticationType(String value) {
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
  public static AuthenticationType fromValue(String value) {
    for (AuthenticationType b : AuthenticationType.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

