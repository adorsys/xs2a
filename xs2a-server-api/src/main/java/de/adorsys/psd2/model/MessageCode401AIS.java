package de.adorsys.psd2.model;

import java.util.Objects;
import io.swagger.annotations.ApiModel;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Message codes defined for AIS for HTTP Error code 401 (UNAUTHORIZED).
 */
public enum MessageCode401AIS {
  
  CERTIFICATE_INVALID("CERTIFICATE_INVALID"),
  
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

  MessageCode401AIS(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static MessageCode401AIS fromValue(String text) {
    for (MessageCode401AIS b : MessageCode401AIS.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}

