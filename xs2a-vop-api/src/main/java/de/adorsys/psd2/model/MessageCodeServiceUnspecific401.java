package de.adorsys.psd2.model;

import jakarta.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonCreator;


/**
 * See document \\\"openFinance API Framework Data Dictionary\\\", section \\\"Service Unspecific HTTP Error Codes\\\" for more details.
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public enum MessageCodeServiceUnspecific401 {

  CERTIFICATE_INVALID("CERTIFICATE_INVALID"),

  ROLE_INVALID("ROLE_INVALID"),

  CERTIFICATE_EXPIRED("CERTIFICATE_EXPIRED"),

  CERTIFICATE_BLOCKED("CERTIFICATE_BLOCKED"),

  CERTIFICATE_REVOKED("CERTIFICATE_REVOKED"),

  CERTIFICATE_MISSING("CERTIFICATE_MISSING"),

  CLIENT_INVALID("CLIENT_INVALID"),

  CLIENT_INCONSISTENT("CLIENT_INCONSISTENT"),

  API_CONTRACT_ID_INVALID("API_CONTRACT_ID_INVALID"),

  SIGNATURE_INVALID("SIGNATURE_INVALID"),

  SIGNATURE_MISSING("SIGNATURE_MISSING"),

  ROLE_INVALID2("ROLE_INVALID"),

  PSU_CREDENTIALS_INVALID("PSU_CREDENTIALS_INVALID"),

  CORPORATE_ID_INVALID("CORPORATE_ID_INVALID"),

  CONSENT_INVALID("CONSENT_INVALID"),

  CONSENT_EXPIRED("CONSENT_EXPIRED"),

  TOKEN_UNKNOWN("TOKEN_UNKNOWN"),

  TOKEN_INVALID("TOKEN_INVALID"),

  TOKEN_EXPIRED("TOKEN_EXPIRED");

  private final String value;

  MessageCodeServiceUnspecific401(String value) {
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
  public static MessageCodeServiceUnspecific401 fromValue(String value) {
    for (MessageCodeServiceUnspecific401 b : MessageCodeServiceUnspecific401.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

