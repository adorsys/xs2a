package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets exchangeRateTypeCode
 */
public enum ExchangeRateTypeCode {

  SPOT("SPOT"),

  SALE("SALE"),

  AGRD("AGRD");

  private String value;

  ExchangeRateTypeCode(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static ExchangeRateTypeCode fromValue(String text) {
    for (ExchangeRateTypeCode b : ExchangeRateTypeCode.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}

