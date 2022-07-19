package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Message codes defined for Trusted Beneficiaries for HTTP Error code 401 (UNAUTHORIZED).
 */
public enum MessageCode401LTB {
  CERTIFICATE_INVALID("CERTIFICATE_INVALID"),
    ROLE_INVALID("ROLE_INVALID"),
    CERTIFICATE_EXPIRED("CERTIFICATE_EXPIRED"),
    CERTIFICATE_BLOCKED("CERTIFICATE_BLOCKED"),
    CERTIFICATE_REVOKE("CERTIFICATE_REVOKE"),
    CERTIFICATE_MISSING("CERTIFICATE_MISSING"),
    SIGNATURE_INVALID("SIGNATURE_INVALID"),
    SIGNATURE_MISSING("SIGNATURE_MISSING"),
    CORPORATE_ID_INVALID("CORPORATE_ID_INVALID"),
    PSU_CREDENTIALS_INVALID("PSU_CREDENTIALS_INVALID"),
    CONSENT_INVALID("CONSENT_INVALID"),
    CONSENT_EXPIRED("CONSENT_EXPIRED"),
    TOKEN_UNKNOWN("TOKEN_UNKNOWN"),
    TOKEN_INVALID("TOKEN_INVALID"),
    TOKEN_EXPIRED("TOKEN_EXPIRED");

  private String value;

  MessageCode401LTB(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static MessageCode401LTB fromValue(String text) {
    for (MessageCode401LTB b : MessageCode401LTB.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
