package de.adorsys.psd2.model;

import java.util.Objects;
import io.swagger.annotations.ApiModel;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Charge Bearer. ChargeBearerType1Code from ISO20022.
 */
public enum ChargeBearer {
  
  DEBT("DEBT"),
  
  CRED("CRED"),
  
  SHAR("SHAR"),
  
  SLEV("SLEV");

  private String value;

  ChargeBearer(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static ChargeBearer fromValue(String text) {
    for (ChargeBearer b : ChargeBearer.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}

